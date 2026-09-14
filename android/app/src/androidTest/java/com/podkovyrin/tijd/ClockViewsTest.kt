package com.podkovyrin.tijd

import android.content.res.Configuration
import android.app.WallpaperColors
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import android.util.TypedValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClockViewsTest {
    @Test
    fun textFitsSmallAndWideWidgetsWithSystemFontScaling() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        for ((fontScale, densityDpi) in listOf(1f to 390, 2f to 390, 1f to 420, 2f to 420)) {
            val configuration = Configuration(instrumentation.targetContext.resources.configuration)
            configuration.fontScale = fontScale
            configuration.densityDpi = densityDpi
            val context = instrumentation.targetContext.createConfigurationContext(configuration)
            for ((width, height, singleLine) in listOf(Triple(110, 110, false), Triple(250, 110, false), Triple(110, 40, false), Triple(350, 200, false), Triple(350, 60, true), Triple(220, 60, true))) {
                val density = context.resources.displayMetrics.density
                val phrases = (0..11).flatMap { hour ->
                    (0..59).map { minute -> SpokenTime.format("nl", hour, minute) }
                }.distinct()
                var stableSize: Float? = null
                for (phrase in phrases) {
                    instrumentation.runOnMainSync {
                        val root = ClockViews.create(context, width.toFloat(), height.toFloat(), phrase, singleLine = singleLine, style = WidgetStyle(languageCode = "nl"))
                            .apply(context, FrameLayout(context))
                        val text = root.findViewById<TextView>(R.id.clock_text)
                        root.measure(
                            View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec((height * density).toInt(), View.MeasureSpec.EXACTLY),
                        )
                        root.layout(0, 0, root.measuredWidth, root.measuredHeight)
                        stableSize?.let { assertEquals("Font changed for $phrase in $width x $height", it, text.textSize, 0f) }
                        stableSize = text.textSize
                        val layout = checkNotNull(text.layout)
                        assertEquals(phrase.length, layout.getLineEnd(layout.lineCount - 1))
                        val contentWidth = text.width - text.compoundPaddingLeft - text.compoundPaddingRight
                        val contentHeight = text.height - text.compoundPaddingTop - text.compoundPaddingBottom
                        assertTrue("$phrase in $width x $height at font scale $fontScale", layout.height <= contentHeight)
                        assertTextPosition(text)
                        if (singleLine) assertEquals(1, layout.lineCount)
                        if (width == 350 && height == 200) {
                            assertTrue(text.textSize >= context.resources.getDimension(R.dimen.widget_text_size))
                        }
                        val padding = context.resources.getDimensionPixelSize(R.dimen.widget_padding)
                        assertEquals(padding, text.paddingLeft)
                        assertEquals(padding, text.paddingTop)
                        assertEquals(padding, text.paddingRight)
                        assertEquals(padding, text.paddingBottom)
                        for (line in 0 until layout.lineCount) {
                            val end = layout.getLineEnd(line)
                            assertTrue(
                                "$phrase breaks inside a word at character $end in $width x $height",
                                end == phrase.length || phrase[end - 1] == ' ' || phrase[end] == ' ',
                            )
                            assertEquals(0, layout.getEllipsisCount(line))
                            assertTrue(
                                "$phrase at $width x $height: visible line width ${layout.getLineMax(line)} exceeds $contentWidth",
                                layout.getLineMax(line) <= contentWidth,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun alignmentChangesWhenAWidgetIsReusedForDifferentLineCounts() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = instrumentation.targetContext
            val density = context.resources.displayMetrics.density
            for (ink in ClockInk.entries) {
                val view = ClockViews.create(context, 350f, 60f, "negen uur", ink, style = WidgetStyle(languageCode = "nl"))
                    .apply(context, FrameLayout(context)) as TextView
                for ((width, height, phrase) in listOf(
                    Triple(350, 60, "negen uur"),
                    Triple(110, 110, "negentien over twaalf"),
                    Triple(350, 60, "kwart voor negen"),
                )) {
                    ClockViews.create(context, width.toFloat(), height.toFloat(), phrase, ink, style = WidgetStyle(languageCode = "nl")).reapply(context, view)
                    view.measure(
                        View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec((height * density).toInt(), View.MeasureSpec.EXACTLY),
                    )
                    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                    assertTrue("$phrase in $width x $height: ${view.lineCount} lines at ${view.textSize}px", if (width == 110) view.lineCount > 1 else view.lineCount == 1)
                    assertTextPosition(view)
                }
            }
        }
    }

    private fun assertTextPosition(text: TextView) {
        val layout = checkNotNull(text.layout)
        val layoutTop = text.baseline - layout.getLineBaseline(0)
        if (layout.lineCount == 1) {
            val lineCenter = text.compoundPaddingLeft + (layout.getLineLeft(0) + layout.getLineRight(0)) / 2f
            assertEquals("Single-line horizontal center", text.width / 2f, lineCenter, 1f)
            assertEquals("Single-line vertical center", text.height / 2f, layoutTop + layout.height / 2f, 1f)
        } else {
            assertEquals("Multiline top", text.compoundPaddingTop, layoutTop)
            for (line in 0 until layout.lineCount) assertEquals("Multiline left", 0f, layout.getLineLeft(line), 0.01f)
        }
    }

    @Test
    fun fontGrowsWithAvailableSpaceAndLeavesTwoSpOfBreathingRoom() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val context = instrumentation.targetContext
            val density = context.resources.displayMetrics.density
            fun render(width: Int, height: Int): TextView {
                val view = ClockViews.create(context, width.toFloat(), height.toFloat(), "negentien over twaalf", style = WidgetStyle(languageCode = "nl"))
                    .apply(context, FrameLayout(context)) as TextView
                view.measure(
                    View.MeasureSpec.makeMeasureSpec((width * density).toInt(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec((height * density).toInt(), View.MeasureSpec.EXACTLY),
                )
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                return view
            }
            val small = render(110, 110)
            val large = render(350, 200)
            val minimum = context.resources.getDimension(R.dimen.widget_text_size)
            assertTrue(large.textSize > small.textSize)
            assertTrue(large.textSize > minimum)
            val fittedSize = large.textSize
            // Restoring the 2sp margin should still fit, proving this isn't another fixed cap.
            val margin = context.resources.getDimension(R.dimen.widget_text_margin)
            large.setTextSize(TypedValue.COMPLEX_UNIT_PX, fittedSize + margin)
            large.measure(
                View.MeasureSpec.makeMeasureSpec((350 * density).toInt(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec((200 * density).toInt(), View.MeasureSpec.EXACTLY),
            )
            large.layout(0, 0, large.measuredWidth, large.measuredHeight)
            val layout = checkNotNull(large.layout)
            assertTrue(layout.height <= large.height - large.compoundPaddingTop - large.compoundPaddingBottom)
            for (line in 0 until layout.lineCount) {
                val end = layout.getLineEnd(line)
                assertTrue(end == large.length() || large.text[end - 1] == ' ' || large.text[end] == ' ')
                assertTrue(layout.getLineMax(line) <= large.width - large.compoundPaddingLeft - large.compoundPaddingRight)
            }
        }
    }

    @Test
    fun wallpaperHintsChooseContrastingInkAndBothStylesHaveShadows() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bright = WallpaperColors(Color.valueOf(Color.WHITE), null, null, WallpaperColors.HINT_SUPPORTS_DARK_TEXT)
            val dark = WallpaperColors(Color.valueOf(Color.BLACK), null, null, WallpaperColors.HINT_SUPPORTS_DARK_THEME)
            assertEquals(ClockInk.Dark, WallpaperAppearance.fromColors(bright))
            assertEquals(ClockInk.Light, WallpaperAppearance.fromColors(dark))
            assertEquals(ClockInk.Light, WallpaperAppearance.fromColors(null))
        }
        instrumentation.runOnMainSync {
            for (ink in ClockInk.entries) {
                val context = instrumentation.targetContext
                val root = ClockViews.create(context, 250f, 110f, "kwart voor twaalf", ink, style = WidgetStyle(languageCode = "nl"))
                    .apply(context, FrameLayout(context)) as TextView
                assertEquals(if (ink == ClockInk.Light) Color.WHITE else Color.BLACK, root.currentTextColor)
                assertTrue(root.shadowRadius > 0)
                assertTrue(Color.alpha(root.shadowColor) in 1..254)
            }
        }
    }
}
