package com.example.wellniaryproject

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FirebaseDietLog(
    val log: DietLogEntity,
    val docId: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DietRecordsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var userLogs by remember { mutableStateOf<List<FirebaseDietLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid != null) {
            userLogs = fetchDietLogsFromFirebase(uid)
        }
        isLoading = false
    }

    val groupedRecords = userLogs.groupBy { it.log.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diet Records") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color(0xFFBBDEFB),
                contentColor = Color.Black
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = Color(0xFFF0F8FF),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 4.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        if (groupedRecords.isEmpty()) {
                            item {
                                Text(
                                    text = "No diet records found.",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            groupedRecords.forEach { (date, records) ->
                                item {
                                    Text(
                                        text = "📅 $date",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0D47A1),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                items(records) { firebaseLog ->
                                    val record = firebaseLog.log
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = buildString {
                                                append("⤷ [${record.mealType}] ")
                                                append("Staple: ${record.staple}, ")
                                                append("Meat: ${record.meat}, ")
                                                append("Vegetables: ${record.vegetable}, ")
                                                append("Others: ${record.other}")
                                            },
                                            fontSize = 15.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = "🗑️",
                                            fontSize = 16.sp,
                                            color = Color.Red,
                                            modifier = Modifier
                                                .clickable {
                                                    uid?.let { userId ->
                                                        coroutineScope.launch {
                                                            val success = deleteDietLogFromFirebase(firebaseLog.docId)
                                                            if (success) {
                                                                userLogs = fetchDietLogsFromFirebase(userId)
                                                                Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Deletion failed", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                }
                                                .padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun fetchDietLogsFromFirebase(uid: String): List<FirebaseDietLog> {
    return try {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("dietLogs")
            .whereEqualTo("uid", uid)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            val date = doc.getString("date") ?: return@mapNotNull null
            val mealType = doc.getString("mealType") ?: ""
            val staple = doc.getString("staple") ?: ""
            val meat = doc.getString("meat") ?: ""
            val vegetable = doc.getString("vegetable") ?: ""
            val other = doc.getString("other") ?: ""

            FirebaseDietLog(
                log = DietLogEntity(
                    uid = uid,
                    date = date,
                    mealType = mealType,
                    staple = staple,
                    meat = meat,
                    vegetable = vegetable,
                    other = other
                ),
                docId = doc.id
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun deleteDietLogFromFirebase(docId: String): Boolean {
    return try {
        FirebaseFirestore.getInstance()
            .collection("dietLogs")
            .document(docId)
            .delete()
            .await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
