package com.shinobicontrols.heartrate

import android.content.Context
import android.support.v4.content.ContextCompat
import com.shinobicontrols.advancedcharting.styling.AdvancedLineSeriesStyle
import com.shinobicontrols.advancedcharting.styling.GradientStop
import com.shinobicontrols.charts.DataAdapter
import com.shinobicontrols.charts.DataPoint
import com.shinobicontrols.charts.LineSeries
import com.shinobicontrols.charts.SeriesStyle
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

fun populateDataAdapter(dataAdapter: DataAdapter<Date, Double>,
                        filename: String, context: Context) {

    val reader = BufferedReader(InputStreamReader(context.assets.open(filename)))

    do {
        val dataRow = reader.readLine()
        if (dataRow != null) {
            val lineItem = dataRow.split(',')
            dataAdapter.add(DataPoint(Date(lineItem[0].toLong()),
                    lineItem[1].toDouble()))
        }
    } while (dataRow != null)

    reader.close()
}

fun styleBpmSeries(bpmSeries: LineSeries, context: Context) {
    val seriesStyle = AdvancedLineSeriesStyle()
    with(seriesStyle) {
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
    bpmSeries.style = seriesStyle
    bpmSeries.title = context.getString(R.string.hr_series_title)
}