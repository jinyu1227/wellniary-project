package com.example.wellniaryproject

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Report(navController: NavHostController) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // ✅ 当前选中日期
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val weekStart = selectedDate.with(DayOfWeek.MONDAY)
    val weekEnd = selectedDate.with(DayOfWeek.SUNDAY)

    // ✅ 从 ViewModel 拿 Room 数据
    val viewModel = viewModel<DietLogViewModel>(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )

    val allLogs by viewModel.getLogsForUser(uid).collectAsState(initial = emptyList())

    // ✅ 本周内的记录
    val weeklyLogs = remember(allLogs, selectedDate) {
        allLogs.filter {
            try {
                val date = LocalDate.parse(it.date.trim(), formatter)
                date in weekStart..weekEnd
            } catch (e: Exception) {
                false
            }
        }
    }

    // ✅ 饼图统计
    val categoryCount = remember(weeklyLogs) {
        val countMap = mutableMapOf(
            "staple" to 0,
            "meat" to 0,
            "vegetable" to 0,
            "other" to 0
        )

        weeklyLogs.forEach { log ->
            if (log.staple.isNotBlank()) countMap["staple"] = countMap["staple"]!! + 1
            if (log.meat.isNotBlank()) countMap["meat"] = countMap["meat"]!! + 1
            if (log.vegetable.isNotBlank()) countMap["vegetable"] = countMap["vegetable"]!! + 1
            if (log.other.isNotBlank()) countMap["other"] = countMap["other"]!! + 1
        }

        countMap
    }

    // ✅ UI 展示
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        DateSelector(selectedDate = selectedDate) { selectedDate = it }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Week: $weekStart - $weekEnd",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        if (categoryCount.values.sum() == 0) {
            Spacer(Modifier.height(24.dp))
            Text("No food records this week.", color = Color.Gray)
        } else {
            Spacer(Modifier.height(24.dp))
            PieChart(categoryCount)
        }
    }
}
