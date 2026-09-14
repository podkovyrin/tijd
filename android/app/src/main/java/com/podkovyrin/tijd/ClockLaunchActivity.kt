package com.podkovyrin.tijd

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast

/** A widget tap opens the user's clock app, without creating or changing any alarms. */
class ClockLaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.clock_unavailable, Toast.LENGTH_LONG).show()
        } finally {
            finish()
        }
    }
}
