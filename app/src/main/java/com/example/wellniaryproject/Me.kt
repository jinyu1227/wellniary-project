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

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val uid = currentUser!!.uid
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "my_app_db").build()
            val dao = db.userProfileDao()
            val profile = dao.getProfileByUid(uid)
            needsProfileSetup = profile == null
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
            Profile(
                navController = navController,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    currentUser = null
                    isLogin = true
                }
            )
        }
    }
}