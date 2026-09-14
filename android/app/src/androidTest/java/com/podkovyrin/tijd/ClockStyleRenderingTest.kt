package com.podkovyrin.tijd

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClockStyleRenderingTest {
    @Test fun nativeFontsAndScaledSizesFitEveryPhrase() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val phrases = (0..11).flatMap { hour -> (0..59).map { SpokenTime.format("nl", hour, it) } }.distinct()
        for ((font, percent) in listOf("serif" to 85, "mono" to 70)) {
            val choice = ClockFont.available().first { it.id == font }
            val style = WidgetStyle(fontId = font, sizePercent = percent, languageCode = "nl")
            val density = context.resources.displayMetrics.density
            instrumentation.runOnMainSync {
                val view = ClockViews.create(context, 110f, 110f, phrases.first(), style = style)
                    .apply(context, FrameLayout(context)) as TextView
                assertEquals(choice.typeface, view.typeface)
                val size = view.textSize
                for (phrase in phrases) {
                    ClockViews.create(context, 110f, 110f, phrase, partial = true, style = style).reapply(context, view)
                    val spec = View.MeasureSpec.makeMeasureSpec((110 * density).toInt(), View.MeasureSpec.EXACTLY)
                    view.measure(spec, spec)
                    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                    assertEquals(size, view.textSize, 0f)
                    val layout = checkNotNull(view.layout)
                    assertTrue(layout.height <= view.height - view.compoundPaddingTop - view.compoundPaddingBottom)
                    for (line in 0 until layout.lineCount) {
                        val end = layout.getLineEnd(line)
                        assertTrue("$font $percent: $phrase", end == phrase.length || phrase[end - 1] == ' ' || phrase[end] == ' ')
                        assertTrue(layout.getLineMax(line) <= view.width - view.compoundPaddingLeft - view.compoundPaddingRight)
                    }
                }
            }
        }
    }

    @Test fun automaticTextContrastsWithPanelAndExplicitTextChoosesShadow() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = instrumentation.targetContext
            fun render(style: WidgetStyle, ink: ClockInk = ClockInk.Light) =
                ClockViews.create(context, 250f, 110f, ink = ink, style = style)
                    .apply(context, FrameLayout(context)) as TextView
            assertEquals(Color.BLACK, render(WidgetStyle(backgroundId = "white", backgroundOpacity = 100)).currentTextColor)
            assertEquals(Color.WHITE, render(WidgetStyle(backgroundId = "black", backgroundOpacity = 100), ClockInk.Dark).currentTextColor)
            assertEquals(Color.BLACK, render(WidgetStyle(backgroundId = "white", backgroundOpacity = 60)).currentTextColor)
            assertEquals(Color.WHITE, render(WidgetStyle(backgroundId = "white", backgroundOpacity = 0)).currentTextColor)
            assertEquals(0x80ffffff.toInt(), render(WidgetStyle(colorId = "black")).shadowColor)
            assertEquals(0x80000000.toInt(), render(WidgetStyle(colorId = "white"), ClockInk.Dark).shadowColor)
        }
    }

    @Test fun styleSurvivesPartialUpdateAndResetsOnFullUpdate() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = instrumentation.targetContext
            val color = StyleCatalog.colors.first()
            val style = WidgetStyle(colorId = color.id, backgroundId = color.id, alignment = WidgetAlignment.END, languageCode = "nl")
            val view = ClockViews.create(context, 250f, 110f, style = style).apply(context, FrameLayout(context)) as TextView
            assertEquals(color.argb, view.currentTextColor)
            ClockViews.create(context, 250f, 110f, "negen uur", partial = true, style = style).reapply(context, view)
            assertEquals(color.argb, view.currentTextColor)
            assertEquals(Gravity.RIGHT or Gravity.CENTER_VERTICAL, view.gravity)
            ClockViews.create(context, 250f, 110f).reapply(context, view)
            assertEquals(Color.WHITE, view.currentTextColor)
            if (android.os.Build.VERSION.SDK_INT >= 31) assertEquals(Color.TRANSPARENT, view.backgroundTintList?.defaultColor)
            else assertEquals(Color.TRANSPARENT, (view.background as android.graphics.drawable.ColorDrawable).color)
        }
    }
}
