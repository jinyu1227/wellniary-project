package com.example.wellniaryproject

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.wellniaryproject.viewmodel.ReportViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Report(navController: NavHostController) {
    val viewModel: ReportViewModel = viewModel()
    val data by viewModel.weeklyData.collectAsState()
    val pieData by viewModel.dietCategoryRatio.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("MM-dd")

    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val weekStart = selectedDate.with(DayOfWeek.MONDAY)
    val weekEnd = selectedDate.with(DayOfWeek.SUNDAY)

    val dates = remember(weekStart) {
        (0..6).map { weekStart.plusDays(it.toLong()).toString() }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DateSelector(selectedDate = selectedDate) { selectedDate = it }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Week: $weekStart - $weekEnd", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))
        Text("Water Intake Past 7 Days", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        val waterMap = data.associateBy { it.date }
        val maxWater = (data.maxOfOrNull { it.waterCount } ?: 1).coerceAtLeast(1)
        val waterStep = (maxWater / 4).coerceAtLeast(1)

        Row(modifier = Modifier.height(200.dp)) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxHeight()
            ) {
                for (i in 0..4) {
                    val value = maxWater - i * waterStep
                    Text(
                        text = "$value",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.height(150.dp / 4)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dates.forEach { dateStr ->
                    val record = waterMap[dateStr]
                    val barHeight = ((record?.waterCount ?: 0) / maxWater.toFloat()) * 150

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${record?.waterCount ?: 0}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(barHeight.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = LocalDate.parse(dateStr).format(formatter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Weight Past 7 Days", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        val weightMap = data.associateBy { it.date }
        val weightPoints = dates.map { day -> weightMap[day]?.weight ?: 0f }

        val hasWeight = weightPoints.any { it > 0f }
        if (!hasWeight) {
            Text("No weight data available", style = MaterialTheme.typography.bodyMedium)
        } else {
            val maxWeight = weightPoints.maxOrNull() ?: 1f
            val minWeight = weightPoints.filter { it > 0f }.minOrNull() ?: 0f
            val range = (maxWeight - minWeight).takeIf { it > 0f } ?: 1f
            val weightStep = (range / 4).takeIf { it > 0f } ?: 1f

            Column {
                Row(modifier = Modifier.height(180.dp)) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .fillMaxHeight()
                    ) {
                        for (i in 4 downTo 0) {
                            val label = minWeight + weightStep * i
                            Text(
                                text = "%.1f".format(label),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.End,
                                modifier = Modifier.height(150.dp / 4)
                            )
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        val xStep = size.width / 6f
                        weightPoints.forEachIndexed { i, weight ->
                            val x = xStep * i
                            val y = size.height - ((weight - minWeight) / range) * size.height

                            if (weight > 0f) {
                                drawCircle(
                                    color = Color(0xFF4A90E2),
                                    radius = 6f,
                                    center = Offset(x, y)
                                )

                                drawContext.canvas.nativeCanvas.apply {
                                    drawText(
                                        "%.1f".format(weight),
                                        x,
                                        y - 12,
                                        android.graphics.Paint().apply {
                                            color = android.graphics.Color.BLACK
                                            textSize = 28f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                    )
                                }
                            }

                            if (i > 0 && weightPoints[i - 1] > 0f && weight > 0f) {
                                val prev = weightPoints[i - 1]
                                val px = xStep * (i - 1)
                                val py = size.height - ((prev - minWeight) / range) * size.height

                                drawLine(
                                    color = Color(0xFF4A90E2),
                                    start = Offset(px, py),
                                    end = Offset(x, y),
                                    strokeWidth = 4f
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dates.forEach { date ->
                        Text(
                            text = LocalDate.parse(date).format(formatter),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Diet Composition (Past 7 Days)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        DietPieChart(pieData)
    }
}

@Composable
fun DietPieChart(data: ReportViewModel.MealCategoryCount) {
    val values = listOf(data.staple, data.meat, data.vegetable, data.other)
    val colors = listOf(Color(0xFF90CAF9), Color(0xFFFFA726), Color(0xFF66BB6A), Color(0xFFCE93D8))
    val labels = listOf("Staple", "Meat", "Vegetable", "Other")

    val total = values.sum()
    if (total == 0) {
        Text("No diet records found", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val angles = values.map { it * 360f / total }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val radius = size.minDimension / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            var startAngle = -90f

            for (i in angles.indices) {
                val sweep = angles[i]

                // 🟡 绘制扇形
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )

                // 🔵 计算中点角度 & 坐标
                val angleRad = Math.toRadians((startAngle + sweep / 2).toDouble())
                val labelX = centerX + radius / 2 * kotlin.math.cos(angleRad).toFloat()
                val labelY = centerY + radius / 2 * kotlin.math.sin(angleRad).toFloat()

                // 🔤 画百分比文字
                drawContext.canvas.nativeCanvas.drawText(
                    "${(values[i] * 100 / total)}%",
                    labelX,
                    labelY,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )

                startAngle += sweep
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 饼图图例
        labels.zip(values.zip(colors)).forEach { (label, pair) ->
            val (value, color) = pair
            val percent = (value * 100f / total).toInt()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            ) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$label: $value ($percent%)", fontSize = 12.sp)
            }
        }
    }
}

