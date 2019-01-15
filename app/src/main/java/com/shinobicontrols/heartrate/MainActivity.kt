package com.shinobicontrols.heartrate

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextPaint
import android.view.View
import com.shinobicontrols.advancedcharting.animation.AxisSpanAnimationRunner
import com.shinobicontrols.advancedcharting.sampling.NthPointSampler
import com.shinobicontrols.advancedcharting.smoothing.CatmullRomSplineSmoother
import com.shinobicontrols.charts.*
import com.shinobicontrols.charts.Annotation
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : ShinobiChart.OnInternalLayoutListener,
        ShinobiChart.OnTickMarkDrawListener, AppCompatActivity() {
    private lateinit var shinobiChart: ShinobiChart
    private lateinit var orientationStrategy: ScreenOrientationStrategy
    private val maxImagePixelSizes = MaxImagePixelSizes(0, 0)
    private val viewAnnotations = ArrayList<Annotation>()
    private val paceMajorTickLabelPaint = TextPaint()
    private val enableSmoothing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val supportChartFragment =
                supportFragmentManager.findFragmentById(R.id.chart) as SupportChartFragment
        shinobiChart = supportChartFragment.shinobiChart
        shinobiChart.setOnInternalLayoutListener(this)
        shinobiChart.setOnTickMarkDrawListener(this)
        orientationStrategy = ScreenOrientationStrategyFactory.getScreenOrientationStrategy(
                resources.configuration.orientation, maxImagePixelSizes)
        updateLabelPaint(paceMajorTickLabelPaint, resources)

        //Only create the chart if it has not been created before
        if (savedInstanceState == null) {
            styleChart()
            //Set up the chart axes
            val xAxis = getXAxis()
            val yAxis = getYAxis(YAxisType.Y, resources)
            val reverseYAxis = getYAxis(YAxisType.REVERSE_Y,
                    resources)
            shinobiChart.xAxis = xAxis
            shinobiChart.yAxis = yAxis
            //Create each series
            val bpmSeries = createSeries(SeriesType.HEART_RATE,
                    applicationContext, getString(R.string.hr_filename))
            val msMorningSeries = createSeries(SeriesType.ACTIVITY,
                    applicationContext, getString(R.string.morning_walk_filename))
            val msLunchSeries = createSeries(SeriesType.ACTIVITY,
                    applicationContext, getString(R.string.lunch_run_filename))
            val msEveningSeries = createSeries(SeriesType.ACTIVITY,
                    applicationContext, getString(R.string.evening_walk_filename))
            val bpmDataAdapter: DataAdapter<Date, Double> = getDataAdapter(bpmSeries)
            val msMorningDataAdapter: DataAdapter<Date, Double> = getDataAdapter(msMorningSeries)
            val msLunchDataAdapter: DataAdapter<Date, Double> = getDataAdapter(msLunchSeries)
            val msEveningDataAdapter: DataAdapter<Date, Double> = getDataAdapter(msEveningSeries)
            //Enable smoothing and sampling
            enableSmoothing(enableSmoothing, arrayOf(bpmSeries, msMorningSeries,
                    msLunchSeries, msEveningSeries))
            bpmSeries.dataAdapter = NthPointSampler<Date, Double>(bpmDataAdapter, 30)
            msMorningSeries.dataAdapter = NthPointSampler(msMorningDataAdapter, 5)
            msLunchSeries.dataAdapter = NthPointSampler(msLunchDataAdapter, 5)
            msEveningSeries.dataAdapter = NthPointSampler(msEveningDataAdapter, 5)
            //Add each series to the chart
            with(shinobiChart) {
                addSeries(bpmSeries)
                addSeries(msMorningSeries, xAxis, reverseYAxis)
                addSeries(msLunchSeries, xAxis, reverseYAxis)
                addSeries(msEveningSeries, xAxis, reverseYAxis)
            }
            //Add listeners to the x axis to show the pace series and tick marks on zoom
            with(xAxis) {
                addOnRangeChangeListener(createAxisSpanAnimationRunner(msMorningSeries,
                        createSeriesAnimationCreator()))
                addOnRangeChangeListener(createAxisSpanAnimationRunner(msLunchSeries,
                        createSeriesAnimationCreator()))
                addOnRangeChangeListener(createAxisSpanAnimationRunner(msEveningSeries,
                        createSeriesAnimationCreator()))
                addOnRangeChangeListener(createLegendAndPaceTickmarkUpdater(shinobiChart))
            }
            //Register our activities with the annotations helper
            registerActivities(Pair(ActivityType.WALK, msMorningDataAdapter),
                    Pair(ActivityType.RUN, msLunchDataAdapter),
                    Pair(ActivityType.WALK, msEveningDataAdapter))
            addBandAnnotations(shinobiChart.annotationsManager, xAxis, yAxis, applicationContext)
        } else {
            maxImagePixelSizes.landscape = savedInstanceState.getInt(MAX_IMAGE_PIXEL_SIZE_LANDSCAPE)
            maxImagePixelSizes.portrait = savedInstanceState.getInt(MAX_IMAGE_PIXEL_SIZE_PORTRAIT)
        }
        shinobiChart.redrawChart()
    }

    private fun styleChart() {
        with(shinobiChart) {
            style.plotAreaBackgroundColor = Color.WHITE
            style.backgroundColor = Color.WHITE
            legend.position = Legend.Position.BOTTOM_CENTER
            legend.visibility = View.VISIBLE
        }
    }

    private fun enableSmoothing(enable: Boolean, seriesArray: Array<LineSeries>) {
        if (enable) {
            for (s in seriesArray)
                s.linePathInterpolator = CatmullRomSplineSmoother<Date, Double>(6)
        }
    }

    private fun createSeriesAnimationCreator(): SeriesAnimationCreator<Float, Float> {

        return object : SeriesAnimationCreator<Float, Float> {

            val fadeAnimationCreator = FadeAnimationCreator()
            override fun createExitAnimation(series: Series<*>?): Animation<Float> {
                return fadeAnimationCreator.createExitAnimation(series)
            }

            override fun createEntryAnimation(series: Series<*>?): Animation<Float> {
                return fadeAnimationCreator.createEntryAnimation(series)
            }
        }
    }

    private fun createAxisSpanAnimationRunner(lineSeries: LineSeries,
                                              seriesAnimationCreator:
                                              SeriesAnimationCreator<Float, Float>):
            AxisSpanAnimationRunner {
        return AxisSpanAnimationRunner.builder(lineSeries)
                .withInAnimation(seriesAnimationCreator.createEntryAnimation(lineSeries))
                .withOutAnimation(seriesAnimationCreator.createExitAnimation(lineSeries))
                .withUpperTransition(DateFrequency(3, DateFrequency.Denomination.HOURS),
                        DateFrequency(90, DateFrequency.Denomination.MINUTES))
                .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        for (item in viewAnnotations) {
            shinobiChart.annotationsManager.removeAnnotation(item)
            shinobiChart.xAxis.setCurrentDisplayedRangePreservedOnUpdate(false)
        }
    }

    override fun onInternalLayout(chart: ShinobiChart?) {
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

    override fun onDrawTickMark(canvas: Canvas?, tickMark: TickMark?, labelBackgroundRect: Rect?,
                                tickMarkRect: Rect?, axis: Axis<*, *>?) {
        ChartUtils.drawTickMarkLine(canvas, tickMark)
        val x = labelBackgroundRect!!.centerX()
        val y = labelBackgroundRect.centerY()
        if (axis!! == shinobiChart.allYAxes[1]) {
            tickMark!!.labelText = convertLabelSign(tickMark.value as Double)
        }
        ChartUtils.drawText(canvas, tickMark!!.labelText, x, y, paceMajorTickLabelPaint)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run { putInt(MAX_IMAGE_PIXEL_SIZE_LANDSCAPE, maxImagePixelSizes.landscape) }
        outState?.run { putInt(MAX_IMAGE_PIXEL_SIZE_PORTRAIT, maxImagePixelSizes.portrait) }
        super.onSaveInstanceState(outState)
    }
}
