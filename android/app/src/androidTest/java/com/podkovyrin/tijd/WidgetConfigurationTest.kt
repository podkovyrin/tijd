package com.podkovyrin.tijd

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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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
    fun editsSaveImmediatelyAndBackKeepsThem() {
        ActivityScenario.launchActivityForResult<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity { select(it, R.string.text_color, "cobalt") }
            instrumentation.waitForIdleSync()
            assertEquals(original.copy(colorId = "cobalt"), store.read(ids[0]))
            assertEquals(other, store.read(ids[1]))
            assertEquals(previousDefaults, store.defaults())
            scenario.onActivity { click(it, R.string.editor_done) }
            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
        assertEquals(original.copy(colorId = "cobalt"), store.read(ids[0]))
    }

    @Test
    fun editsSurviveRecreationAndDefaultsAreOptIn() {
        ActivityScenario.launchActivityForResult<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity {
                select(it, R.string.text_color, "cobalt")
                select(it, R.string.text_size, "85")
                select(it, R.string.background, "ivory")
            }
            instrumentation.waitForIdleSync()
            val expected = original.copy(colorId = "cobalt", sizePercent = 85, backgroundId = "ivory")
            assertEquals(expected, store.read(ids[0]))
            scenario.onActivity { views(it).filterIsInstance<CheckBox>().single().isChecked = true }
            assertEquals(expected, store.defaults())
            scenario.recreate()
            scenario.onActivity {
                assertEquals("cobalt", selected(it, R.string.text_color))
                assertEquals("85", selected(it, R.string.text_size))
                assertEquals("ivory", selected(it, R.string.background))
                assertTrue(views(it).filterIsInstance<CheckBox>().single().isChecked)
                views(it).filterIsInstance<CheckBox>().single().isChecked = false
                select(it, R.string.text_color, "teal")
            }
            instrumentation.waitForIdleSync()
            assertEquals(expected.copy(colorId = "teal"), store.read(ids[0]))
            assertEquals(expected, store.defaults())
            scenario.onActivity { click(it, R.string.editor_done) }
            val result = scenario.result
            assertEquals(Activity.RESULT_OK, result.resultCode)
            assertEquals(ids[0], result.resultData.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1))
        }
        assertEquals(other, store.read(ids[1]))
    }

    @Test
    fun newConfigurationCanFinishWithoutAnEdit() {
        store.delete(intArrayOf(ids[0]))
        ActivityScenario.launchActivityForResult<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity { click(it, R.string.editor_done) }
            assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        }
        assertEquals(previousDefaults, store.read(ids[0]))
    }

    @Test
    fun resetSavesImmediatelyAndOnlyChangesThisWidget() {
        ActivityScenario.launch<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity { click(it, R.string.reset_style) }
            instrumentation.waitForIdleSync()
            assertEquals(WidgetStyle(), store.read(ids[0]))
            assertEquals(other, store.read(ids[1]))
            assertEquals(previousDefaults, store.defaults())
        }
    }

    @Test
    fun matchingWidgetLayoutsAndLanguagesHaveDistinctAccessibleEditTargets() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val cards = views(activity).filterIsInstance<com.google.android.material.card.MaterialCardView>()
                val descriptions = ids.map { id ->
                    val expected = activity.getString(
                        R.string.widget_edit_accessibility, id, activity.getString(R.string.compact_widget),
                        LanguagePicker.displayName(store.read(id).languageCode),
                    )
                    val card = cards.single { it.contentDescription == expected }
                    assertTrue(card.isFocusable)
                    assertTrue(card.isClickable)
                    card.contentDescription.toString()
                }
                assertEquals("Each widget must have a distinct accessible edit label", 2, descriptions.toSet().size)
            }
        }
    }

    @Test
    fun languageSearchSupportsNamesCodesAndEmptyResults() {
        ActivityScenario.launch<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity { activity ->
                var selected = ""
                val dialog = LanguagePicker.show(activity, "nl") { selected = it }
                val root = dialog.window!!.decorView
                fun descendants(view: View): List<View> = listOf(view) + if (view is ViewGroup) {
                    (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
                } else emptyList()
                val fields = descendants(root)
                val search = fields.filterIsInstance<android.widget.EditText>().single()
                val list = fields.filterIsInstance<android.widget.ListView>().single()
                search.setText("no such language")
                assertEquals(0, list.count)
                search.setText("Nederlands")
                assertEquals(1, list.count)
                assertTrue(list.adapter.getItem(0).toString().contains("Dutch"))
                search.setText("pt-BR")
                assertEquals(1, list.count)
                list.performItemClick(null, 0, list.adapter.getItemId(0))
                assertEquals("pt-BR", selected)
                assertFalse(dialog.isShowing)
            }
        }
    }

    @Test
    @androidx.test.filters.SdkSuppress(minSdkVersion = 29)
    fun materialDropdownOpensAndCompletesARealSelection() {
        ActivityScenario.launch<WidgetConfigurationActivity>(editorIntent(ids[0])).use { scenario ->
            scenario.onActivity { activity -> menu(activity, R.string.text_color).performClick() }
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                val menu = menu(activity, R.string.text_color)
                assertTrue("Expected color options", menu.adapter.count > 1)
                assertTrue("Tapping the field must open the dropdown", menu.isPopupShowing)
                fun descendants(view: View): List<View> = listOf(view) + if (view is ViewGroup) {
                    (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
                } else emptyList()
                val popup = android.view.inspector.WindowInspector.getGlobalWindowViews().flatMap(::descendants)
                    .filterIsInstance<android.widget.ListView>()
                    .single { it.adapter.count > 1 && it.adapter.getItem(1) is StyleOption }
                val index = (0 until popup.adapter.count).first { (popup.adapter.getItem(it) as StyleOption).id == "white" }
                val item = popup.getChildAt(index - popup.firstVisiblePosition)
                assertTrue("White option should be visible", item != null)
                assertTrue(popup.performItemClick(item, index, popup.adapter.getItemId(index)))
            }
            instrumentation.waitForIdleSync()
            assertEquals(original.copy(colorId = "white"), store.read(ids[0]))
        }
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

    private fun menu(activity: Activity, label: Int) = views(activity).filterIsInstance<MaterialAutoCompleteTextView>()
        .single { it.contentDescription == activity.getString(label) }

    private fun select(activity: Activity, label: Int, option: String) {
        val menu = menu(activity, label)
        val index = (0 until menu.adapter.count).first { (menu.adapter.getItem(it) as StyleOption).id == option }
        // Drive the selection callback; these tests verify persistence, not popup positioning.
        val list = android.widget.ListView(activity).apply { adapter = menu.adapter }
        menu.setText(menu.adapter.getItem(index).toString(), false)
        menu.onItemClickListener!!.onItemClick(list, null, index, menu.adapter.getItemId(index))
    }

    private fun selected(activity: Activity, label: Int) = menu(activity, label).tag as String

    private fun click(activity: Activity, label: Int) {
        val target = if (label == R.string.editor_done) {
            views(activity).single { it.contentDescription == activity.getString(label) }
        } else views(activity).filterIsInstance<Button>().single { it.text == activity.getString(label) }
        assertTrue(target.performClick())
    }
}
