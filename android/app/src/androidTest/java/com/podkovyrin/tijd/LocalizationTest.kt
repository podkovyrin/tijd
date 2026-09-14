package com.podkovyrin.tijd

import android.content.res.Configuration
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalizationTest {
    private fun context(tag: String) = InstrumentationRegistry.getInstrumentation().targetContext.let {
        it.createConfigurationContext(Configuration(it.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(tag))
        })
    }

    @Test
    fun localeResourcesResolveAndKeepFormattingAndBranding() {
        assertEquals("Language", context("en").getString(R.string.language))
        assertEquals("Taal", context("nl-NL").getString(R.string.language))
        assertEquals("Langue", context("fr-FR").getString(R.string.language))
        val declared = mutableListOf<String>()
        context("en").resources.getXml(R.xml.locales_config).use { parser ->
            while (parser.eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "locale") {
                    declared += parser.getAttributeValue("http://schemas.android.com/apk/res/android", "name")
                }
                parser.next()
            }
        }
        assertEquals(91, declared.size)
        for (tag in declared.filter { it != "en" } + listOf("pt-AO", "he-IL", "id-ID", "pa-Guru-IN", "pa-Arab-PK", "zh-Hans-CN", "zh-Hant-TW")) {
            val localized = context(tag)
            assertNotEquals("Unexpected English fallback for $tag", "Language", localized.getString(R.string.language))
            assertEquals("Tijd", localized.getString(R.string.app_name))
            // Palette and font names deliberately retain the English catalog labels.
            for (resource in listOf(R.string.color_sage, R.string.color_navy, R.string.font_sans_light)) {
                assertEquals("Fixed catalog label changed in $tag", context("en").getString(resource), localized.getString(resource))
            }
            val label = localized.getString(R.string.widget_edit_accessibility, 47, "LAYOUT", "LANGUAGE")
            val number = String.format(Locale.forLanguageTag(tag), "%d", 47)
            assertTrue("Lost widget id in $tag", label.contains(number))
            assertTrue("Lost layout in $tag", label.contains("LAYOUT"))
            assertTrue("Lost language in $tag", label.contains("LANGUAGE"))
        }
        assertEquals(View.LAYOUT_DIRECTION_RTL, context("ar").resources.configuration.layoutDirection)
        assertNotEquals(context("pa-Guru-IN").getString(R.string.language), context("pa-Arab-PK").getString(R.string.language))
        assertNotEquals(context("sr-Latn").getString(R.string.language), context("sr").getString(R.string.language))
    }

    @Test
    fun longTranslatedButtonsExpandAtLargeTextSizes() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val base = context("de")
            val large = base.createConfigurationContext(Configuration(base.resources.configuration).apply { fontScale = 2f })
            val themed = androidx.appcompat.view.ContextThemeWrapper(large, R.style.AppTheme)
            val parent = android.widget.LinearLayout(themed)
            for (layout in listOf(R.layout.button_primary, R.layout.button_tonal)) {
                val button = android.view.LayoutInflater.from(themed).inflate(layout, parent, false) as android.widget.Button
                button.setText(R.string.license_acknowledgments)
                val width = themed.dp(200)
                button.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                button.layout(0, 0, width, button.measuredHeight)
                assertTrue("Long translated label must expand the button", button.height > themed.dp(56))
                assertEquals(button.text.length, button.layout.getLineEnd(button.layout.lineCount - 1))
                for (line in 0 until button.layout.lineCount) assertEquals(0, button.layout.getEllipsisCount(line))
            }
        }
    }

    @Test
    fun pickerUsesTheActivityLocaleAndWidgetLanguageRemainsIndependent() {
        assertTrue(LanguagePicker.displayName(context("nl"), "de").contains("Duits"))
        assertEquals("pa-Arab", SpokenTime.defaultLanguage(Locale.forLanguageTag("pa-Arab-PK")))
        assertEquals("zh-Hant", SpokenTime.defaultLanguage(Locale.forLanguageTag("zh-Hant-TW")))
        assertEquals("sh", SpokenTime.defaultLanguage(Locale.forLanguageTag("sr-Latn")))
    }
}
