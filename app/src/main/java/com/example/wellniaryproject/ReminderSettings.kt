package com.example.wellniaryproject

import android.R.color.white
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("ResourceAsColor")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettings(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val reminderDao = db.reminderDao()

    val drinkWater = remember { mutableStateOf(false) }
    val logDiet = remember { mutableStateOf(false) }
    val logWeight = remember { mutableStateOf(false) }
    val checkReport = remember { mutableStateOf(false) }

    var selectedDay by remember { mutableStateOf("Monday") }
    var selectedTime by remember { mutableStateOf("09:00") }

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = remember {
        TimePickerDialog(context, { _, h, m ->
            selectedTime = String.format("%02d:%02d", h, m)
        }, hour, minute, true)
    }


    // 初始化状态从 Room 读取
    LaunchedEffect(Unit) {
        val settings = reminderDao.getAll()
        settings.forEach {
            when (it.label) {
                "Drink Water" -> drinkWater.value = it.enabled
                "Log Diet" -> logDiet.value = it.enabled
                "Log Weight" -> logWeight.value = it.enabled
                "Check Report" -> checkReport.value = it.enabled
            }
            selectedDay = it.day
            selectedTime = it.time
        }
    }


    val reminderItems = listOf(
        "Drink Water" to drinkWater,
        "Log Diet" to logDiet,
        "Log Weight" to logWeight,
        "Check Report" to checkReport
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(white))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFBBDEFB))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Reminder Settings",
                fontSize = 20.sp,
                color = Color.Black
            )
        }

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Reminder Items",
                        fontSize = 16.sp,
                        color = Color(0xFF1A237E),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(reminderItems) { (label, state) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.value,
                            onCheckedChange = { state.value = it }
                        )
                        Text(label, fontSize = 16.sp)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Weekly Time Setting",
                        fontSize = 16.sp,
                        color = Color(0xFF1A237E),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    ReminderDropdown(
                        title = "Select Day",
                        options = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                        selected = selectedDay,
                        onSelect = { selectedDay = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    TextField(
                        value = selectedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Time") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { timePickerDialog.show() }
                    )


                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = {
                            val dayMap = mapOf(
                                "Monday" to Calendar.MONDAY,
                                "Tuesday" to Calendar.TUESDAY,
                                "Wednesday" to Calendar.WEDNESDAY,
                                "Thursday" to Calendar.THURSDAY,
                                "Friday" to Calendar.FRIDAY,
                                "Saturday" to Calendar.SATURDAY,
                                "Sunday" to Calendar.SUNDAY
                            )
                            val dayOfWeek = dayMap[selectedDay] ?: Calendar.MONDAY
                            val hour = selectedTime.split(":")[0].toInt()
                            val minute = selectedTime.split(":")[1].toInt()

                            val reminderList = listOf(
                                Triple("Drink Water", drinkWater.value, "💧 Time to drink water!"),
                                Triple("Log Diet", logDiet.value, "🍽️ Time to log your diet!"),
                                Triple("Log Weight", logWeight.value, "⚖️ Time to log your weight!"),
                                Triple("Check Report", checkReport.value, "📊 Check your health report")
                            )

                            val hasChecked = reminderList.any { it.second } // ✅ 是否有选中的提醒项
                            if (!hasChecked) {
                                Toast.makeText(context, "Please select at least one reminder", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            scope.launch {
                                reminderDao.clearAll()
                                reminderList.forEach { (label, isChecked, title) ->
                                    reminderDao.insert(
                                        ReminderSetting(
                                            label = label,
                                            enabled = isChecked,
                                            day = selectedDay,
                                            time = selectedTime
                                        )
                                    )
                                    if (isChecked) {
                                        scheduleWeeklyReminder(
                                            context = context,
                                            dayOfWeek = dayOfWeek,
                                            hour = hour,
                                            minute = minute,
                                            title = title,
                                            message = "Reminder for $label"
                                        )
                                    }
                                }

                                Toast.makeText(context, "Reminders saved", Toast.LENGTH_SHORT).show()
                            }

                        }
                        ,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                    ) {
                        Text("Save Reminder", color = Color.White)
                    }

//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Button(
//                        onClick = {
//                            val intent = Intent(context, ReminderReceiver::class.java).apply {
//                                putExtra("title", "🔔 Test Reminder")
//                                putExtra("message", "This is a test notification from AlarmManager.")
//                            }
//                            context.sendBroadcast(intent)
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(20.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
//                    ) {
//                        Text("Test Reminder", color = Color.White)
//                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDropdown(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(title, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            TextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White
                )
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { label ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onSelect(label)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun scheduleWeeklyReminder(
    context: Context,
    dayOfWeek: Int,
    hour: Int,
    minute: Int,
    title: String,
    message: String
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, dayOfWeek)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(Calendar.getInstance())) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("message", message)
    }

    val requestCode = (dayOfWeek * 10000 + hour * 100 + minute) + title.hashCode()
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY * 7,
        pendingIntent
    )
}
