package nl.nederlandstijd.widget

import android.content.Context
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Emulator-only: explicitly resets the test installation's font cache. */
@RunWith(AndroidJUnit4::class)
class ClockCacheTest {
    @Test
    fun savedMetricsSurviveMemoryEvictionAndInvalidateOnFontChanges() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("clock_font_sizes", Context.MODE_PRIVATE).edit().clear().commit()
        StableClockText.clearMemoryCache()
        val layout = ClockFont.layoutFor(ClockInk.Light)
        var calculations = 0
        val first = StableClockText.fit(context, "negen uur", 137f, 93f, layout, false) { calculations++ }
        assertTrue("Initial sizing was not calculated", calculations > 1)
        StableClockText.clearMemoryCache()
        calculations = 0
        val restored = StableClockText.fit(context, "negen uur", 137f, 93f, layout, false) { calculations++ }
        assertEquals(first, restored)
        assertEquals("Memory eviction should load persisted metrics without recalculating phrases", 1, calculations)
        val changed = context.createConfigurationContext(Configuration(context.resources.configuration).apply { fontScale = 2f })
        calculations = 0
        StableClockText.fit(changed, "negen uur", 137f, 93f, layout, false) { calculations++ }
        assertTrue("Font configuration changes must invalidate saved metrics", calculations > 1)
    }

    @Test
    fun cancelledSizingDoesNotSaveAnIncompleteResult() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("clock_font_sizes", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        StableClockText.clearMemoryCache()
        var checks = 0
        try {
            StableClockText.fit(context, "negen uur", 139f, 97f, ClockFont.layoutFor(ClockInk.Light), false) {
                if (++checks > 5) throw CancellationException("Test cancellation during sizing")
            }
            throw AssertionError("Sizing ignored cancellation")
        } catch (_: CancellationException) {
            assertTrue("An interrupted calculation was persisted", preferences.all.isEmpty())
        }
    }
}
