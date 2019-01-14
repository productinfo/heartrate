package com.shinobicontrols.heartrate

import android.content.Context
import android.support.v4.content.ContextCompat
import com.shinobicontrols.advancedcharting.styling.AdvancedLineSeriesStyle
import com.shinobicontrols.advancedcharting.styling.GradientStop
import com.shinobicontrols.charts.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

enum class SeriesType {
    HEART_RATE, ACTIVITY
}

fun getDataAdapter(lineSeries: LineSeries): DataAdapter<Date, Double> {
    @Suppress("UNCHECKED_CAST")
    return lineSeries.dataAdapter as DataAdapter<Date, Double>
}

fun createSeries(seriesType: SeriesType, context: Context, filename: String): LineSeries {
    val lineSeries = LineSeries()
    lineSeries.dataAdapter = createDataAdapter(seriesType, context, filename)
    when (seriesType) {
        SeriesType.HEART_RATE -> styleBpmSeries(lineSeries, context)
        else -> {
            styleMsSeries(lineSeries, context)
        }
    }
    return lineSeries
}

private fun createDataAdapter(seriesType: SeriesType, context: Context,
                              filename: String): DataAdapter<Date, Double> {
    val dataAdapter = SimpleDataAdapter<Date, Double>()
    populateDataAdapter(seriesType, dataAdapter, filename, context)
    return dataAdapter
}

private fun populateDataAdapter(seriesType: SeriesType,
                                dataAdapter: DataAdapter<Date, Double>,
                                filename: String, context: Context) {

    val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))

    do {
        val dataRow = reader.readLine()
        if (dataRow != null) {
            val lineItem = dataRow.split(',')
            dataAdapter.add(DataPoint(Date(lineItem[0].toLong()),
                    getValueDataElement(lineItem[1].toDouble(), seriesType)))
        }
    } while (dataRow != null)

    reader.close()
}

private fun getValueDataElement(value: Double, seriesType: SeriesType): Double {
    return when (seriesType) {
        SeriesType.HEART_RATE -> value
        else -> {
            calculateNegativePace(value)
        }
    }
}

private fun calculateNegativePace(metersPerSecond: Double): Double {
    //Calc mph
    val mph: Double = metersPerSecond * 2.23694
    //Now pace
    val pace: Double = 60 / mph
    //Negate it
    return pace * -1
}

private fun styleBpmSeries(bpmSeries: LineSeries, context: Context) {
    bpmSeries.style = AdvancedLineSeriesStyle().apply {
        areaLineColor = ContextCompat.getColor(context, R.color.colorBpmLine)
        areaColor = ContextCompat.getColor(context, R.color.colorBpmLine)
        fillStyle = SeriesStyle.FillStyle.GRADIENT
        addGradientStop(GradientStop.create(ContextCompat.getColor(context, R.color
                .colorBpmLow), 0.3f))
        addGradientStop(GradientStop.create(ContextCompat.getColor(context, R.color
                .colorBpmMedium), 0.6f))
        addGradientStop(GradientStop.create(ContextCompat.getColor(context, R.color
                .colorBpmHigh), 0.9f))
    }
    bpmSeries.title = context.getString(R.string.hr_series_title)
}

private fun styleMsSeries(msSeries: LineSeries, context: Context) {
    with(msSeries) {
        style.lineColor = ContextCompat.getColor(context, R.color.colorMsLine)
        title = context.getString(R.string.pace_series_title)
        visibility = Series.INVISIBLE
        isShownInLegend = false
    }
}