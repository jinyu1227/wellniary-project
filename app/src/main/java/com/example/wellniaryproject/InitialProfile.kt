package com.example.wellniaryproject

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialProfile(
    navController: NavHostController,
    onFinish: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid ?: ""
    val email = user?.email ?: ""
    val context = LocalContext.current

    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "my_app_db").build()
    }
    val dao = db.userProfileDao()

    var username by remember { mutableStateOf(email.substringBefore("@")) }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf(false) }
    var birthdayError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var heightError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            birthday = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            birthdayError = false
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Complete Your Profile", fontSize = 24.sp, modifier = Modifier.padding(vertical = 16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {},
            enabled = false,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                usernameError = it.isBlank()
            },
            isError = usernameError,
            label = { Text("Username *") },
            modifier = Modifier.fillMaxWidth()
        )
        if (usernameError) Text("Username is required", color = Color.Red, fontSize = 12.sp)

        OutlinedTextField(
            value = birthday,
            onValueChange = {},
            readOnly = true,
            label = { Text("Birthday *") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            isError = birthdayError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Black,
                disabledLabelColor = Color.DarkGray
            )
        )
        if (birthdayError) Text("Birthday is required", color = Color.Red, fontSize = 12.sp)

        GenderDropdown(gender) {
            gender = it
            genderError = false
        }
        if (genderError) Text("Gender is required", color = Color.Red, fontSize = 12.sp)

        OutlinedTextField(
            value = height,
            onValueChange = {
                height = it
                heightError = it.toFloatOrNull() == null || it.toFloat() !in 50f..300f
            },
            label = { Text("Height (cm) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = heightError
        )
        if (heightError) Text("Height must be between 50 and 300", color = Color.Red, fontSize = 12.sp)

        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                weightError = it.toFloatOrNull() == null || it.toFloat() !in 10f..500f
            },
            label = { Text("Weight (kg) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = weightError
        )
        if (weightError) Text("Weight must be between 10 and 500", color = Color.Red, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { onBackToLogin() }) {
                Text("Back")
            }
            Button(onClick = {
                usernameError = username.isBlank()
                birthdayError = birthday.isBlank()
                genderError = gender.isBlank()
                heightError = height.toFloatOrNull() == null || height.toFloat() !in 50f..300f
                weightError = weight.toFloatOrNull() == null || weight.toFloat() !in 10f..500f

                if (!usernameError && !birthdayError && !genderError && !heightError && !weightError) {
                    val profile = UserProfile(
                        uid = uid,
                        email = email,
                        username = username,
                        birthday = birthday,
                        gender = gender,
                        state = state,
                        height = height,
                        weight = weight
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insertProfile(profile)
                        FirebaseDatabase.getInstance().reference.child("users").child(uid).setValue(profile)
                    }
                    onFinish()
                }
            }) {
                Text("Next")
            }
        }
    }
}

@Composable
fun GenderDropdown(selectedGender: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender *") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Female", "Male", "Non-binary", "Prefer not to say", "Other").forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}
