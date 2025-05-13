package com.example.ass3

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Intake(navController: NavHostController) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Dropdown options
    val mealOptions = listOf("Breakfast", "Lunch", "Dinner")
    val stapleOptions = listOf("Rice", "Bread", "Noodles")
    val meatOptions = listOf("Chicken", "Beef", "Pork", "Fish")
    val vegetableOptions = listOf("Carrot", "Spinach", "Broccoli")
    val otherOptions = listOf("Egg", "Fruit", "Soup", "Yogurt")

    // User selections
    var mealTime by remember { mutableStateOf(mealOptions.first()) }
    var staple by remember { mutableStateOf("") }
    var meat by remember { mutableStateOf("") }
    var vegetable by remember { mutableStateOf("") }
    var other by remember { mutableStateOf("") }

    val recordList = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        DateSelector(selectedDate = selectedDate) {
            selectedDate = it
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown Menus
        DropdownField("Meal Type", mealOptions, mealTime) { mealTime = it }
        DropdownField("Staple Food", stapleOptions, staple) { staple = it }
        DropdownField("Meat", meatOptions, meat) { meat = it }
        DropdownField("Vegetables", vegetableOptions, vegetable) { vegetable = it }
        DropdownField("Others", otherOptions, other) { other = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Button
        Button(
            onClick = {
                val record =
                    "${selectedDate.format(formatter)} [$mealTime] Staple: $staple, Meat: $meat, Vegetables: $vegetable, Others: $other"
                recordList.add(record)
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFADD8E6)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))
        // --- Section Title ---
        Text("Food Records:", fontSize = 18.sp, color = Color.Black)

// --- Group records by date ---
        val groupedRecords = recordList.groupBy { record ->
            record.substringBefore(" ") // 获取日期作为分组键
        }

// --- Card container ---
        Card(
            modifier = Modifier
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
                            text = date,
                            fontSize = 16.sp,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(records) { record ->
                        Text(
                            text = "• " + record.substringAfter(" "),
                            fontSize = 15.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
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