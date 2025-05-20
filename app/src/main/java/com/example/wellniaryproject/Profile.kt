package com.example.wellniaryproject

import android.app.DatePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.wellniaryproject.AppDatabase
import com.example.wellniaryproject.UserProfile
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavHostController,
    onLogout: () -> Unit,
    onOpenReminder: () -> Unit
) {
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val uid = firebaseUser?.uid ?: ""
    val email = firebaseUser?.email ?: ""
    val context = LocalContext.current

    val db = remember {
        Room.databaseBuilder(context, AppDatabase::class.java, "my_app_db").build()
    }
    val dao = db.userProfileDao()

    var displayedUsername by remember { mutableStateOf(email.substringBefore("@")) }
    var editedUsername by remember { mutableStateOf(displayedUsername) }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf(false) }
    var birthdayError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var stateError by remember { mutableStateOf(false) }
    var heightError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    var isEditing by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

    val bmi = calculateBMI(height.toFloatOrNull(), weight.toFloatOrNull())

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            birthday = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
            birthdayError = false
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    LaunchedEffect(uid) {
        val local = dao.getProfileByUid(uid)
        if (local != null) {
            displayedUsername = local.username
            editedUsername = local.username
            birthday = local.birthday
            gender = local.gender
            state = local.state
            height = local.height
            weight = local.weight
        } else {
            // ❗ 如果本地为空，就拉 Firebase 同步
            val firebaseRef = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            val snapshot = firebaseRef.get().await()
            if (snapshot.exists()) {
                val email = snapshot.child("email").getValue(String::class.java) ?: return@LaunchedEffect
                val username = snapshot.child("username").getValue(String::class.java) ?: return@LaunchedEffect
                val birthdayValue = snapshot.child("birthday").getValue(String::class.java) ?: return@LaunchedEffect
                val genderValue = snapshot.child("gender").getValue(String::class.java) ?: return@LaunchedEffect
                val stateValue = snapshot.child("state").getValue(String::class.java) ?: ""
                val heightValue = snapshot.child("height").getValue(String::class.java) ?: return@LaunchedEffect
                val weightValue = snapshot.child("weight").getValue(String::class.java) ?: return@LaunchedEffect

                val profile = UserProfile(uid, email, username, birthdayValue, genderValue, stateValue, heightValue, weightValue)

                // 存入 Room
                dao.insertProfile(profile)

                // 显示到 UI
                displayedUsername = username
                editedUsername = username
                birthday = birthdayValue
                gender = genderValue
                state = stateValue
                height = heightValue
                weight = weightValue
            }
        }
    }



    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(Color(0xFFFAFAFA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(240.dp).statusBarsPadding(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Transparent,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(colors = listOf(Color(0xFFADD8E6), Color(0xFFBBDEFB)))
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(shape = CircleShape, modifier = Modifier.size(80.dp), color = Color.White) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_placeholder),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(displayedUsername, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                    Text("BMI: ${bmi?.toString() ?: "--"}", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(title = "Personal Info", showEdit = !isEditing, onEditClick = { isEditing = true }) {
            if (isEditing) {
                Text("* Required for accurate health analysis.", style = TextStyle(fontSize = 12.sp, color = Color.Gray), modifier = Modifier.padding(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {},
                enabled = false,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = editedUsername,
                onValueChange = {
                    editedUsername = it
                    usernameError = it.isBlank() || it.length > 20
                },
                enabled = isEditing,
                isError = usernameError,
                label = { Text("Username *") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                singleLine = true
            )
            if (usernameError) {
                Text(
                    text = if (editedUsername.isBlank()) "Username is required." else "Username too long (max 20 characters).",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (usernameError) Text("Username is required.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))

            OutlinedTextField(
                value = birthday,
                onValueChange = {},
                enabled = false,
                readOnly = true,
                label = { Text(if (isEditing) "Birthday *" else "Birthday", color = if (isEditing) Color.Black else Color.Gray) },
                isError = birthdayError,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(enabled = isEditing) { datePickerDialog.show() },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isEditing) Color.Black else Color.LightGray,
                    unfocusedBorderColor = if (isEditing) Color.Black else Color.LightGray,
                    disabledBorderColor = if (isEditing) Color.Black else Color.LightGray,
                    disabledTextColor = if (isEditing) Color.Black else Color.Gray,
                    disabledLabelColor = if (isEditing) Color.Black else Color.Gray
                )
            )
            if (birthdayError) Text("Birthday is required.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))

            if (isEditing) {
                ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        label = { Text("Gender *") },
                        readOnly = true,
                        isError = genderError,
                        enabled = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                        trailingIcon = {
                            Icon(if (genderExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                        }
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        listOf("Female", "Male", "Non-binary", "Prefer not to say", "Other").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                gender = it
                                genderExpanded = false
                                genderError = false
                            })
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Gender") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    singleLine = true
                )
            }
            if (genderError) Text("Gender is required.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))

            OutlinedTextField(
                value = state,
                onValueChange = {
                    state = it
                    stateError = it.length > 20
                },
                enabled = isEditing,
                label = { Text("State") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                singleLine = true
            )

            if (stateError) {
                Text(
                    text = "State too long (max 20 characters).",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    usernameError = editedUsername.isBlank() || editedUsername.length > 20
                    birthdayError = birthday.isBlank()
                    genderError = gender.isBlank()
                    genderError = gender.isBlank()
                    stateError = state.length > 20
                    heightError = height.toFloatOrNull() == null || height.toFloatOrNull()!! !in 50f..300f
                    weightError = weight.toFloatOrNull() == null || weight.toFloatOrNull()!! !in 10f..500f

                    if (!usernameError && !birthdayError && !genderError && !stateError && !heightError && !weightError) {
                        val userProfile = UserProfile(
                            uid = uid,
                            email = email,
                            username = editedUsername,
                            birthday = birthday,
                            gender = gender,
                            state = state,
                            height = height,
                            weight = weight
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            // 1. 保存到本地 Room
                            dao.insertProfile(userProfile)

                            // 2. 同步到 Firebase Realtime Database
                            FirebaseDatabase.getInstance()
                                .reference
                                .child("users")
                                .child(uid)
                                .setValue(userProfile)

                            val db = Room.databaseBuilder(context, AppDatabase::class.java, "my_app_db").build()
                            db.userProfileDao().insertProfile(userProfile)
                        }

                        displayedUsername = editedUsername
                        isEditing = false
                    }
                }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))) {
                    Text("Save Profile", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatCard(title = "Health Info") {
            EditableStatBox(height, "Height (cm)", editable = !isEditing) { showHeightDialog = true }
            EditableStatBox(weight, "Weight (kg)", editable = !isEditing) { showWeightDialog = true }
        }

        if (heightError) Text("Please enter a legal number in the range of 50 - 300 cm.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
        if (weightError) Text("Please enter a legal number in the range of 30 - 150 kg.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onOpenReminder() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
        ) {
            Text("Health Reminder Settings", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("achievement_detail") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)) // 可更改颜色
        ) {
            Text("View Achievements", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onLogout() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Log out", fontSize = 16.sp, color = Color.White)
        }
    }

    if (showHeightDialog) {
        InputDialog("Edit Height (cm)", height, onDismiss = { showHeightDialog = false }, onConfirm = { newValue ->
            height = newValue
            showHeightDialog = false

            val updatedProfile = UserProfile(
                uid = uid,
                email = email,
                username = editedUsername,
                birthday = birthday,
                gender = gender,
                state = state,
                height = newValue,
                weight = weight
            )

            CoroutineScope(Dispatchers.IO).launch {
                dao.insertProfile(updatedProfile)
                FirebaseDatabase.getInstance()
                    .reference
                    .child("users")
                    .child(uid)
                    .setValue(updatedProfile)
            }
        })
    }

    if (showWeightDialog) {
        InputDialog("Edit Weight (kg)", weight, onDismiss = { showWeightDialog = false }, onConfirm = { newValue ->
            weight = newValue
            showWeightDialog = false

            val updatedProfile = UserProfile(
                uid = uid,
                email = email,
                username = editedUsername,
                birthday = birthday,
                gender = gender,
                state = state,
                height = height,
                weight = newValue
            )

            CoroutineScope(Dispatchers.IO).launch {
                dao.insertProfile(updatedProfile)
                FirebaseDatabase.getInstance()
                    .reference
                    .child("users")
                    .child(uid)
                    .setValue(updatedProfile)
            }
        })
    }

}

@Composable
fun EditableStatBox(value: String, label: String, editable: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = if (editable) Modifier.clickable { onClick() } else Modifier
        )
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun InputDialog(
    title: String,
    value: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var input by remember { mutableStateOf(value) }
    var isError by remember { mutableStateOf(false) }

    val errorMessage = when (title) {
        "Edit Height (cm)" -> "Height must be between 50 and 300 cm."
        "Edit Weight (kg)" -> "Weight must be between 10 and 500 kg."
        else -> "Invalid input"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        isError = false
                    },
                    isError = isError,
                    label = { Text("Value") },
                    singleLine = true
                )
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val number = input.toFloatOrNull()
                isError = when (title) {
                    "Edit Height (cm)" -> number == null || number !in 50f..300f
                    "Edit Weight (kg)" -> number == null || number !in 10f..500f
                    else -> true
                }

                if (!isError) {
                    onConfirm(input)
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


fun calculateBMI(heightCm: Float?, weightKg: Float?): Float? {
    if (heightCm == null || heightCm <= 0f || weightKg == null) return null
    val heightM = heightCm / 100f
    return (weightKg / (heightM * heightM)).toBigDecimal().setScale(1, java.math.RoundingMode.HALF_UP).toFloat()
}

@Composable
fun InfoCard(title: String, showEdit: Boolean = false, onEditClick: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0)), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A237E))
                if (showEdit) {
                    TextButton(onClick = onEditClick) {
                        Text("Edit", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun StatCard(title: String, content: @Composable RowScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE0E0E0)), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, content = content)
        }
    }
}

@Composable
fun StatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}
