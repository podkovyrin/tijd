package com.podkovyrin.tijd

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetLayoutPickerTest {
    @Test
    fun gesturesAndTabsSelectDistinctFootprintsAndAddTheSelectedLayout() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        var chosen: Boolean? = null
        lateinit var dialog: BottomSheetDialog
        lateinit var pager: ViewPager2
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { activity ->
                    dialog = WidgetLayoutPicker.show(activity, scope) { chosen = it }
                    pager = dialog.findViewById(R.id.layout_pager)!!
                }
                instrumentation.waitForIdleSync()
                fun awaitPage(expected: Int) {
                    val deadline = SystemClock.uptimeMillis() + 4000
                    while (SystemClock.uptimeMillis() < deadline) {
                        var ready = false
                        instrumentation.runOnMainSync { ready = pager.currentItem == expected && pager.scrollState == ViewPager2.SCROLL_STATE_IDLE }
                        if (ready) return
                        SystemClock.sleep(50)
                    }
                    fail("Pager did not settle on layout $expected")
                }
                fun swipe(forward: Boolean) {
                    val location = IntArray(2)
                    var width = 0
                    var height = 0
                    instrumentation.runOnMainSync { pager.getLocationOnScreen(location); width = pager.width; height = pager.height }
                    val startX = location[0] + width * if (forward) 0.85f else 0.15f
                    val endX = location[0] + width * if (forward) 0.15f else 0.85f
                    val y = location[1] + height * 0.45f
                    val down = SystemClock.uptimeMillis()
                    for (step in 0..16) {
                        val action = when (step) { 0 -> MotionEvent.ACTION_DOWN; 16 -> MotionEvent.ACTION_UP; else -> MotionEvent.ACTION_MOVE }
                        val event = MotionEvent.obtain(down, SystemClock.uptimeMillis(), action, startX + (endX - startX) * step / 16, y, 0)
                        event.source = InputDevice.SOURCE_TOUCHSCREEN
                        assertTrue(instrumentation.uiAutomation.injectInputEvent(event, true))
                        event.recycle()
                        SystemClock.sleep(16)
                    }
                }
                awaitPage(0)
                swipe(true)
                awaitPage(1)
                swipe(false)
                awaitPage(0)
                scenario.onActivity {
                    fun descendants(view: View): List<View> = listOf(view) + if (view is ViewGroup) {
                        (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
                    } else emptyList()
                    val views = descendants(dialog.window!!.decorView)
                    assertFalse("Both widget previews should render", views.filterIsInstance<android.widget.TextView>()
                        .any { text -> text.text == it.getString(R.string.preview_unavailable) })
                    val previews = views.filterIsInstance<WidgetPreview>()
                    assertTrue("Compact preview must be square", previews.any { it.width > 0 && it.width == it.height })
                    assertTrue("Row preview must be wide", previews.any { it.height > 0 && it.width >= it.height * 3 })
                    views.filterIsInstance<TabLayout>().single().getTabAt(1)!!.select()
                }
                awaitPage(1)
                scenario.onActivity {
                    val button = dialog.findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)!!
                    fun find(view: View): MaterialButton? {
                        if (view is MaterialButton && view.text == it.getString(R.string.add_this_widget)) return view
                        if (view is ViewGroup) for (index in 0 until view.childCount) find(view.getChildAt(index))?.let { return it }
                        return null
                    }
                    assertTrue(find(button)!!.performClick())
                    assertEquals(true, chosen)
                    assertFalse(dialog.isShowing)
                }
            } finally {
                instrumentation.runOnMainSync { runCatching { dialog.dismiss() } }
                scope.cancel()
            }
        }
    }
}
