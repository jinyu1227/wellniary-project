package com.example.wellniaryproject

import android.app.Activity
import android.util.Patterns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

@Composable
fun Login(
    navController: NavHostController,
    onSwitchToSignup: () -> Unit,
    onSuccessLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showErrors by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener
                    val email = user.email ?: ""

                    val userRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
                    userRef.get().addOnSuccessListener { snapshot ->
                        if (!snapshot.exists()) {
                            val profile = UserProfile(
                                uid = uid,
                                email = email,
                                username = email.substringBefore("@"),
                                birthday = "",
                                gender = "",
                                state = "",
                                height = "",
                                weight = ""
                            )
                            userRef.setValue(profile).addOnCompleteListener {
                                onSuccessLogin() // ✅ 等写完再跳
                            }
                        } else {
                            onSuccessLogin() // ✅ 已有数据，直接跳
                        }
                    }



                    onSuccessLogin()
                } else {
                    loginError = authResult.exception?.message ?: "Google login failed"
                }
            }
        } catch (e: ApiException) {
            loginError = "Google login error: ${e.message}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to Wellniary",
                fontSize = 24.sp,
                fontWeight = MaterialTheme.typography.titleLarge.fontWeight
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = when {
                        it.isBlank() -> "Username cannot be empty"
                        !Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Invalid email format"
                        else -> null
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && usernameError != null
            )
            if (showErrors && !usernameError.isNullOrBlank()) {
                Text(usernameError!!, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = when {
                        it.isBlank() -> "Password cannot be empty"
                        it.length < 8 || !it.any { c -> c.isUpperCase() } ->
                            "Password must be ≥8 and contain a capital letter"
                        else -> null
                    }
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = showErrors && passwordError != null
            )
            if (showErrors && !passwordError.isNullOrBlank()) {
                Text(passwordError!!, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Password must be ≥8 chars & include capital. Username must be a valid email.",
                fontSize = 12.sp,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    showErrors = true
                    if (usernameError == null && passwordError == null) {
                        auth.signInWithEmailAndPassword(username, password)
                            .addOnSuccessListener {
                                val user = auth.currentUser
                                if (user != null) {
                                    onSuccessLogin() // ✅ 只触发成功登录，不写入数据库
                                }
                            }
                            .addOnFailureListener {
                                Log.e("LoginDebug", "Login failed: ${it.localizedMessage}", it)
                                loginError = it.localizedMessage ?: "Login failed"
                            }

                            .addOnFailureListener {
                                Log.e("LoginDebug", "Login failed: ${it.localizedMessage}", it)
                                loginError = it.localizedMessage ?: "Login failed"
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Login")
            }

            if (loginError != null) {
                Text(loginError!!, color = Color.Red, fontSize = 12.sp)
            }

            TextButton(onClick = {
                onSwitchToSignup()  // ✅ 回调控制切换注册页
            }) {
                Text("If you have no account, please register")
            }

            Button(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("564369390556-q318lhh116hq5gdq91imj3vvqt1831nj.apps.googleusercontent.com")
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)

                    googleSignInClient.signOut().addOnCompleteListener {
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "google_logo",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    tint = Color.Unspecified
                )
                Text("Login with Google")
            }

        }
    }
}
