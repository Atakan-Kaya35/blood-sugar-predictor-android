package com.ataka.bspapp.ui

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*

@Composable
fun PredictionChart(values: List<Pair<Float, Float>>, indicators: Int) {
    val context = LocalContext.current

    val realCount = 9
    val realEntries = values.take(realCount).mapIndexed { i, v ->
        Entry(i.toFloat(), v.first)
    }
    val predEntries = values.drop(realCount).mapIndexed { i, v ->
        Entry((realCount + i * 2).toFloat(), v.first)
    }

    val realDataSet = LineDataSet(realEntries, "Real").apply {
        color = Color.BLUE
        circleRadius = 3f
        setCircleColor(Color.BLUE)
        lineWidth = 2f
        setDrawValues(false)
    }

    val predDataSet = LineDataSet(predEntries, "Predicted").apply {
        color = Color.RED
        circleRadius = 3f
        setCircleColor(Color.RED)
        lineWidth = 2f
        setDrawValues(false)
    }

    val lineData = LineData(realDataSet, predDataSet)

    // 📦 Layout box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AndroidView(
            factory = {
                LineChart(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    data = lineData

                    setTouchEnabled(true)
                    setPinchZoom(true)
                    description.isEnabled = false
                    setDrawGridBackground(false)
                    animateX(1000)

                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.setDrawGridLines(false)
                    axisLeft.setDrawGridLines(true)
                    axisRight.isEnabled = false
                    axisLeft.axisMinimum = 40f
                    axisLeft.axisMaximum = 280f
                    legend.isEnabled = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight() // ✅ ensures Compose reserves full vertical space
                    ,
            update = {
                it.data = lineData
                it.invalidate()
            }
        )
    }
}
