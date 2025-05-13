package com.example.wellniaryproject

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val dao = AppDatabase.getDatabase(appContext).dietLogDao()
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "🔄 Starting daily sync task...")

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("SyncWorker", "⚠️ No user logged in. Skipping sync.")
            return Result.success()
        }

        return try {
            val unsyncedLogs = dao.getUnsyncedLogsByUid(user.uid)

            if (unsyncedLogs.isEmpty()) {
                Log.i("SyncWorker", "📭 No unsynced logs to sync for UID ${user.uid}")
                return Result.success()
            }

            val uploadedIds = mutableListOf<Int>()

            unsyncedLogs.forEach { log ->
                val logMap = hashMapOf(
                    "uid" to user.uid,
                    "date" to log.date,
                    "mealType" to log.mealType,
                    "staple" to log.staple,
                    "meat" to log.meat,
                    "vegetable" to log.vegetable,
                    "other" to log.other
                )

                firestore.collection("dietLogs")
                    .add(logMap)
                    .addOnSuccessListener {
                        Log.d("SyncWorker", "✅ Uploaded: ${log.id}")
                        uploadedIds.add(log.id)
                    }
                    .addOnFailureListener {
                        Log.e("SyncWorker", "❌ Failed to upload: ${log.id}", it)
                    }
            }

            // wait for a moment to make sure upload success(because .add is asynchronous)
            kotlinx.coroutines.delay(3000)

            //mark synced
            if (uploadedIds.isNotEmpty()) {
                dao.markLogsAsSynced(uploadedIds)
                Log.d("SyncWorker", "✅ Marked ${uploadedIds.size} logs as synced.")
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "❌ Sync failed with exception", e)
            Result.failure()
        }
    }

}
