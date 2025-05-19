package com.example.wellniaryproject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Report(navController: NavHostController) {
    val viewModel: ReportViewModel = viewModel()
    val data by viewModel.weeklyData.collectAsState()
    val pieDataState by viewModel.dietCategoryRatio.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("EEE") // Change to weekday labels

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val weekStart = selectedDate.with(DayOfWeek.MONDAY)
    val weekEnd = selectedDate.with(DayOfWeek.SUNDAY)

    LaunchedEffect(weekStart) {
        viewModel.fetchWeekData(weekStart)
        viewModel.fetchDietDataForPieChart(weekEnd)
    }

    val dates = remember(weekStart) {
        (0..6).map { weekStart.plusDays(it.toLong()) }
    }
    val labels = dates.map { it.format(formatter) } // Now using weekday names

    val waterMap = data.associateBy { it.date }
    val barEntries = dates.mapIndexed { index, date ->
        val key = date.toString()
        BarEntry(index.toFloat(), waterMap[key]?.waterCount?.toFloat() ?: 0f)
    }
    val lineEntries = dates.mapIndexed { index, date ->
        val key = date.toString()
        Entry(index.toFloat(), waterMap[key]?.weight ?: 0f)
    }

    val pieData = mapOf(
        "Staple" to pieDataState.staple.toFloat(),
        "Meat" to pieDataState.meat.toFloat(),
        "Vegetable" to pieDataState.vegetable.toFloat(),
        "Other" to pieDataState.other.toFloat()
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DateSelector(selectedDate = selectedDate) { selectedDate = it }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Week: $weekStart - $weekEnd", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))
        Text("Water Intake (Past 7 Days)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        BarChartScreen(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            stepsData = barEntries,
            labels = labels
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text("Weight (Past 7 Days)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LineChartScreen(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            entries = lineEntries,
            labels = labels
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text("Diet Composition (Past 7 Days)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        PieChartScreen(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            pieData = pieData
        )
    }
}

@Composable
fun ReportDateSelector(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text = selectedDate.toString(),
            modifier = Modifier.padding(8.dp),
            fontSize = 18.sp
        )
    }
}