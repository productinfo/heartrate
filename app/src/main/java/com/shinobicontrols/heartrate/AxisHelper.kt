package com.shinobicontrols.heartrate

import android.content.res.Resources
import com.shinobicontrols.charts.Axis
import com.shinobicontrols.charts.DateTimeAxis
import com.shinobicontrols.charts.NumberAxis
import com.shinobicontrols.charts.NumberRange

enum class YAxisType {
    Y, REVERSE_Y
}

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

private fun createYAxis(): NumberAxis {
    return NumberAxis(NumberRange(40.0, 165.0))
}

private fun createReverseYAxis(resources: Resources): NumberAxis {
    return NumberAxis(NumberRange(-35.0, -3.0)).apply {
        position = Axis.Position.REVERSE
        width = 50f * resources.displayMetrics.scaledDensity
    }
}