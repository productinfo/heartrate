package com.shinobicontrols.heartrate

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.shinobicontrols.charts.*
import com.shinobicontrols.charts.Annotation
import java.util.*

const val MAX_IMAGE_PIXEL_SIZE_LANDSCAPE = "maxImagePixelSizeLandscape"
const val MAX_IMAGE_PIXEL_SIZE_PORTRAIT = "maxImagePixelSizePortrait"
private val activityStartEndDatePairs = createActivityStartEndDatePairs()


fun addBandAnnotations(annotationsManager: AnnotationsManager,
                       xAxis: DateTimeAxis,
                       yAxis: NumberAxis,
                       context: Context) {
    for (item in activityStartEndDatePairs) {
        addBandAnnotation(annotationsManager, xAxis, yAxis,
                item.startDate,
                item.endDate,
                context)
    }
}

fun addViewAnnotations(annotationsManager: AnnotationsManager,
                       xAxis: DateTimeAxis,
                       yAxis: NumberAxis,
                       maxImagePixelSizes: MaxImagePixelSizes,
                       orientationStrategy: ScreenOrientationStrategy,
                       windowManager: WindowManager,
                       context: Context,
                       viewAnnotations: ArrayList<Annotation>) {
    val calculatedViewAnnotationPixelSize =
            calculateViewAnnotationPixelSize(activityStartEndDatePairs[0].startDate,
                    activityStartEndDatePairs[0].endDate, xAxis)
    if (!maxImagePixelSizes.isSet())
        calculateMaxViewAnnotationPixelSizes(calculatedViewAnnotationPixelSize, orientationStrategy,
                windowManager)

    for (item in activityStartEndDatePairs) {
        addViewAnnotation(annotationsManager, xAxis, yAxis,
                item.startDate,
                item.endDate,
                Math.min(calculatedViewAnnotationPixelSize,
                        orientationStrategy.maxViewAnnotationSizeToUse()),
                item.iconResId,
                context,
                viewAnnotations)
    }
}

private fun createActivityStartEndDatePairs(): ArrayList<ActivityStartEndDatePair> {
    val activityStartEndDatePairs = ArrayList<ActivityStartEndDatePair>()
    val calendar = GregorianCalendar()
    //Morning walk
    calendar.set(2018, Calendar.SEPTEMBER, 4, 8, 0, 0)
    var activityStart = calendar.time
    calendar.add(Calendar.MINUTE, 30)
    var activityEnd = calendar.time
    activityStartEndDatePairs.add(ActivityStartEndDatePair(activityStart, activityEnd,
            R.drawable.ic_walk_round))

    //Lunchtime run
    calendar.set(2018, Calendar.SEPTEMBER, 4, 12, 30, 0)
    activityStart = calendar.time
    calendar.add(Calendar.MINUTE, 60)
    activityEnd = calendar.time
    activityStartEndDatePairs.add(ActivityStartEndDatePair(activityStart, activityEnd,
            R.drawable.ic_run_round))

    //Evening run
    calendar.set(2018, Calendar.SEPTEMBER, 4, 17, 30, 0)
    activityStart = calendar.time
    calendar.add(Calendar.MINUTE, 30)
    activityEnd = calendar.time
    activityStartEndDatePairs.add(ActivityStartEndDatePair(activityStart, activityEnd,
            R.drawable.ic_walk_round))
    return activityStartEndDatePairs
}

private fun addBandAnnotation(annotationsManager: AnnotationsManager,
                              xAxis: DateTimeAxis,
                              yAxis: NumberAxis,
                              startDate: Date,
                              endDate: Date,
                              context: Context) {

    val annotation = annotationsManager.addVerticalBandAnnotation(DateRange(startDate, endDate),
            xAxis, yAxis)
    annotation.style.backgroundColor = ContextCompat.getColor(context, R.color.colorBandAnnotation)
    annotation.position = Annotation.Position.BEHIND_DATA
}

private fun addViewAnnotation(annotationsManager: AnnotationsManager,
                              xAxis: DateTimeAxis,
                              yAxis: NumberAxis,
                              startDate: Date,
                              endDate: Date,
                              size: Int,
                              resourceId: Int,
                              context: Context,
                              viewAnnotations: ArrayList<Annotation>) {

    val annotationView = ImageView(context).apply {
        layoutParams = ViewGroup.LayoutParams(size, size)
        setImageResource(resourceId)
    }
    val annotation = annotationsManager.addViewAnnotation(annotationView,
            calculateActivityTimeMidPoint(startDate, endDate), 150.0,
            xAxis,
            yAxis)
    viewAnnotations.add(annotation)
}

private fun calculateViewAnnotationPixelSize(startDate: Date,
                                              endDate: Date,
                                              xAxis: DateTimeAxis): Int {

    return ((xAxis.getPixelValueForUserValue(endDate) -
            xAxis.getPixelValueForUserValue(startDate)) * .8).toInt()
}

private fun calculateMaxViewAnnotationPixelSizes(currentSize: Int,
                                                 orientationStrategy: ScreenOrientationStrategy,
                                                 windowManager: WindowManager) {
    orientationStrategy.calculateMaxViewAnnotationPixelSizes(currentSize,
            calculateScreenDimensionRatio(windowManager))
}

private fun calculateScreenDimensionRatio(windowManager: WindowManager): Double {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.widthPixels.toDouble() / metrics.heightPixels.toDouble()
}

private fun calculateActivityTimeMidPoint(startDate: Date,
                                          endDate: Date): Date {
    return Date((startDate.time + endDate.time) / 2)
}

data class MaxImagePixelSizes(var landscape: Int,
                              var portrait: Int) {
    fun isSet(): Boolean {
        return landscape > 0 && portrait > 0
    }
}

private data class ActivityStartEndDatePair(val startDate: Date,
                                            val endDate: Date,
                                            val iconResId: Int)
