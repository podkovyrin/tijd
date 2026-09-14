package com.podkovyrin.tijd

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class ClockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Best-effort catch-up while the process exists. This receiver does not keep it alive;
        // the pending RTC alarm remains the recovery path if Android evicts the process.
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            @Suppress("DEPRECATION") // Opportunistic refresh; minute updates also check color hints.
            addAction(Intent.ACTION_WALLPAPER_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                ClockUpdates.receive(this, context, forceFull = intent.action == Intent.ACTION_CONFIGURATION_CHANGED)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
        }
    }
}
