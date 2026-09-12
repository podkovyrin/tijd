package nl.nederlandstijd.widget

import android.graphics.Typeface

/** A finite native-layout catalog: measurement and launcher rendering use the same family. */
internal object ClockFont {
    data class Choice(val id: String, val name: String, val typeface: Typeface, val lightLayout: Int, val darkLayout: Int)

    private fun choice(id: String, name: String, family: String, light: Int, dark: Int) =
        Choice(id, name, Typeface.create(family, Typeface.NORMAL), light, dark)

    private val families by lazy {
        listOf(
            choice("sans", "System sans", "sans-serif-medium", R.layout.widget_clock, R.layout.widget_clock_dark_text),
            choice("rounded", "Rounded", "variable-body-large-emphasized", R.layout.widget_clock_rounded, R.layout.widget_clock_roundeddark),
            choice("google", "Google Sans", "google-sans-medium", R.layout.widget_clock_google, R.layout.widget_clock_googledark),
            choice("serif", "Serif", "serif", R.layout.widget_clock_serif, R.layout.widget_clock_serifdark),
            choice("mono", "Monospace", "monospace", R.layout.widget_clock_mono, R.layout.widget_clock_monodark),
            choice("condensed", "Condensed", "sans-serif-condensed", R.layout.widget_clock_condensed, R.layout.widget_clock_condenseddark),
            choice("serif-mono", "Serif mono", "serif-monospace", R.layout.widget_clock_serifmono, R.layout.widget_clock_serifmonodark),
            choice("sans-light", "Light sans", "sans-serif-light", R.layout.widget_clock_sanslight, R.layout.widget_clock_sanslightdark),
            choice("sans-black", "Heavy sans", "sans-serif-black", R.layout.widget_clock_sansblack, R.layout.widget_clock_sansblackdark),
            choice("casual", "Casual", "casual", R.layout.widget_clock_casual, R.layout.widget_clock_casualdark),
            choice("cursive", "Cursive", "cursive", R.layout.widget_clock_cursive, R.layout.widget_clock_cursivedark),
        ).filter { it.id == "sans" || it.typeface != Typeface.DEFAULT }.distinctBy { it.typeface }
    }

    private val automatic by lazy {
        families.firstOrNull { it.id == "rounded" } ?: families.firstOrNull { it.id == "google" } ?: families.first()
    }

    fun available(): List<Choice> = listOf(automatic.copy(id = "automatic", name = "Automatic")) + families

    fun layoutFor(ink: ClockInk, fontId: String = "automatic"): Int {
        val font = families.firstOrNull { it.id == fontId } ?: automatic
        return if (ink == ClockInk.Light) font.lightLayout else font.darkLayout
    }
}
