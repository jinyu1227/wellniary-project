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

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var firebaseError by remember { mutableStateOf<String?>(null) }

    fun validateUsername(input: String): String? =
        if (input.length > 20) "Username must be ≤ 20 characters" else null

    fun validateEmail(input: String): String? =
        if (input.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(input).matches()) "Invalid email" else null

    fun validatePassword(input: String): String? =
        if (input.length < 8 || input.none { it.isUpperCase() }) "Password must be ≥8 and contain a capital letter" else null

    fun validateConfirmPassword(confirm: String, password: String): String? =
        if (confirm != password) "Passwords do not match" else null

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
            onValueChange = {
                username = it
                usernameError = validateUsername(it)
            },
            label = { Text("Username") },
            isError = usernameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (usernameError != null) {
            Text(usernameError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = validateEmail(it)
            },
            label = { Text("Email Address") },
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError != null) {
            Text(emailError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = validatePassword(it)
                confirmPasswordError = validateConfirmPassword(confirmPassword, it)
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError != null) {
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
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = validateConfirmPassword(it, password)
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = confirmPasswordError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (confirmPasswordError != null) {
            Text(confirmPasswordError!!, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    usernameError = validateUsername(username)
                    emailError = validateEmail(email)
                    passwordError = validatePassword(password)
                    confirmPasswordError = validateConfirmPassword(confirmPassword, password)

                    if (usernameError == null && emailError == null && passwordError == null && confirmPasswordError == null) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                firebaseError = null
                                onRegisterSuccess()
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


