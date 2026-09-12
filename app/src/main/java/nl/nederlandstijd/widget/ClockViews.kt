package nl.nederlandstijd.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.util.SizeF
import android.view.Gravity
import android.widget.RemoteViews
import java.util.Calendar

internal object ClockViews {
    fun currentText(): String = Calendar.getInstance().run {
        DutchTimeFormatter.format(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
    }

    data class Geometry(val sizes: List<SizeF>, val responsive: Boolean)

    fun geometry(options: Bundle): Geometry {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION") // Also runs on API 31 and 32.
            val sizes = options.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                .orEmpty().filter { it.width.isFinite() && it.height.isFinite() && it.width > 0 && it.height > 0 }
                .distinct().take(16)
            if (sizes.isNotEmpty()) return Geometry(sizes, responsive = true)
        }
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110).coerceAtLeast(1)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110).coerceAtLeast(1)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth).coerceAtLeast(minWidth)
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight).coerceAtLeast(minHeight)
        return Geometry(listOf(
            SizeF(maxWidth.toFloat(), minHeight.toFloat()),
            SizeF(minWidth.toFloat(), maxHeight.toFloat()),
        ).distinct(), responsive = false)
    }

    /** Complete any expensive cache misses before sampling the time for publication. */
    fun prepare(context: Context, geometry: Geometry, ink: ClockInk, singleLine: Boolean, checkActive: () -> Unit) {
        for (size in geometry.sizes) {
            checkActive()
            StableClockText.fit(context, "", size.width, size.height, ClockFont.layoutFor(ink), singleLine, checkActive)
        }
    }

    fun create(
        context: Context,
        widthDp: Float,
        heightDp: Float,
        text: String = currentText(),
        ink: ClockInk = ClockInk.Light,
        singleLine: Boolean = false,
        partial: Boolean = false,
    ): RemoteViews {
        val layoutId = ClockFont.layoutFor(ink)
        val fit = StableClockText.fit(context, text, widthDp, heightDp, layoutId, singleLine)
        return RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.clock_text, text)
            setInt(R.id.clock_text, "setGravity", if (fit.lineCount == 1) Gravity.CENTER else Gravity.TOP or Gravity.START)
            if (!partial) {
                setTextViewTextSize(R.id.clock_text, TypedValue.COMPLEX_UNIT_PX, fit.sizePx)
                setOnClickPendingIntent(
                    R.id.clock_text,
                    PendingIntent.getActivity(
                        context, 0, Intent(context, ClockLaunchActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    ),
                )
            }
        }
    }

    fun forWidget(
        context: Context,
        options: Bundle,
        text: String = currentText(),
        ink: ClockInk = ClockInk.Light,
        singleLine: Boolean = false,
    ): RemoteViews = forGeometry(context, geometry(options), text, ink, singleLine)

    fun forGeometry(
        context: Context,
        geometry: Geometry,
        text: String,
        ink: ClockInk,
        singleLine: Boolean,
        partial: Boolean = false,
    ): RemoteViews {
        require(!partial || geometry.sizes.size == 1)
        val views = geometry.sizes.associateWith {
            create(context, it.width, it.height, text, ink, singleLine, partial)
        }
        if (views.size == 1) return views.values.first()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && geometry.responsive) return RemoteViews(views)
        return RemoteViews(views.values.first(), views.values.last())
    }
}
