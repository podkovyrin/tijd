package nl.nederlandstijd.widget

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetStyleStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences("widget_styles", Context.MODE_PRIVATE)
    private val store = WidgetStyleStore(context)
    private val ids = intArrayOf(990001, 990002, 990003)
    private val priorDefaults = store.defaults()

    @After
    fun cleanUp() {
        store.delete(ids)
        store.saveDefaults(priorDefaults)
    }

    @Test
    fun widgetStylesAreIndependentAndDefaultsAreSnapshots() {
        store.delete(ids)
        val rose = WidgetStyle(colorId = "rose", fontId = "serif")
        val teal = WidgetStyle(colorId = "teal", backgroundId = "ivory")
        assertTrue(store.saveDefaults(rose))
        assertEquals(WidgetStyle(), store.read(ids[0]))
        assertTrue(store.ensureInitialized(intArrayOf(ids[0])))
        assertTrue(store.saveDefaults(teal))
        assertTrue(store.ensureInitialized(intArrayOf(ids[0], ids[1])))
        assertEquals(rose, store.read(ids[0]))
        assertEquals(teal, store.read(ids[1]))
        assertTrue(store.save(ids[0], WidgetStyle()))
        assertEquals(teal, store.read(ids[1]))
        assertEquals(teal, store.defaults())
    }

    @Test
    fun saveCanSetInstanceAndDefaultsTogether() {
        store.delete(ids)
        assertTrue(!store.contains(ids[0]))
        val style = WidgetStyle(colorId = "mint", cornerRadius = 32)
        assertTrue(store.save(ids[0], style, useAsDefault = true))
        assertTrue(store.contains(ids[0]))
        assertEquals(style, store.read(ids[0]))
        assertEquals(style, store.defaults())
        assertTrue(store.ensureInitialized(intArrayOf(ids[1])))
        assertEquals(style, store.read(ids[1]))
    }

    @Test
    fun restorationReadsSourcesBeforeReplacingOverlappingIds() {
        val rose = WidgetStyle(colorId = "rose")
        val teal = WidgetStyle(colorId = "teal")
        store.save(ids[0], rose)
        store.save(ids[1], teal)
        assertTrue(store.remap(intArrayOf(ids[0], ids[1]), intArrayOf(ids[1], ids[2])))
        assertEquals(WidgetStyle(), store.read(ids[0]))
        assertEquals(rose, store.read(ids[1]))
        assertEquals(teal, store.read(ids[2]))
        assertTrue(store.delete(intArrayOf(ids[1])))
        assertEquals(WidgetStyle(), store.read(ids[1]))
        assertEquals(teal, store.read(ids[2]))
    }

    @Test
    fun invalidPersistedFieldsAndCorruptRecordsFallBackSafely() {
        preferences.edit().putString("widget_${ids[0]}", """
            {"color":"missing","font":"missing","size":999,"alignment":"missing",
             "background":"missing","opacity":200,"radius":-1}
        """.trimIndent()).commit()
        assertEquals(WidgetStyle(backgroundOpacity = 100), store.read(ids[0]))
        preferences.edit().putString("widget_${ids[0]}", "not json").commit()
        assertEquals(WidgetStyle(), store.read(ids[0]))
        preferences.edit().putInt("widget_${ids[0]}", 7).commit()
        assertEquals(WidgetStyle(), store.read(ids[0]))
    }
}
