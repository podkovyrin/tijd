package com.podkovyrin.tijd

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/** A scaled copy of the native widget, using the same geometry and renderer as its host. */
@SuppressLint("ViewConstructor") // Created programmatically with its owner’s lifecycle scope.
internal class WidgetPreview(context: Context, private val scope: CoroutineScope) : FrameLayout(context) {
    private var renderJob: Job? = null
    private var style = WidgetStyle()
    private var options = Bundle()
    private var singleLine = false
    private var phrase = ClockViews.currentText()

    init {
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.rgb(43, 53, 59))
            cornerRadius = dp(18).toFloat()
        }
        clipToOutline = true
        minimumHeight = dp(120)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun show(style: WidgetStyle, options: Bundle, singleLine: Boolean, phrase: String = ClockViews.currentText(style.languageCode)) {
        this.style = style
        this.options = Bundle(options)
        this.singleLine = singleLine
        this.phrase = phrase
        render()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) render()
    }

    private fun render() {
        renderJob?.cancel()
        if (width <= 0 || height <= 0) return
        val selectedStyle = style
        val selectedOptions = Bundle(options)
        val selectedPhrase = phrase
        val row = singleLine
        renderJob = scope.launch {
            try {
                val size = ClockViews.geometry(selectedOptions).sizes.first()
                val views = withContext(Dispatchers.IO) {
                    val ink = WallpaperAppearance.resolve(context, selectedOptions)
                    val coroutine = coroutineContext
                    ClockViews.prepare(context, ClockViews.Geometry(listOf(size), false), ink, row, selectedStyle) {
                        coroutine.ensureActive()
                    }
                    ClockViews.create(context, size.width, size.height, selectedPhrase, ink, row, style = selectedStyle)
                }
                // Use the host-like application inflater. AppCompat's activity inflater would
                // substitute MaterialTextView, which RemoteViews deliberately does not allow.
                val native = views.apply(context.applicationContext, this@WidgetPreview)
                fun disableInteractions(view: android.view.View) {
                    view.isClickable = false
                    view.isFocusable = false
                    if (view is android.view.ViewGroup) {
                        for (index in 0 until view.childCount) disableInteractions(view.getChildAt(index))
                    }
                }
                disableInteractions(native)
                val nativeWidth = dp(size.width)
                val nativeHeight = dp(size.height)
                val scale = min(1f, min(width.toFloat() / nativeWidth, height.toFloat() / nativeHeight))
                native.scaleX = scale
                native.scaleY = scale
                removeAllViews()
                addView(native, LayoutParams(nativeWidth, nativeHeight, Gravity.CENTER))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: RuntimeException) {
                android.util.Log.e("WidgetPreview", "Unable to render widget preview", exception)
                removeAllViews()
                addView(TextView(context).apply {
                    setText(R.string.preview_unavailable)
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
        }
    }

    override fun onDetachedFromWindow() {
        renderJob?.cancel()
        super.onDetachedFromWindow()
    }

    private fun dp(value: Int) = dp(value.toFloat())
    private fun dp(value: Float) = (value * resources.displayMetrics.density).toInt().coerceAtLeast(1)
}
