package com.example.wellniaryproject

import android.app.Application
import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DropdownMenuItem
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.OutlinedTextField
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import androidx.work.*
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Intake(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val viewModel = viewModel<DietLogViewModel>(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Dropdown options
    val mealOptions = listOf("Breakfast", "Lunch", "Dinner")
    val stapleOptions = listOf("Rice", "Bread", "Noodles")
    val meatOptions = listOf("Chicken", "Beef", "Pork", "Fish")
    val vegetableOptions = listOf("Carrot", "Spinach", "Broccoli")
    val otherOptions = listOf("Egg", "Fruit", "Soup", "Yogurt")

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // User selections
    var mealTime by remember { mutableStateOf(mealOptions.first()) }
    var staple by remember { mutableStateOf("") }
    var meat by remember { mutableStateOf("") }
    var vegetable by remember { mutableStateOf("") }
    var other by remember { mutableStateOf("") }

    var nutritionInfo by remember { mutableStateOf("") }

    val userLogs by viewModel.getLogsForUser(uid ?: "").collectAsState(initial = emptyList())
    val groupedRecords = userLogs.groupBy { it.date }


    LaunchedEffect(Unit) {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "diet_sync_daily",
            ExistingPeriodicWorkPolicy.KEEP, // 不重复注册
            syncRequest
        )
    }

    fun fetchNutritionInfo(foodName: String) {
        nutritionInfo = "Loading..."
        coroutineScope.launch {
            try {
                val response = RetrofitBuilder.apiService.getNutritionInfo(
                    NutritionRequest(query = foodName)
                )
                val food = response.foods.firstOrNull()
                nutritionInfo = if (food != null) {
                    "\uD83D\uDD25 ${food.nf_calories} kcal | \uD83E\uDD69 ${food.nf_protein}g protein | \uD83E\uDDC8 ${food.nf_total_fat}g fat"
                } else {
                    "No data found."
                }
            } catch (e: Exception) {
                nutritionInfo = "Error fetching nutrition data."
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        DateSelector(selectedDate = selectedDate) {
            selectedDate = it
        }

        DropdownField("Meal Type", mealOptions, mealTime) { mealTime = it }
        DropdownField("Staple Food", stapleOptions, staple) {
            staple = it
            fetchNutritionInfo(it)
        }

        DropdownField("Meat", meatOptions, meat) {
            meat = it
            fetchNutritionInfo(it)
        }

        DropdownField("Vegetables", vegetableOptions, vegetable) {
            vegetable = it
            fetchNutritionInfo(it)
        }
        DropdownField("Others", otherOptions, other) {
            other = it
            fetchNutritionInfo(it)
        }

        if (nutritionInfo.isNotBlank()) {
            Text(nutritionInfo, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
        }

//        Spacer(modifier = Modifier.height(16.dp))

//        Text(
//            text = "Logged in as: ${uid ?: "Guest"}",
//            fontSize = 14.sp,
//            color = Color.Gray,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
        // Confirm Button
        Button(
            onClick = {
                if (uid == null) {
                    Toast.makeText(context, "Please log in first.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (staple.isBlank() && meat.isBlank() && vegetable.isBlank() && other.isBlank()) {
                    Toast.makeText(context, "Please enter at least one food item.", Toast.LENGTH_SHORT).show()
                } else {
                    val log = DietLogEntity(
                        uid = uid,
                        date = selectedDate.format(formatter),
                        mealType = mealTime,
                        staple = staple,
                        meat = meat,
                        vegetable = vegetable,
                        other = other
                    )
                    viewModel.insertLog(log)

                    Toast.makeText(context, "Record saved successfully.", Toast.LENGTH_SHORT).show()

                    staple = ""
                    meat = ""
                    vegetable = ""
                    other = ""
                }
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFADD8E6)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm", color = Color.Black)
        }

//        Spacer(modifier = Modifier.height(5.dp))
//
//        Button(
//            onClick = {
//                val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
//                WorkManager.getInstance(context).enqueue(request)
//
//                Toast.makeText(context, "Sync task triggered", Toast.LENGTH_SHORT).show()
//            },
//            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF90CAF9)),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Test Upload Now", color = Color.White)
//        }
        Spacer(modifier = Modifier.height(10.dp))

        // --- Section Title ---
        Text("Diet Records:", fontSize = 18.sp, fontWeight = FontWeight.Bold,color = Color(0xFF222222),
            modifier = Modifier.padding(bottom = 5.dp))

// Grouped Card Container
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp)
                .defaultMinSize(minHeight = 80.dp),
            backgroundColor = Color(0xFFF0F8FF),
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
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

                    items(records) { record ->
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
                                        viewModel.deleteLog(record.id)
                                        Toast
                                            .makeText(context, "Record deleted", Toast.LENGTH_SHORT)
                                            .show()
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Please select") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF00BCD4),
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    var showPicker by remember { mutableStateOf(false) }

    // ⬇️ 今日按钮 + 图标
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { showPicker = true },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFDDEFF3) // 淡蓝背景
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = ButtonDefaults.elevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.DateRange,
                contentDescription = "Calendar",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (selectedDate == LocalDate.now()) "Today" else selectedDate.format(formatter),
                color = Color.Black
            )
        }
    }

    // ⬇️ 日期选择器弹窗
    if (showPicker) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                onDateChange(LocalDate.of(year, month + 1, day))
                showPicker = false
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }
}
