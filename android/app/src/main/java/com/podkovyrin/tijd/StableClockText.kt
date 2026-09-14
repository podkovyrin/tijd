package com.podkovyrin.tijd

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.ceil
import java.security.MessageDigest
import java.text.BreakIterator
import java.util.Locale

/** One font size per widget geometry and system font configuration, valid for the whole day. */
internal object StableClockText {
    data class TextFit(val sizePx: Float, val lineCount: Int)

    private data class CacheKey(
        val widthPx: Int,
        val heightPx: Int,
        val layoutId: Int,
        val singleLine: Boolean,
        val sizePercent: Int,
        val configuration: Configuration,
        val languageCode: String,
    )

    private val sizes = LruCache<CacheKey, Float>(64)
    private data class Phrase(val text: String, val breaks: Set<Int>)
    private val phraseCache = LruCache<String, List<Phrase>>(4)

    private fun phrases(languageCode: String): List<Phrase> = synchronized(phraseCache) {
        phraseCache.get(languageCode) ?: run {
            val iterator = BreakIterator.getLineInstance(Locale.forLanguageTag(languageCode))
            (0..23).flatMap { hour ->
                (0..59).map { minute -> SpokenTime.format(languageCode, hour, minute) }
            }.distinct().sortedByDescending { it.length }.map { text ->
                iterator.setText(text)
                val breaks = mutableSetOf<Int>()
                var boundary = iterator.first()
                while (boundary != BreakIterator.DONE) {
                    breaks.add(boundary)
                    boundary = iterator.next()
                }
                Phrase(text, breaks)
            }.also { phraseCache.put(languageCode, it) }
        }
    }

