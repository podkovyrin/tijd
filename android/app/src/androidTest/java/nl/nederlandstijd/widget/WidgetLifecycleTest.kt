package nl.nederlandstijd.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.os.SystemClock
import android.util.SizeF
import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetLifecycleTest {
    @Test
    fun responsiveHostReceivesFittedLayoutsAndActualMinuteAlarm() = checkHost(singleLine = false)

    @Test
    fun rowHostReceivesSingleLineLayoutsAndActualMinuteAlarm() = checkHost(singleLine = true)

    @Test
    fun singleLayoutHostReceivesPartialUpdatesAndSurvivesHostRecreation() = checkHost(singleLine = false, responsive = false)

    private fun checkHost(singleLine: Boolean, responsive: Boolean = true) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val manager = AppWidgetManager.getInstance(context)
        val host = AppWidgetHost(context, 701)
        var id = AppWidgetManager.INVALID_APPWIDGET_ID
        val smallWidth = if (singleLine) 220f else 110f
        val largeWidth = if (singleLine) 350f else 250f
        val heightDp = if (singleLine) 60f else 110f
        var widthDp = smallWidth
        lateinit var view: AppWidgetHostView
        try {
            instrumentation.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
            instrumentation.uiAutomation.executeShellCommand(
                "appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow",
            ).close()
            instrumentation.runOnMainSync {
                id = host.allocateAppWidgetId()
                val options = Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, smallWidth.toInt())
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, smallWidth.toInt())
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp.toInt())
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp.toInt())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && responsive) {
                        putParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES, arrayListOf(
                            SizeF(smallWidth, heightDp), SizeF(largeWidth, heightDp),
                        ))
                    }
                }
                assertTrue(manager.bindAppWidgetIdIfAllowed(
                    id, ComponentName(context, if (singleLine) DutchTimeRowWidgetReceiver::class.java else DutchTimeWidgetReceiver::class.java), options,
                ))
                view = host.createView(context, id, manager.getAppWidgetInfo(id))
                host.startListening()
            }

            fun displayedText(): String? {
                var result: String? = null
                instrumentation.runOnMainSync {
                    val density = context.resources.displayMetrics.density
                    val width = (widthDp * density).toInt() + view.paddingLeft + view.paddingRight
                    val height = (heightDp * density).toInt() + view.paddingTop + view.paddingBottom
                    view.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
                    )
                    view.layout(0, 0, width, height)
                    result = view.findViewById<TextView>(R.id.clock_text)?.text?.toString()
                }
                return result
            }

            fun awaitText(description: String, timeout: Long = 20_000, predicate: (String?) -> Boolean) {
                val deadline = SystemClock.elapsedRealtime() + timeout
                do {
                    if (predicate(displayedText())) return
                    SystemClock.sleep(100)
                } while (SystemClock.elapsedRealtime() < deadline)
                throw AssertionError("$description; displayed: ${displayedText()}")
            }

            awaitText("The provider did not render the current time") { it == ClockViews.currentText() }
            manager.updateAppWidget(id, ClockViews.forWidget(context, manager.getAppWidgetOptions(id), "stale", singleLine = singleLine))
            awaitText("The host rejected a fitted layout") { it == "stale" }

            context.sendBroadcast(Intent(context, ClockTickReceiver::class.java).setAction(MinuteScheduler.TICK_ACTION))
            awaitText("The tick did not replace the stale text") { it == ClockViews.currentText() }
            assertTrue(MinuteScheduler.canSchedule(context))
            val previous = displayedText()
            var previousSize = 0f
            instrumentation.runOnMainSync { previousSize = view.findViewById<TextView>(R.id.clock_text).textSize }
            awaitText("The actual minute alarm did not update the host", timeout = 70_000) {
                it != previous && it == ClockViews.currentText()
            }
            instrumentation.runOnMainSync {
                val text = checkNotNull(view.findViewById<TextView>(R.id.clock_text))
                assertEquals("The minute tick changed the font size", previousSize, text.textSize, 0f)
                val layout = checkNotNull(text.layout)
                assertTrue(layout.height <= text.height - text.compoundPaddingTop - text.compoundPaddingBottom)
                if (singleLine) assertTrue(layout.lineCount == 1)
                for (line in 0 until layout.lineCount) {
                    val end = layout.getLineEnd(line)
                    assertTrue(end == text.length() || text.text[end - 1] == ' ' || text.text[end] == ' ')
                }
            }
            // Recreate the host from the system's cached RemoteViews, not the last live callback.
            instrumentation.runOnMainSync {
                host.stopListening()
                view = host.createView(context, id, manager.getAppWidgetInfo(id))
                host.startListening()
            }
            awaitText("The cached widget lost the latest time after host recreation") { it == ClockViews.currentText() }
            instrumentation.runOnMainSync {
                val text = view.findViewById<TextView>(R.id.clock_text)
                assertEquals(previousSize, text.textSize, 0f)
                assertTrue("A partial update lost the Clock action", text.hasOnClickListeners())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && responsive) {
                var smallFontSize = 0f
                instrumentation.runOnMainSync {
                    smallFontSize = view.findViewById<TextView>(R.id.clock_text).textSize
                    widthDp = largeWidth
                }
                awaitText("The wider cached layout did not grow or preserve its font") {
                    var grew = false
                    instrumentation.runOnMainSync {
                        val size = view.findViewById<TextView>(R.id.clock_text).textSize
                        grew = size >= smallFontSize
                    }
                    grew && it == ClockViews.currentText()
                }
                instrumentation.runOnMainSync { widthDp = smallWidth }
                awaitText("Returning to the small layout did not restore its font size") {
                    var shrank = false
                    instrumentation.runOnMainSync {
                        shrank = view.findViewById<TextView>(R.id.clock_text).textSize <= smallFontSize
                    }
                    shrank && it == ClockViews.currentText()
                }
            }
        } finally {
            instrumentation.runOnMainSync {
                host.stopListening()
                if (id != AppWidgetManager.INVALID_APPWIDGET_ID) host.deleteAppWidgetId(id)
                host.deleteHost()
            }
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }
}
