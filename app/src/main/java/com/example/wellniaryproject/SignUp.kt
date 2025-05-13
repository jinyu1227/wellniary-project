package com.example.wellniaryproject

import android.util.Patterns
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Signup(
    onRegisterSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var showErrors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register for Wellniary", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            isError = showErrors && emailError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (showErrors && emailError != null) {
            Text(emailError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = showErrors && passwordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (showErrors && passwordError != null) {
            Text(passwordError!!, color = Color.Red, fontSize = 12.sp)
        }

        Text(
            "Password must be ≥8 characters and contain a capital letter.",
            style = TextStyle(fontSize = 12.sp, color = Color.Gray, textDecoration = TextDecoration.Underline),
            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = showErrors && confirmPasswordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (showErrors && confirmPasswordError != null) {
            Text(confirmPasswordError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    showErrors = true

                    emailError = when {
                        email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
                        else -> null
                    }

                    passwordError = when {
                        password.length < 8 || password.none { it.isUpperCase() } ->
                            "Password must be ≥8 and contain a capital letter"
                        else -> null
                    }

                    confirmPasswordError = if (confirmPassword != password) {
                        "Passwords do not match"
                    } else null

                    if (emailError == null && passwordError == null && confirmPasswordError == null) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                firebaseError = null
                                onRegisterSuccess() // ✅ 切回登录页
                            }
                            .addOnFailureListener {
                                Log.e("Signup", "Firebase error: ${it.localizedMessage}", it)
                                firebaseError = it.localizedMessage ?: "Signup failed"
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADD8E6)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Register")
            }

            OutlinedButton(
                onClick = { onCancel() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }

        if (firebaseError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(firebaseError!!, color = Color.Red, fontSize = 12.sp)
        }
    }
}
