package com.example.wellniaryproject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportScreen(navController: NavHostController) {
    // ⬇️ 默认选中上周一 ~ 上周日
    val today = LocalDate.now()
    val thisMonday = today.with(DayOfWeek.MONDAY)
    val thisSunday = today.with(DayOfWeek.SUNDAY)
    var startDate by remember { mutableStateOf(thisMonday) }
    var endDate by remember { mutableStateOf(thisSunday) }

    val fullFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val dayFormatter = DateTimeFormatter.ofPattern("MMM d")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DateSelector(startDate = startDate, endDate = endDate) { newStart, newEnd ->
            startDate = newStart
            endDate = newEnd
        }

        // 日期范围显示
        Text(
            text = "${startDate.format(fullFormatter)} - ${endDate.format(fullFormatter)}",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = endDate.format(dayFormatter),
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text("Avg ", fontSize = 16.sp, color = Color.Gray)
            Text("154.8", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(" catties", fontSize = 16.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        repeat(2) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.health_report),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(
    startDate: LocalDate,
    endDate: LocalDate,
    onWeekRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("MMM d")

    var showPicker by remember { mutableStateOf(false) }

    // ✅ 当前“本周”的起止
    val currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY)
    val currentWeekEnd = LocalDate.now().with(DayOfWeek.SUNDAY)

    // ✅ 判断是否是“本周”
    val isCurrentWeek =
        startDate == currentWeekStart && endDate == currentWeekEnd

    // ✅ 显示的文字：默认显示“本周”，否则显示日期范围
    val dateRangeText = if (isCurrentWeek) {
        "Current Week"
    } else {
        "${startDate.format(formatter)} – ${endDate.format(formatter)}"
    }

    Button(
        onClick = { showPicker = true },
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFDDEFF3)),
        shape = RoundedCornerShape(24.dp),
        elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "Select Date",
            tint = Color.Black
        )
        Spacer(Modifier.width(8.dp))
        Text(text = dateRangeText, color = Color.Black)
    }

    if (showPicker) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val pickedDate = LocalDate.of(year, month + 1, day)
                val weekStart = pickedDate.with(DayOfWeek.MONDAY)
                val weekEnd = pickedDate.with(DayOfWeek.SUNDAY)
                onWeekRangeSelected(weekStart, weekEnd)
                showPicker = false
            },
            startDate.year,
            startDate.monthValue - 1,
            startDate.dayOfMonth
        ).show()
    }
}



