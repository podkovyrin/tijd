package nl.nederlandstijd.widget

import android.graphics.Typeface

/** Use the host device's fonts; unavailable OEM families fall back to system medium. */
internal object ClockFont {
    private val family by lazy {
        listOf("variable-body-large-emphasized", "google-sans-medium")
            .firstOrNull { Typeface.create(it, Typeface.NORMAL) != Typeface.DEFAULT }
    }

    fun layoutFor(ink: ClockInk): Int = when (family) {
        "variable-body-large-emphasized" -> if (ink == ClockInk.Light) R.layout.widget_clock_rounded else R.layout.widget_clock_roundeddark
        "google-sans-medium" -> if (ink == ClockInk.Light) R.layout.widget_clock_google else R.layout.widget_clock_googledark
        else -> ink.layoutId
    }
}