    @SuppressLint("ApplySharedPref") // Rare cache writes finish on IO before the broadcast completes.
    fun fit(context: Context, text: String, widthDp: Float, heightDp: Float, layoutId: Int, singleLine: Boolean, sizePercent: Int = 100, languageCode: String = SpokenTime.defaultLanguage(), checkActive: () -> Unit = {}): TextFit {
        val metrics = context.resources.displayMetrics
        val key = CacheKey(
            (widthDp * metrics.density).toInt().coerceAtLeast(1),
            (heightDp * metrics.density).toInt().coerceAtLeast(1),
            layoutId, singleLine, sizePercent.coerceIn(50, 100), Configuration(context.resources.configuration),
            languageCode,
        )
        // Measure the same native view used by the host, including the device's typeface.
        val view = LayoutInflater.from(context).inflate(layoutId, FrameLayout(context), false) as TextView
        val size = synchronized(sizes) {
            checkActive()
            sizes.get(key) ?: run {
                val preferences = context.getSharedPreferences("clock_font_sizes", Context.MODE_PRIVATE)
                // Resource/font changes across app and OS upgrades must not reuse old metrics.
                val identity = persistentIdentity(key)
                val diskKey = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray())
                    .joinToString("") { "%02x".format(it) }
                val saved = preferences.getFloat(diskKey, -1f)
                if (saved.isFinite() && saved >= 0f) saved else {
                    val calculated = calculateSize(context, key, view, checkActive)
                    checkActive()
                    val editor = preferences.edit()
                    // Bound storage even after repeated resizes and system configuration changes.
                    preferences.all.keys.sorted().take((preferences.all.size - 63).coerceAtLeast(0))
                        .forEach { editor.remove(it) }
                    // This runs on the updater's IO dispatcher; persist before the receiver exits.
                    editor.putFloat(diskKey, calculated).commit()
                    ClockDiagnostics.event("font_calculated width=${key.widthPx} height=${key.heightPx}")
                    calculated
                }.also { sizes.put(key, it) }
            }
        }
        measure(view, text, size, key)
        return TextFit(size, if (size > 0f) view.lineCount else 0)
    }

    internal fun clearMemoryCache() = synchronized(sizes) { sizes.evictAll() }

    private fun persistentIdentity(key: CacheKey): String = with(key.configuration) {
        @Suppress("DEPRECATION")
        val languages = if (Build.VERSION.SDK_INT >= 24) locales.toLanguageTags() else locale.toLanguageTag()
        val weight = if (Build.VERSION.SDK_INT >= 31) fontWeightAdjustment else 0
        // Use resource-affecting values, not Configuration.toString's transient sequence numbers.
        listOf(BuildConfig.VERSION_CODE, Build.FINGERPRINT, key.widthPx, key.heightPx, key.layoutId,
            key.singleLine, key.sizePercent, key.languageCode, fontScale, densityDpi, uiMode, screenLayout, orientation, screenWidthDp,
            screenHeightDp, smallestScreenWidthDp, weight, languages, mcc, mnc, touchscreen,
            keyboard, keyboardHidden, hardKeyboardHidden, navigation, navigationHidden).joinToString("|")
    }

    private fun calculateSize(context: Context, key: CacheKey, view: TextView, checkActive: () -> Unit): Float {
        val shadowInset = ceil(view.shadowRadius + abs(view.shadowDy)).toInt()
        // Wrapping uses the actual inner width. The 16dp padding already protects the
        // horizontal shadow; a second gap would reject legitimate near-edge line breaks.
        val contentWidth = key.widthPx - view.compoundPaddingLeft - view.compoundPaddingRight
        val contentHeight = key.heightPx - view.compoundPaddingTop - view.compoundPaddingBottom - shadowInset
        if (contentWidth <= 0 || contentHeight <= 0) return 0f

        val phrases = phrases(key.languageCode)
        fun fitsEveryPhrase(size: Float): Boolean = phrases.all { phrase ->
            checkActive()
            measure(view, phrase.text, size, key)
            val layout = checkNotNull(view.layout)
            (!key.singleLine || layout.lineCount == 1) && layout.height <= contentHeight &&
                (0 until layout.lineCount).all { line ->
                    val end = layout.getLineEnd(line)
                    val wholeWords = end in phrase.breaks
                    wholeWords && layout.getLineMax(line) <= contentWidth
                }
        }

        // Word widths provide a monotonic upper bound. Full line layout does not: a
        // slightly smaller font can move an extra word onto a nearly full line. Searching
        // that predicate with binary search can incorrectly settle on a much smaller size.
        val unwrappedParts = if (key.singleLine) phrases.map { it.text } else phrases.flatMap { phrase ->
            phrase.breaks.sorted().zipWithNext { start, end -> phrase.text.substring(start, end).trim() }
        }.distinct()
        fun fitsUnwrappedParts(size: Int): Boolean {
            checkActive()
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
            val metrics = view.paint.fontMetricsInt
            return metrics.descent - metrics.ascent <= contentHeight &&
                unwrappedParts.all { view.paint.measureText(it) <= contentWidth }
        }
        var lower = 0
        var upper = key.heightPx * 2
        while (lower < upper) {
            val candidate = (lower + upper + 1) / 2
            if (fitsUnwrappedParts(candidate)) lower = candidate else upper = candidate - 1
        }
        // Descend from the upper bound to find the largest actual layout that fits every
        // phrase, without assuming that line wrapping behaves monotonically.
        var largestFit = lower.toFloat()
        while (largestFit > 0f && !fitsEveryPhrase(largestFit)) largestFit -= 1f
        val preferredMinimum = context.resources.getDimension(R.dimen.widget_text_size)
        val breathingRoom = context.resources.getDimension(R.dimen.widget_text_margin)
        var result = if (largestFit < preferredMinimum) largestFit
        else (largestFit - breathingRoom).coerceAtLeast(preferredMinimum)
        result *= key.sizePercent / 100f
        // Fractional sizes can rewrap differently. Check all phrases after the 2sp margin,
        // so a later minute never needs its own, smaller font size.
        while (result > 0f && !fitsEveryPhrase(result)) result = (result - 1f).coerceAtLeast(0f)
        return result
    }

    private fun measure(view: TextView, text: String, size: Float, key: CacheKey) {
        view.textDirection = View.TEXT_DIRECTION_FIRST_STRONG
        view.layoutDirection = android.text.TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(key.languageCode))
        view.text = text
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(key.widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(key.heightPx, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, key.widthPx, key.heightPx)
    }
}
