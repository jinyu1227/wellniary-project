package com.example.wellniaryproject

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PieChart(data: Map<String, Int>) {
    val colors = listOf(Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF2196F3), Color(0xFFE91E63))
    val total = data.values.sum().toFloat()
    if (total == 0f) {
        Text("No food records this week", color = Color.Gray)
        return
    }
    var startAngle = 0f

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
        .padding(16.dp)) {
        data.entries.forEachIndexed { index, entry ->
            val sweep = 360f * (entry.value / total)
            if (entry.value == 0) return@forEachIndexed
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }

    // 图例
    Column(modifier = Modifier.padding(top = 8.dp)) {
        data.entries.forEachIndexed { index, entry ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                Box(modifier = Modifier.size(16.dp).background(colors[index % colors.size]))
                Spacer(Modifier.width(8.dp))
                Text("${entry.key.capitalize()}: ${entry.value}")
            }
        }
    }
}
