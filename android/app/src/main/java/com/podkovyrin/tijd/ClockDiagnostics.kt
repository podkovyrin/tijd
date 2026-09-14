package com.podkovyrin.tijd

import android.util.Log

/** Debug-only timings; no files, telemetry, timers, or production log traffic. */
internal object ClockDiagnostics {
    fun event(message: String) {
        if (BuildConfig.DEBUG) Log.d("DutchTimeWidget", message)
    }
}
