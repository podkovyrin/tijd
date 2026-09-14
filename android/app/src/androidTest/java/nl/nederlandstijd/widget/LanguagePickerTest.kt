package nl.nederlandstijd.widget

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguagePickerTest {
    @Test
    fun searchResultAndCancelStayAboveKeyboardAndTitleReturnsAfterDismissal() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var dialog: AlertDialog
        lateinit var search: EditText
        lateinit var list: ListView
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { activity ->
                    dialog = LanguagePicker.show(activity, "zu") { }
                    fun descendants(view: View): List<View> = listOf(view) + if (view is ViewGroup) {
                        (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
                    } else emptyList()
                    val views = descendants(dialog.window!!.decorView)
                    search = views.filterIsInstance<EditText>().single()
                    list = views.filterIsInstance<ListView>().single()
                    search.setText("Zulu")
                    search.requestFocus()
                }
                instrumentation.waitForIdleSync()
                scenario.onActivity { activity ->
                    activity.getSystemService(InputMethodManager::class.java).showSoftInput(search, InputMethodManager.SHOW_IMPLICIT)
                }
                fun awaitKeyboard(visible: Boolean) {
                    val deadline = android.os.SystemClock.uptimeMillis() + 5000
                    while (android.os.SystemClock.uptimeMillis() < deadline) {
                        var ready = false
                        instrumentation.runOnMainSync {
                            ready = ViewCompat.getRootWindowInsets(search)?.isVisible(WindowInsetsCompat.Type.ime()) == visible &&
                                (dialog.findViewById<View>(androidx.appcompat.R.id.topPanel)!!.visibility == View.GONE) == visible
                        }
                        if (ready) { instrumentation.waitForIdleSync(); return }
                        android.os.SystemClock.sleep(50)
                    }
                    fail("Keyboard did not become visible=$visible")
                }
                awaitKeyboard(true)
                scenario.onActivity { activity ->
                    val available = Rect().also { search.getWindowVisibleDisplayFrame(it) }
                    for (view in listOf(list.getChildAt(0), dialog.getButton(AlertDialog.BUTTON_NEGATIVE))) {
                        val location = IntArray(2).also { view.getLocationOnScreen(it) }
                        assertTrue("Result and Cancel must be entirely above the keyboard", location[1] + view.height <= available.bottom)
                        assertTrue(location[1] >= available.top)
                        val visible = Rect()
                        assertTrue(view.getGlobalVisibleRect(visible))
                        assertEquals(view.height, visible.height())
                    }
                    assertEquals(0, list.checkedItemPosition)
                    activity.getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(search.windowToken, 0)
                }
                awaitKeyboard(false)
                scenario.onActivity {
                    assertEquals("Zulu", search.text.toString())
                    assertEquals(0, list.checkedItemPosition)
                    assertTrue(dialog.findViewById<View>(androidx.appcompat.R.id.topPanel)!!.isShown)
                }
            } finally {
                instrumentation.runOnMainSync { runCatching { dialog.dismiss() } }
            }
        }
    }

    @Test
    fun constrainedDialogCanScrollToAndSelectTheLastLanguageAfterResizing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        lateinit var dialog: AlertDialog
        lateinit var list: ListView
        var selected = ""
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { activity ->
                    dialog = LanguagePicker.show(activity, "nl") { selected = it }
                    fun descendants(view: View): List<View> = listOf(view) + if (view is ViewGroup) {
                        (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
                    } else emptyList()
                    list = descendants(dialog.window!!.decorView).filterIsInstance<ListView>().single()
                }
                // Exercise both a small dialog and a further height reduction, as with IME resize.
                for (heightDp in listOf(360, 300)) {
                    scenario.onActivity { activity ->
                        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, activity.dp(heightDp))
                    }
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {
                        val visible = Rect()
                        assertTrue(list.getGlobalVisibleRect(visible))
                        assertEquals("List measurement must match its visible viewport", list.height, visible.height())
                        assertTrue("List should shrink below its preferred height", list.height < it.dp(320))
                        list.setSelection(list.count - 1)
                    }
                    instrumentation.waitForIdleSync()
                    scenario.onActivity {
                        assertEquals(list.count - 1, list.lastVisiblePosition)
                        val last = list.getChildAt(list.childCount - 1)
                        val visible = Rect()
                        assertTrue(last.getGlobalVisibleRect(visible))
                        assertEquals("Final language must be fully visible at ${heightDp}dp (viewport ${list.height}px)", last.height, visible.height())
                    }
                }
                scenario.onActivity {
                    val index = list.count - 1
                    assertTrue(list.performItemClick(list.getChildAt(list.childCount - 1), index, list.adapter.getItemId(index)))
                    assertEquals("zu", selected)
                    assertFalse(dialog.isShowing)
                }
            } finally {
                instrumentation.runOnMainSync { runCatching { dialog.dismiss() } }
            }
        }
    }
}
