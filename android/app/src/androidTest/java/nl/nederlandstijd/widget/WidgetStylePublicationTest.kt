package nl.nederlandstijd.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetStylePublicationTest {
    @Test
    fun ordinaryRefreshPublishesChangedStyleAndPreservesOtherInstances() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val manager = AppWidgetManager.getInstance(context)
        val store = WidgetStyleStore(context)
        val host = AppWidgetHost(context, 704)
        val ids = mutableListOf<Int>()
        val views = mutableListOf<AppWidgetHostView>()
        val initial = WidgetStyle(colorId = "navy", fontId = "sans", backgroundId = "ivory", backgroundOpacity = 100)
        val edited = WidgetStyle(colorId = "rose", fontId = "serif", backgroundId = "forest", backgroundOpacity = 100)
        val options = Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 240)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 240)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 110)
        }

        fun awaitCondition(description: String, condition: () -> Boolean) {
            val deadline = SystemClock.elapsedRealtime() + 20_000
            do {
                var matched = false
                instrumentation.runOnMainSync { matched = condition() }
                if (matched) return
                SystemClock.sleep(100)
            } while (SystemClock.elapsedRealtime() < deadline)
            throw AssertionError(description)
        }

        fun text(index: Int): TextView? {
            val view = views[index]
            val density = context.resources.displayMetrics.density
            val width = (240 * density).toInt() + view.paddingLeft + view.paddingRight
            val height = (110 * density).toInt() + view.paddingTop + view.paddingBottom
            view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, width, height)
            return view.findViewById(R.id.clock_text)
        }

        fun background(text: TextView): Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            text.backgroundTintList?.defaultColor
        } else {
            (text.background as? ColorDrawable)?.color
        }

        fun matches(index: Int, style: WidgetStyle): Boolean {
            val text = text(index) ?: return false
            val typeface = if (style.fontId == "serif") Typeface.create("serif", Typeface.NORMAL)
                else Typeface.create("sans-serif-medium", Typeface.NORMAL)
            return text.text.toString() == ClockViews.currentText() && text.typeface == typeface &&
                text.currentTextColor == StyleCatalog.color(style.colorId)?.argb &&
                background(text) == StyleCatalog.color(style.backgroundId)?.argb
        }

        try {
            instrumentation.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
            instrumentation.runOnMainSync {
                repeat(2) {
                    val id = host.allocateAppWidgetId()
                    ids.add(id)
                    assertTrue(store.save(id, initial))
                    assertTrue(manager.bindAppWidgetIdIfAllowed(id,
                        ComponentName(context, DutchTimeWidgetReceiver::class.java), options))
                    views.add(host.createView(context, id, manager.getAppWidgetInfo(id)))
                }
                host.startListening()
            }
            runBlocking { ClockUpdates.refresh(context) }
            awaitCondition("Both widgets should start with the chosen style") { matches(0, initial) && matches(1, initial) }

            assertTrue(store.save(ids[0], edited))
            // No forceFull request: the publisher must notice the changed style itself.
            runBlocking { ClockUpdates.refresh(context, forceFull = false) }
            awaitCondition("Style change was not published, or affected another widget") {
                matches(0, edited) && matches(1, initial)
            }
            var size = 0f
            instrumentation.runOnMainSync { size = checkNotNull(text(0)).textSize }

            manager.partiallyUpdateAppWidget(ids[0], RemoteViews(context.packageName,
                ClockFont.layoutFor(ClockInk.Light, edited.fontId)).apply {
                setTextViewText(R.id.clock_text, "stale")
            })
            awaitCondition("The host did not receive the stale text used to verify the next update") {
                text(0)?.text?.toString() == "stale"
            }
            runBlocking { ClockUpdates.refresh(context, forceFull = false) }
            awaitCondition("A subsequent update lost a widget style") { matches(0, edited) && matches(1, initial) }
            instrumentation.runOnMainSync {
                assertEquals(size, checkNotNull(text(0)).textSize, 0f)
                assertTrue(checkNotNull(text(0)).hasOnClickListeners())
                host.deleteAppWidgetId(ids[0])
            }
            awaitCondition("Deleting the hosted widget did not remove its saved style") { !store.contains(ids[0]) }
            assertEquals(initial, store.read(ids[1]))
        } finally {
            instrumentation.runOnMainSync {
                host.stopListening()
                host.deleteHost()
            }
            store.delete(ids.toIntArray())
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }
}
