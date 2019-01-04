package com.shinobicontrols.heartrate

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.shinobicontrols.advancedcharting.sampling.NthPointSampler
import com.shinobicontrols.advancedcharting.smoothing.CatmullRomSplineSmoother
import com.shinobicontrols.charts.*
import com.shinobicontrols.charts.Annotation
import java.util.*

class MainActivity : ShinobiChart.OnInternalLayoutListener
        , AppCompatActivity() {
    private lateinit var shinobiChart: ShinobiChart
    private lateinit var orientationStrategy: ScreenOrientationStrategy
    private val maxImagePixelSizes = MaxImagePixelSizes(0, 0)
    private val viewAnnotations = ArrayList<Annotation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val supportChartFragment =
                supportFragmentManager.findFragmentById(R.id.chart) as SupportChartFragment
        shinobiChart = supportChartFragment.shinobiChart
        shinobiChart.setOnInternalLayoutListener(this)
        orientationStrategy = ScreenOrientationStrategyFactory.getScreenOrientationStrategy(
                resources.configuration.orientation, maxImagePixelSizes)

        //Only create the chart if it has not been created before
        if (savedInstanceState == null) {
            val xAxis = getXAxis()
            val yAxis = getYAxis(YAxisType.Y, resources)
            val reverseYAxis = getYAxis(YAxisType.REVERSE_Y,
                    resources)
            shinobiChart.xAxis = xAxis
            shinobiChart.yAxis = yAxis
            val bpmSeries = getSeries(SeriesType.HEART_RATE,
                    applicationContext)
            val msMorningSeries = getSeries(SeriesType.MORNING_WALK,
                    applicationContext)
            val msLunchSeries = getSeries(SeriesType.LUNCH_RUN,
                    applicationContext)
            val msEveningSeries = getSeries(SeriesType.EVENING_WALK,
                    applicationContext)
            val bpmDataAdapter: DataAdapter<Date, Double> = getDataAdapter(bpmSeries)
            val msMorningDataAdapter: DataAdapter<Date, Double> = getDataAdapter(msMorningSeries)
            val msLunchDataAdapter: DataAdapter<Date, Double> = getDataAdapter(msLunchSeries)
            val msEveningDataAdapter: DataAdapter<Date, Double> = getDataAdapter(msEveningSeries)
            enableSmoothing(arrayOf(bpmSeries, msMorningSeries,
                    msLunchSeries, msEveningSeries))
            bpmSeries.dataAdapter = NthPointSampler<Date, Double>(bpmDataAdapter, 30)
            msMorningSeries.dataAdapter = NthPointSampler(msMorningDataAdapter, 5)
            msLunchSeries.dataAdapter = NthPointSampler(msLunchDataAdapter, 5)
            msEveningSeries.dataAdapter = NthPointSampler(msEveningDataAdapter, 5)
            with(shinobiChart) {
                addSeries(bpmSeries)
                addSeries(msMorningSeries, xAxis, reverseYAxis)
                addSeries(msLunchSeries, xAxis, reverseYAxis)
                addSeries(msEveningSeries, xAxis, reverseYAxis)
            }
            addBandAnnotations(shinobiChart.annotationsManager, xAxis, yAxis, applicationContext)
            styleChart()
            shinobiChart.redrawChart()
        } else {
            maxImagePixelSizes.landscape = savedInstanceState.getInt(MAX_IMAGE_PIXEL_SIZE_LANDSCAPE)
            maxImagePixelSizes.portrait = savedInstanceState.getInt(MAX_IMAGE_PIXEL_SIZE_PORTRAIT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (item in viewAnnotations) {
            shinobiChart.annotationsManager.removeAnnotation(item)
            shinobiChart.xAxis.setCurrentDisplayedRangePreservedOnUpdate(false)
        }
    }

    override fun onInternalLayout(p0: ShinobiChart?) {
        addViewAnnotations(shinobiChart.annotationsManager,
                shinobiChart.xAxis as DateTimeAxis,
                shinobiChart.yAxis as NumberAxis,
                maxImagePixelSizes,
                orientationStrategy,
                windowManager,
                applicationContext,
                viewAnnotations)
        shinobiChart.setOnInternalLayoutListener(null)
        shinobiChart.xAxis.setCurrentDisplayedRangePreservedOnUpdate(true)
    }

    private fun styleChart() {
        with(shinobiChart) {
            style.plotAreaBackgroundColor = Color.WHITE
            style.backgroundColor = Color.WHITE
            legend.position = Legend.Position.BOTTOM_CENTER
            legend.visibility = View.VISIBLE
        }
    }

    private fun enableSmoothing(seriesArray: Array<LineSeries>) {
        for (s in seriesArray)
            s.linePathInterpolator = CatmullRomSplineSmoother<Date, Double>(6)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run { putInt(MAX_IMAGE_PIXEL_SIZE_LANDSCAPE, maxImagePixelSizes.landscape) }
        outState?.run { putInt(MAX_IMAGE_PIXEL_SIZE_PORTRAIT, maxImagePixelSizes.portrait) }
        super.onSaveInstanceState(outState)
    }
}
