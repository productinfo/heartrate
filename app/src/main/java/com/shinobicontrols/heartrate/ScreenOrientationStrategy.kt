package com.shinobicontrols.heartrate

import android.content.res.Configuration

interface ScreenOrientationStrategy {
    fun maxViewAnnotationSizeToUse(): Int
    fun calculateMaxViewAnnotationPixelSizes(currentSize: Int, ratio: Double)
}

private abstract class BaseScreenOrientationStrategy(
        val pixelSizes: MaxImagePixelSizes) : ScreenOrientationStrategy

private class LandscapeOrientationStrategy(
        pixelSizes: MaxImagePixelSizes) : BaseScreenOrientationStrategy(pixelSizes) {

    override fun maxViewAnnotationSizeToUse(): Int {
        return pixelSizes.landscape
    }

    override fun calculateMaxViewAnnotationPixelSizes(currentSize: Int, ratio: Double) {
        pixelSizes.landscape = currentSize
        pixelSizes.portrait = (currentSize / ratio).toInt()
    }
}

private class PortraitOrientationStrategy(
        pixelSizes: MaxImagePixelSizes) : BaseScreenOrientationStrategy(pixelSizes) {
    override fun maxViewAnnotationSizeToUse(): Int {
        return pixelSizes.portrait
    }

    override fun calculateMaxViewAnnotationPixelSizes(currentSize: Int, ratio: Double) {
        pixelSizes.portrait = currentSize
        pixelSizes.landscape = (currentSize / ratio).toInt()
    }
}

class ScreenOrientationStrategyFactory {
    companion object {
        @JvmStatic
        fun getScreenOrientationStrategy(orientation: Int,
                                         pixelSizes: MaxImagePixelSizes): ScreenOrientationStrategy {
            return if (orientation == Configuration
                            .ORIENTATION_LANDSCAPE) LandscapeOrientationStrategy(pixelSizes) else
                (PortraitOrientationStrategy(pixelSizes))
        }
    }
}