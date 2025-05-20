package com.example.wellniaryproject

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Me(navController: NavHostController) {
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    var isLogin by remember { mutableStateOf(true) }
    var needsProfileSetup by remember { mutableStateOf<Boolean?>(null) }

    var showReminderSettings by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val uid = user.uid
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "my_app_db").build()
            val dao = db.userProfileDao()

            val localProfile = dao.getProfileByUid(uid)

            val isLocalValid = localProfile != null &&
                    !localProfile.email.isNullOrBlank() &&
                    !localProfile.username.isNullOrBlank() &&
                    !localProfile.birthday.isNullOrBlank() &&
                    !localProfile.gender.isNullOrBlank() &&
                    !localProfile.height.isNullOrBlank() &&
                    !localProfile.weight.isNullOrBlank()

            if (isLocalValid) {
                needsProfileSetup = false
            } else {
                val firebaseRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .reference.child("users").child(uid)

                firebaseRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val email = snapshot.child("email").getValue(String::class.java)
                        val username = snapshot.child("username").getValue(String::class.java)
                        val birthday = snapshot.child("birthday").getValue(String::class.java)
                        val gender = snapshot.child("gender").getValue(String::class.java)
                        val height = snapshot.child("height").getValue(String::class.java)
                        val weight = snapshot.child("weight").getValue(String::class.java)

                        val isRemoteValid = !email.isNullOrBlank() &&
                                !username.isNullOrBlank() &&
                                !birthday.isNullOrBlank() &&
                                !gender.isNullOrBlank() &&
                                !height.isNullOrBlank() &&
                                !weight.isNullOrBlank()

                        if (isRemoteValid) {
                            val profile = UserProfile(
                                uid = uid,
                                email = email,
                                username = username,
                                birthday = birthday,
                                gender = gender,
                                state = snapshot.child("state").getValue(String::class.java) ?: "",
                                height = height,
                                weight = weight
                            )

                            scope.launch(Dispatchers.IO) {
                                dao.insertProfile(profile)
                            }

                            needsProfileSetup = false
                        } else {
                            needsProfileSetup = true
                        }
                    } else {
                        needsProfileSetup = true
                    }
                }.addOnFailureListener {
                    needsProfileSetup = true
                }
            }
        }
    }



    when {
        currentUser == null -> {
            if (isLogin) {
                Login(
                    navController = navController,
                    onSwitchToSignup = { isLogin = false },
                    onSuccessLogin = {
                        currentUser = FirebaseAuth.getInstance().currentUser
                    }
                )
            } else {
                Signup(
                    onRegisterSuccess = {
                        isLogin = true
                    },
                    onCancel = {
                        isLogin = true
                    }
                )
            }
        }

        needsProfileSetup == null -> {
            // Loading or checking state
            androidx.compose.material3.CircularProgressIndicator()
        }

        needsProfileSetup == true -> {
            InitialProfile(
                navController = navController,
                onFinish = {
                    needsProfileSetup = false
                },
                onBackToLogin = {
                    FirebaseAuth.getInstance().signOut()
                    currentUser = null
                    isLogin = true
                }
            )
        }

        else -> {
            if (showReminderSettings) {
                ReminderSettings(
                    onBack = { showReminderSettings = false }
                )
            } else {
                Profile(
                    navController = navController,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        currentUser = null
                        isLogin = true
                    },
                    onOpenReminder = {
                        showReminderSettings = true
                    }
                )
            }
        }

    }
}