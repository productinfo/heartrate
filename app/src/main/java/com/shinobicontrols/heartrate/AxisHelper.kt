package com.shinobicontrols.heartrate

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.view.View
import com.shinobicontrols.charts.*
import java.text.DecimalFormat

enum class YAxisType {
    Y, REVERSE_Y
}

val decimalFormat = DecimalFormat("##")

fun getXAxis(): DateTimeAxis {
    return DateTimeAxis().apply {
        enableGesturePanning(true)
        enableGestureZooming(true)
    }
}

fun getYAxis(yAxisType: YAxisType,
             resources: Resources): NumberAxis {
    return when (yAxisType) {
        YAxisType.Y -> createYAxis()
        YAxisType.REVERSE_Y -> createReverseYAxis(resources)
    }
}

fun updateLabelPaint(labelPaint: TextPaint, resources: Resources) {
    with(labelPaint) {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        color = Color.BLACK
        textSize = 16f * resources.displayMetrics.scaledDensity
    }
}

fun convertLabelSign(value: Double): String {
    return decimalFormat.format(value * -1)
}

fun createLegendAndPaceTickmarkUpdater(shinobiChart: ShinobiChart):
        Axis.OnRangeChangeListener {
    return object : Axis.OnRangeChangeListener {
        override fun onRangeChange(axis: Axis<*, *>?) {
            updateLegendAndTicks()
            shinobiChart.redrawChart()
        }

        private fun updateLegendAndTicks() {
            val visible = seriesIsVisible(shinobiChart.series[1])
            val style = shinobiChart.getYAxisForSeries(shinobiChart.series[1]).getStyle()
            style.tickStyle.setMajorTicksShown(visible)
            style.tickStyle.setLabelsShown(visible)
            shinobiChart.series[1].isShownInLegend = visible
        }

        private fun seriesIsVisible(series: Series<*>): Boolean {
            return series.visibility == View.VISIBLE && series.alpha == 1.0f
        }
    }
}

private fun createYAxis(): NumberAxis {
    return NumberAxis(NumberRange(40.0, 165.0))
}

private fun createReverseYAxis(resources: Resources): NumberAxis {
    return NumberAxis(NumberRange(-35.0, -3.0)).apply {
        position = Axis.Position.REVERSE
        width = 50f * resources.displayMetrics.scaledDensity
        getStyle().tickStyle.setLabelsShown(false)
        getStyle().tickStyle.setMajorTicksShown(false)
    }
}