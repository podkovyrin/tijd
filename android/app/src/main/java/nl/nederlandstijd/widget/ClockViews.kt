package nl.nederlandstijd.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.util.SizeF
import android.view.Gravity
import android.widget.RemoteViews
import java.util.Calendar
import kotlin.math.pow

internal object ClockViews {
    fun currentText(languageCode: String = SpokenTime.defaultLanguage()): String = Calendar.getInstance().run {
        SpokenTime.format(languageCode, get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
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
    fun prepare(context: Context, geometry: Geometry, ink: ClockInk, singleLine: Boolean, style: WidgetStyle = WidgetStyle(), checkActive: () -> Unit) {
        val selected = style
        for (size in geometry.sizes) {
            checkActive()
            StableClockText.fit(context, "", size.width, size.height, ClockFont.layoutFor(effectiveInk(ink, selected), selected.fontId), singleLine, selected.sizePercent, selected.languageCode, checkActive)
        }
    }

    fun create(
        context: Context,
        widthDp: Float,
        heightDp: Float,
        text: String? = null,
        ink: ClockInk = ClockInk.Light,
        singleLine: Boolean = false,
        partial: Boolean = false,
        style: WidgetStyle = WidgetStyle(),
    ): RemoteViews {
        val selected = style
        val effectiveInk = effectiveInk(ink, selected)
        val layoutId = ClockFont.layoutFor(effectiveInk, selected.fontId)
        val phrase = text ?: currentText(selected.languageCode)
        val fit = StableClockText.fit(context, phrase, widthDp, heightDp, layoutId, singleLine, selected.sizePercent, selected.languageCode)
        return RemoteViews(context.packageName, layoutId).apply {
            setTextViewText(R.id.clock_text, phrase)
            setInt(R.id.clock_text, "setLayoutDirection", android.text.TextUtils.getLayoutDirectionFromLocale(java.util.Locale.forLanguageTag(selected.languageCode)))
            @SuppressLint("RtlHardcoded") // Explicit Left and Right choices must stay physical in RTL locales.
            val gravity = when (selected.alignment) {
                WidgetAlignment.AUTO -> if (fit.lineCount == 1) Gravity.CENTER else Gravity.TOP or Gravity.START
                WidgetAlignment.START -> Gravity.CENTER_VERTICAL or Gravity.LEFT
                WidgetAlignment.CENTER -> Gravity.CENTER
                WidgetAlignment.END -> Gravity.CENTER_VERTICAL or Gravity.RIGHT
            }
            setInt(R.id.clock_text, "setGravity", gravity)
            if (!partial) {
                setTextColor(R.id.clock_text, StyleCatalog.color(selected.colorId)?.argb
                    ?: if (effectiveInk == ClockInk.Light) Color.WHITE else Color.BLACK)
                val panel = StyleCatalog.color(selected.backgroundId)?.argb
                val background = panel?.let { (it and 0x00ffffff) or ((255 * selected.backgroundOpacity / 100) shl 24) }
                    ?: Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val drawable = when (selected.cornerRadius) {
                        0 -> R.drawable.widget_panel_square
                        32 -> R.drawable.widget_panel_round
                        else -> R.drawable.widget_panel_soft
                    }
                    setInt(R.id.clock_text, "setBackgroundResource", drawable)
                    setColorStateList(R.id.clock_text, "setBackgroundTintList", ColorStateList.valueOf(background))
                } else {
                    setInt(R.id.clock_text, "setBackgroundColor", background)
                }
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


    /** Explicit text chooses its shadow; automatic text contrasts with the visible panel.
     * Wallpaper metadata supplies only a light/dark hint, so translucent panels use that
     * hint as their backing color. Fully transparent panels preserve wallpaper behavior.
     */
    private fun effectiveInk(wallpaperInk: ClockInk, style: WidgetStyle): ClockInk {
        StyleCatalog.color(style.colorId)?.let {
            return if (luminance(it.argb) > 0.179) ClockInk.Light else ClockInk.Dark
        }
        val panel = StyleCatalog.color(style.backgroundId)?.argb ?: return wallpaperInk
        if (style.backgroundOpacity == 0) return wallpaperInk
        val alpha = style.backgroundOpacity / 100.0
        val backing = if (wallpaperInk == ClockInk.Dark) 255 else 0
        fun blend(channel: Int) = (channel * alpha + backing * (1 - alpha)).toInt()
        val blended = Color.rgb(blend(Color.red(panel)), blend(Color.green(panel)), blend(Color.blue(panel)))
        return if (luminance(blended) > 0.179) ClockInk.Dark else ClockInk.Light
    }

    private fun luminance(color: Int): Double {
        fun linear(channel: Int): Double {
            val value = channel / 255.0
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(Color.red(color)) + 0.7152 * linear(Color.green(color)) + 0.0722 * linear(Color.blue(color))
    }

    fun forWidget(
        context: Context,
        options: Bundle,
        text: String? = null,
        ink: ClockInk = ClockInk.Light,
        singleLine: Boolean = false,
        style: WidgetStyle = WidgetStyle(),
    ): RemoteViews = forGeometry(context, geometry(options), text ?: currentText(style.languageCode), ink, singleLine, style = style)

    fun forGeometry(
        context: Context,
        geometry: Geometry,
        text: String,
        ink: ClockInk,
        singleLine: Boolean,
        partial: Boolean = false,
        style: WidgetStyle = WidgetStyle(),
    ): RemoteViews {
        require(!partial || geometry.sizes.size == 1)
        val views = geometry.sizes.associateWith {
            create(context, it.width, it.height, text, ink, singleLine, partial, style)
        }
        if (views.size == 1) return views.values.first()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && geometry.responsive) return RemoteViews(views)
        return RemoteViews(views.values.first(), views.values.last())
    }
}
