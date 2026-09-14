package com.podkovyrin.tijd

import android.graphics.Typeface

/** A finite native-layout catalog: measurement and launcher rendering use the same family. */
internal object ClockFont {
    data class Choice(val id: String, val name: String, val typeface: Typeface, val lightLayout: Int, val darkLayout: Int, val label: Int)

    private fun choice(id: String, name: String, family: String, light: Int, dark: Int, label: Int) =
        Choice(id, name, Typeface.create(family, Typeface.NORMAL), light, dark, label)

    private val families by lazy {
        listOf(
            choice("sans", "System sans", "sans-serif-medium", R.layout.widget_clock, R.layout.widget_clock_dark_text, R.string.font_sans),
            choice("rounded", "Rounded", "variable-body-large-emphasized", R.layout.widget_clock_rounded, R.layout.widget_clock_roundeddark, R.string.font_rounded),
            choice("google", "Google Sans", "google-sans-medium", R.layout.widget_clock_google, R.layout.widget_clock_googledark, R.string.font_google),
            choice("serif", "Serif", "serif", R.layout.widget_clock_serif, R.layout.widget_clock_serifdark, R.string.font_serif),
            choice("mono", "Monospace", "monospace", R.layout.widget_clock_mono, R.layout.widget_clock_monodark, R.string.font_mono),
            choice("condensed", "Condensed", "sans-serif-condensed", R.layout.widget_clock_condensed, R.layout.widget_clock_condenseddark, R.string.font_condensed),
            choice("serif-mono", "Serif mono", "serif-monospace", R.layout.widget_clock_serifmono, R.layout.widget_clock_serifmonodark, R.string.font_serif_mono),
            choice("sans-light", "Light sans", "sans-serif-light", R.layout.widget_clock_sanslight, R.layout.widget_clock_sanslightdark, R.string.font_sans_light),
            choice("sans-black", "Heavy sans", "sans-serif-black", R.layout.widget_clock_sansblack, R.layout.widget_clock_sansblackdark, R.string.font_sans_black),
            choice("casual", "Casual", "casual", R.layout.widget_clock_casual, R.layout.widget_clock_casualdark, R.string.font_casual),
            choice("cursive", "Cursive", "cursive", R.layout.widget_clock_cursive, R.layout.widget_clock_cursivedark, R.string.font_cursive),
        ).filter { it.id == "sans" || it.typeface != Typeface.DEFAULT }.distinctBy { it.typeface }
    }

    private val automatic by lazy {
        families.firstOrNull { it.id == "rounded" } ?: families.firstOrNull { it.id == "google" } ?: families.first()
    }

    fun available(): List<Choice> = listOf(automatic.copy(id = "automatic", name = "Automatic", label = R.string.automatic)) + families

    fun layoutFor(ink: ClockInk, fontId: String = "automatic"): Int {
        val font = families.firstOrNull { it.id == fontId } ?: automatic
        return if (ink == ClockInk.Light) font.lightLayout else font.darkLayout
    }
}
