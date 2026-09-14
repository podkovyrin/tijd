package nl.nederlandstijd.widget

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in release captures. All fixtures live in the test APK; production screens are unchanged. */
@RunWith(AndroidJUnit4::class)
class StoreScreenshotTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun captureMainAndEditor() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue("Run through scripts/android_screenshots.py", args.getString("screenshots") == "true")
        val locale = requireNotNull(args.getString("locale"))
        val language = requireNotNull(args.getString("timeCode"))
        val captureKey = requireNotNull(args.getString("captureKey"))
        val host = AppWidgetHost(context, 709)
        val manager = AppWidgetManager.getInstance(context)
        val store = WidgetStyleStore(context)
        val options = Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 200)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 200)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 140)
        }
        val style = WidgetStyle(languageCode = language)
        val phrase = SpokenTime.format(language, 10, 10)
        instrumentation.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
        host.deleteHost()
        val id = host.allocateAppWidgetId()
        try {
            assertTrue(manager.bindAppWidgetIdIfAllowed(
                id, ComponentName(context, DutchTimeWidgetReceiver::class.java), options,
            ))
            assertTrue(store.save(id, style))
            val directory = File(context.getExternalFilesDir(null), "screenshots/$captureKey").apply { mkdirs() }
            fun <T : Activity> capture(scenario: ActivityScenario<T>, name: String, heading: Int) {
                scenario.use {
                    instrumentation.waitForIdleSync()
                    scenario.onActivity { activity ->
                        assertEquals(locale, activity.resources.configuration.locales[0].toLanguageTag())
                        assertTrue(views(activity.window.decorView).filterIsInstance<TextView>()
                            .any { it.text.toString() == activity.getString(heading) })
                        views(activity.window.decorView).filterIsInstance<WidgetPreview>().single()
                            .show(style, options, false, phrase)
                    }
                    val deadline = SystemClock.uptimeMillis() + 30_000
                    var ready = false
                    while (!ready && SystemClock.uptimeMillis() < deadline) {
                        instrumentation.waitForIdleSync()
                        scenario.onActivity { activity ->
                            val preview = views(activity.window.decorView).filterIsInstance<WidgetPreview>().single()
                            ready = views(preview).filterIsInstance<TextView>().any { it.text.toString() == phrase }
                        }
                        if (!ready) SystemClock.sleep(100)
                    }
                    assertTrue("Preview did not render for $locale", ready)
                    // Allow the completed native view to reach the compositor.
                    SystemClock.sleep(350)
                    val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
                    try {
                        assertEquals(1080, bitmap.width)
                        assertEquals(2400, bitmap.height)
                        File(directory, name).outputStream().use { output ->
                            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                        }
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
            capture(ActivityScenario.launch(MainActivity::class.java), "01-main.png", R.string.welcome_title)
            capture(ActivityScenario.launch<WidgetConfigurationActivity>(
                Intent(context, WidgetConfigurationActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
            ), "02-edit-widget.png", R.string.edit_widget_title)
        } finally {
            host.deleteHost()
            store.delete(intArrayOf(id))
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }

    private fun views(view: View): List<View> = listOf(view) + if (view is ViewGroup) {
        (0 until view.childCount).flatMap { views(view.getChildAt(it)) }
    } else emptyList()
}
