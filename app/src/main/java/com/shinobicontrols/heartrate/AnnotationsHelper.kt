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
private val activityStartEndDatePairs = ArrayList<ActivityStartEndDatePair>()

enum class ActivityType {
    WALK, RUN
}

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
    if (!maxImagePixelSizes.isSet()) {
        calculateMaxViewAnnotationPixelSizes(getViewAnnotationPixelSize(
                activityStartEndDatePairs[0].startDate,
                activityStartEndDatePairs[0].endDate,
                xAxis),
                orientationStrategy,
                windowManager)
    }

    for (item in activityStartEndDatePairs) {
        addViewAnnotation(annotationsManager, xAxis, yAxis,
                item.startDate,
                item.endDate,
                orientationStrategy.maxViewAnnotationSizeToUse(),
                item.iconResId,
                context,
                viewAnnotations)
    }
}

fun registerActivities(vararg activities: Pair<ActivityType, DataAdapter<Date, Double>>) {
    for (activity in activities) {
        registerActivity(activity.first, activity.second)
    }
}

private fun registerActivity(activityType: ActivityType,
                             dataAdapter: DataAdapter<Date, Double>) {
    //get date (x) of first and last data point
    activityStartEndDatePairs.add(ActivityStartEndDatePair(dataAdapter.get(0).x,
            dataAdapter.get(dataAdapter.size() - 1).x,
            if (activityType == ActivityType.WALK) R.drawable.ic_walk_round else R.drawable
                    .ic_run_round))
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
            getActivityTimeMidPoint(startDate, endDate), 150.0,
            xAxis,
            yAxis)
    viewAnnotations.add(annotation)
}

private fun getViewAnnotationPixelSize(startDate: Date,
                                       endDate: Date,
                                       xAxis: DateTimeAxis): Int {

    return ((xAxis.getPixelValueForUserValue(endDate) -
            xAxis.getPixelValueForUserValue(startDate)) * .8).toInt()
}

private fun calculateMaxViewAnnotationPixelSizes(currentSize: Int,
                                                 orientationStrategy: ScreenOrientationStrategy,
                                                 windowManager: WindowManager) {
    orientationStrategy.calculateMaxViewAnnotationPixelSizes(currentSize,
            getScreenDimensionRatio(windowManager))
}

private fun getScreenDimensionRatio(windowManager: WindowManager): Double {
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    return metrics.widthPixels.toDouble() / metrics.heightPixels.toDouble()
}

private fun getActivityTimeMidPoint(startDate: Date,
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
