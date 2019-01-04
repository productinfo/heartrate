package com.shinobicontrols.heartrate

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.shinobicontrols.advancedcharting.sampling.NthPointSampler
import com.shinobicontrols.advancedcharting.smoothing.CatmullRomSplineSmoother
import com.shinobicontrols.charts.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var shinobiChart: ShinobiChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val supportChartFragment =
                supportFragmentManager.findFragmentById(R.id.chart) as SupportChartFragment
        shinobiChart = supportChartFragment.shinobiChart

        //Only create the chart if it has not been created before
        if (savedInstanceState == null) {
            val xAxis = DateTimeAxis()
            val yAxis = NumberAxis(NumberRange(40.0, 165.0))
            shinobiChart.xAxis = xAxis
            shinobiChart.yAxis = yAxis
            val bpmSeries = LineSeries()
            val bpmDataAdapter = SimpleDataAdapter<Date, Double>()
            populateDataAdapter(bpmDataAdapter, getString(R.string.hr_filename),
                    applicationContext)
            bpmSeries.dataAdapter = NthPointSampler<Date, Double>(bpmDataAdapter, 30)
            bpmSeries.linePathInterpolator = CatmullRomSplineSmoother<Date, Double>(6)
            styleBpmSeries(bpmSeries, applicationContext)
            shinobiChart.addSeries(bpmSeries)
        }
    }
}
