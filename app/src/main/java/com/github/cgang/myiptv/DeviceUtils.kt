package com.github.cgang.myiptv

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Utility object for detecting device type (TV vs handheld).
 *
 * Modern TVs are consistently 16:9 aspect ratio, while handheld devices
 * have various aspect ratios (19.5:9, 20:9, 16:10, etc.).
 *
 * This utility combines multiple signals for reliable detection:
 * 1. UI_MODE_TYPE_TELEVISION - most reliable system signal
 * 2. Leanback feature + no touchscreen - secondary signal
 * 3. 16:9 aspect ratio + no touchscreen - weak signal (fallback)
 */
object DeviceUtils {

    /**
     * Detects if the app is running on a TV device.
     *
     * Uses multiple heuristics since no single method is reliable across all OEMs.
     * Priority order:
     * 1. UI_MODE_TYPE_TELEVISION (most reliable)
     * 2. FEATURE_LEANBACK + no touchscreen
     * 3. 16:9 aspect ratio + no touchscreen (fallback)
     *
     * @param context Android context
     * @return true if running on a TV device, false for handheld (phone/tablet)
     */
    fun isTv(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val isTvMode = uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION

        val hasLeanback = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        val hasTouchscreen = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
        val is16by9 = is16by9AspectRatio(context)

        // Strong signal: TV mode
        if (isTvMode) return true

        // Strong signal: Leanback feature without touchscreen
        if (hasLeanback && !hasTouchscreen) return true

        // Weaker signal: 16:9 aspect ratio without touchscreen
        // This catches TVs that don't properly report UI_MODE_TYPE_TELEVISION
        if (is16by9 && !hasTouchscreen) return true

        return false
    }

    /**
     * Check if device has a touchscreen.
     *
     * @param context Android context
     * @return true if device has touchscreen capability (phones/tablets)
     */
    fun hasTouchscreen(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)
    }

    /**
     * Check if screen has 16:9 aspect ratio.
     *
     * Modern TVs are consistently 16:9 (1.778:1), while handheld devices
     * have taller aspect ratios like 19.5:9, 20:9, or 16:10.
     *
     * @param context Android context
     * @return true if screen aspect ratio is approximately 16:9
     */
    fun is16by9AspectRatio(context: Context): Boolean {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()

        if (width == 0f || height == 0f) return false

        val aspectRatio = width / height

        // 16:9 = 1.778, allow 5% tolerance for rounding and overscan
        return kotlin.math.abs(aspectRatio - 1.778f) < 0.09f
    }

    /**
     * Get the screen aspect ratio as a float.
     *
     * @param context Android context
     * @return width/height ratio (e.g., 1.778 for 16:9, 2.16 for 19.5:9)
     */
    fun getAspectRatio(context: Context): Float {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()

        return if (height > 0) width / height else 1.778f
    }
}
