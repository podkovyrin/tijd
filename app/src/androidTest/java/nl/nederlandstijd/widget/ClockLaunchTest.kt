package nl.nederlandstijd.widget

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.SystemClock
import android.provider.AlarmClock
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClockLaunchTest {
    @Test
    fun tappingTheWidgetOpensTheClockAlarmsPage() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        // Intercept the outgoing show action so the test never touches a user's alarms.
        val monitor = object : Instrumentation.ActivityMonitor() {
            override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? =
                if (intent.action == AlarmClock.ACTION_SHOW_ALARMS) {
                    Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
                } else null
        }
        instrumentation.addMonitor(monitor)
        var activity: Activity? = null
        try {
            activity = instrumentation.startActivitySync(
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            instrumentation.runOnMainSync {
                ClockViews.create(context, 350f, 60f, "negen uur")
                    .apply(activity, FrameLayout(activity)).performClick()
            }
            val deadline = SystemClock.elapsedRealtime() + 5_000
            while (monitor.hits == 0 && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(50)
            assertEquals(1, monitor.hits)
        } finally {
            instrumentation.removeMonitor(monitor)
            instrumentation.runOnMainSync { activity?.finish() }
        }
    }
}
