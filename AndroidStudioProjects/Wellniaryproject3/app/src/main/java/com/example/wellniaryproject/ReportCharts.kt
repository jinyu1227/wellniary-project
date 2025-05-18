package com.example.wellniaryproject

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun BarChartScreen(modifier: Modifier, stepsData: List<BarEntry>, labels: List<String>) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                val dataSet = BarDataSet(stepsData, "Water Intake").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextSize = 12f
                }
                val data = BarData(dataSet)
                this.data = data

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    isGranularityEnabled = true
                }

                axisLeft.isEnabled = true
                axisRight.isEnabled = false
                description.isEnabled = false

                data.notifyDataChanged()
                notifyDataSetChanged()
                invalidate()
                animateY(1000)
            }
        },
        update = { chart ->
            val dataSet = BarDataSet(stepsData, "Water Intake").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
            }
            chart.data = BarData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
fun LineChartScreen(modifier: Modifier, entries: List<Entry>, labels: List<String>) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                val dataSet = LineDataSet(entries, "Weight").apply {
                    color = ColorTemplate.COLORFUL_COLORS[0]
                    valueTextSize = 12f
                    setDrawCircles(true)
                    setDrawValues(true)
                    lineWidth = 2f
                    circleRadius = 4f
                }
                val data = LineData(dataSet)
                this.data = data

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(labels)
                    granularity = 1f
                    isGranularityEnabled = true
                }

                axisLeft.isEnabled = true
                axisRight.isEnabled = false
                description.isEnabled = false

                data.notifyDataChanged()
                notifyDataSetChanged()
                invalidate()
                animateX(1000)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Weight").apply {
                color = ColorTemplate.COLORFUL_COLORS[0]
                valueTextSize = 12f
                setDrawCircles(true)
                setDrawValues(true)
                lineWidth = 2f
                circleRadius = 4f
            }
            chart.data = LineData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

@Composable
fun PieChartScreen(modifier: Modifier, pieData: Map<String, Float>) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PieChart(context).apply {
                val entries = pieData.map { PieEntry(it.value, it.key) }
                val dataSet = PieDataSet(entries, "Diet Composition").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    valueTextSize = 14f
                    sliceSpace = 2f
                }
                this.data = PieData(dataSet)

                description.isEnabled = false
                isDrawHoleEnabled = true
                centerText = "Diet"
                animateY(1000)
                invalidate()
            }
        },
        update = { chart ->
            val entries = pieData.map { PieEntry(it.value, it.key) }
            val dataSet = PieDataSet(entries, "Diet Composition").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 14f
                sliceSpace = 2f
            }
            chart.data = PieData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}
