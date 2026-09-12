package nl.nederlandstijd.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Emulator-only, staged by adb so the process can be killed between prepare and verify. */
@RunWith(AndroidJUnit4::class)
class ClockRecoveryTest {
    @Test
    fun stagedProcessAndSleepRecovery() {
        val phase = InstrumentationRegistry.getArguments().getString("recoveryPhase")
        assumeTrue("Run explicitly with -e recoveryPhase prepare/verify/cleanup", phase != null)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val host = AppWidgetHost(context, 709)
        val manager = AppWidgetManager.getInstance(context)
        val preferences = context.getSharedPreferences("recovery_test", Context.MODE_PRIVATE)
        if (phase == "cleanup") {
            host.deleteHost()
            preferences.edit().clear().commit()
            MinuteScheduler.scheduleNext(context)
            return
        }
        if (phase == "prepare") {
            instrumentation.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
            try {
                host.deleteHost()
                val id = host.allocateAppWidgetId()
                val options = Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 157)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 157)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 113)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 113)
                }
                assertTrue(manager.bindAppWidgetIdIfAllowed(id, ComponentName(context, DutchTimeWidgetReceiver::class.java), options))
                runBlocking { ClockUpdates.refresh(context, forceFull = true) }
                MinuteScheduler.scheduleNext(context)
                preferences.edit().putInt("id", id).commit()
            } finally {
                instrumentation.uiAutomation.dropShellPermissionIdentity()
            }
        } else {
            assertEquals("verify", phase)
            val id = preferences.getInt("id", -1)
            assertTrue("Missing recovery fixture", id >= 0)
            var shown: String? = null
            instrumentation.runOnMainSync {
                val view = host.createView(context, id, manager.getAppWidgetInfo(id))
                host.startListening()
                // The first snapshot must already be current: no test-triggered refresh or wait.
                val width = (157 * context.resources.displayMetrics.density).toInt() + view.paddingLeft + view.paddingRight
                val height = (113 * context.resources.displayMetrics.density).toInt() + view.paddingTop + view.paddingBottom
                view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
                view.layout(0, 0, width, height)
                shown = view.findViewById<TextView>(R.id.clock_text)?.text?.toString()
                host.stopListening()
            }
            assertEquals("The host's cached time did not recover", ClockViews.currentText(), shown)
        }
    }
}
