package nl.nederlandstijd.widget

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercise real configuration lifecycle and persistence against two bound widget instances. */
@RunWith(AndroidJUnit4::class)
class WidgetConfigurationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val manager = AppWidgetManager.getInstance(context)
    private val host = AppWidgetHost(context, 703)
    private val store = WidgetStyleStore(context)
    private val ids = mutableListOf<Int>()
    private lateinit var previousDefaults: WidgetStyle
    private val original = WidgetStyle(colorId = "rose", fontId = "serif")
    private val other = WidgetStyle(colorId = "teal", fontId = "mono")

    @Before
    fun bindWidgets() {
        previousDefaults = store.defaults()
        instrumentation.uiAutomation.adoptShellPermissionIdentity("android.permission.BIND_APPWIDGET")
        repeat(2) {
            val id = host.allocateAppWidgetId()
            ids += id
            assertTrue(manager.bindAppWidgetIdIfAllowed(
                id, ComponentName(context, DutchTimeWidgetReceiver::class.java), Bundle().apply {
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 110)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
                    putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 110)
                },
            ))
        }
        assertTrue(store.save(ids[0], original))
        assertTrue(store.save(ids[1], other))
    }

    @After
    fun cleanUp() {
        host.deleteHost()
        store.delete(ids.toIntArray())
        if (::previousDefaults.isInitialized) store.saveDefaults(previousDefaults)
        instrumentation.uiAutomation.dropShellPermissionIdentity()
    }

    @Test
    fun cancelLeavesWidgetAndDefaultsUnchanged() {
        ActivityScenario.launchActivityForResult<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity {
                select(it, R.string.text_color, "cobalt")
                views(it).filterIsInstance<CheckBox>().single().isChecked = true
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity { click(it, android.R.string.cancel) }
            assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
        }
        assertEquals(original, store.read(ids[0]))
        assertEquals(other, store.read(ids[1]))
        assertEquals(previousDefaults, store.defaults())
    }

    @Test
    fun draftSurvivesRecreationAndSaveOnlyChangesSelectedWidget() {
        ActivityScenario.launchActivityForResult<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity {
                select(it, R.string.text_color, "cobalt")
                select(it, R.string.text_size, "85")
                select(it, R.string.background, "ivory")
                select(it, R.string.preview_phrase, "long")
                views(it).filterIsInstance<CheckBox>().single().isChecked = true
            }
            instrumentation.waitForIdleSync()
            assertEquals("Draft edits must not persist before Save", original, store.read(ids[0]))
            scenario.recreate()
            scenario.onActivity {
                assertEquals("cobalt", selected(it, R.string.text_color))
                assertEquals("85", selected(it, R.string.text_size))
                assertEquals("ivory", selected(it, R.string.background))
                assertEquals("long", selected(it, R.string.preview_phrase))
                assertTrue(views(it).filterIsInstance<CheckBox>().single().isChecked)
                // This Save should leave future-widget defaults untouched.
                views(it).filterIsInstance<CheckBox>().single().isChecked = false
                click(it, R.string.save_style)
            }
            val result = scenario.result
            assertEquals(Activity.RESULT_OK, result.resultCode)
            assertEquals(ids[0], result.resultData.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
        }
        assertEquals(original.copy(colorId = "cobalt", sizePercent = 85, backgroundId = "ivory"), store.read(ids[0]))
        assertEquals(other, store.read(ids[1]))
        assertEquals(previousDefaults, store.defaults())
    }

    @Test
    fun exportedConfigurationRejectsAnInvalidWidgetId() {
        val invalidId = AppWidgetManager.INVALID_APPWIDGET_ID
        assertFalse(store.contains(invalidId))
        ActivityScenario.launchActivityForResult<WidgetConfigurationActivity>(editorIntent(invalidId)).use { scenario ->
            assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
        }
        assertFalse(store.contains(invalidId))
        assertEquals(previousDefaults, store.defaults())
    }

    private fun editorIntent(id: Int) = Intent(context, WidgetConfigurationActivity::class.java)
        .setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)

    private fun views(activity: Activity): List<View> {
        fun collect(view: View): List<View> = listOf(view) +
            if (view is ViewGroup) (0 until view.childCount).flatMap { collect(view.getChildAt(it)) } else emptyList()
        return collect(activity.window.decorView)
    }

    private fun spinner(activity: Activity, label: Int) = views(activity).filterIsInstance<Spinner>()
        .single { it.contentDescription == activity.getString(label) }

    private fun select(activity: Activity, label: Int, option: String) {
        val spinner = spinner(activity, label)
        val index = (0 until spinner.count).first { (spinner.getItemAtPosition(it) as StyleOption).id == option }
        spinner.setSelection(index)
    }

    private fun selected(activity: Activity, label: Int) = (spinner(activity, label).selectedItem as StyleOption).id

    private fun click(activity: Activity, label: Int) {
        assertTrue(views(activity).filterIsInstance<Button>().single { it.text == activity.getString(label) }.performClick())
    }
}
