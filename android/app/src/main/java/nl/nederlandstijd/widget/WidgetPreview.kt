package nl.nederlandstijd.widget

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
        setBackgroundColor(Color.rgb(43, 53, 59))
        minimumHeight = dp(120)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun show(style: WidgetStyle, options: Bundle, singleLine: Boolean, phrase: String = ClockViews.currentText()) {
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
                val native = views.apply(context, this@WidgetPreview)
                native.findViewById<TextView>(R.id.clock_text).isClickable = false
                val nativeWidth = dp(size.width)
                val nativeHeight = dp(size.height)
                val scale = min(1f, min(width.toFloat() / nativeWidth, height.toFloat() / nativeHeight))
                native.scaleX = scale
                native.scaleY = scale
                removeAllViews()
                addView(native, LayoutParams(nativeWidth, nativeHeight, Gravity.CENTER))
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: RuntimeException) {
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
