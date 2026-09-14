package com.podkovyrin.tijd

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.appwidget.AppWidgetProviderInfo
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi

internal enum class ClockInk(val layoutId: Int) {
    Light(R.layout.widget_clock),
    Dark(R.layout.widget_clock_dark_text),
}

internal object WallpaperAppearance {
    /** Call off the main thread: wallpaper color metadata is obtained through IPC. */
    fun resolve(context: Context, options: Bundle = Bundle()): ClockInk {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return ClockInk.Light
        val category = options.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY)
        val wallpaper = if (category and AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD != 0) {
            WallpaperManager.FLAG_LOCK
        } else {
            WallpaperManager.FLAG_SYSTEM
        }
        val colors = try {
            val manager = WallpaperManager.getInstance(context)
            manager.getWallpaperColors(wallpaper)
                ?: if (wallpaper == WallpaperManager.FLAG_LOCK) {
                    manager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                } else null
        } catch (_: RuntimeException) {
            // Some devices or live wallpapers do not expose color metadata.
            null
        }
        return fromColors(colors)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun fromColors(colors: WallpaperColors?): ClockInk =
        if (colors != null && colors.colorHints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT != 0) {
            ClockInk.Dark
        } else {
            ClockInk.Light
        }
}
