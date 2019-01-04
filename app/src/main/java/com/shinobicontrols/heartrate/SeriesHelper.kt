package com.shinobicontrols.heartrate

import android.content.Context
import com.shinobicontrols.charts.DataAdapter
import com.shinobicontrols.charts.DataPoint
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