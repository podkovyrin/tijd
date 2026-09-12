package nl.nederlandstijd.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build

internal object MinuteScheduler {
    const val TICK_ACTION = "nl.nederlandstijd.widget.MINUTE_TICK"
    const val EXPECTED_AT = "expected_at"

    fun widgetIds(context: Context): IntArray {
        val manager = AppWidgetManager.getInstance(context)
        return manager.getAppWidgetIds(ComponentName(context, DutchTimeWidgetReceiver::class.java)) +
            manager.getAppWidgetIds(ComponentName(context, DutchTimeRowWidgetReceiver::class.java))
    }

    fun canSchedule(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun nextMinuteAfter(nowMillis: Long): Long = (nowMillis / 60_000 + 1) * 60_000

    fun scheduleNext(context: Context) {
        if (widgetIds(context).isEmpty() || !canSchedule(context)) {
            cancel(context)
            return
        }
        try {
            // RTC does not wake a sleeping phone. Delivery resumes when Android allows it.
            val next = nextMinuteAfter(System.currentTimeMillis())
            context.getSystemService(AlarmManager::class.java).setExact(AlarmManager.RTC, next, operation(context, next))
        } catch (_: SecurityException) {
            // Permission can be revoked between the capability check and this call.
            cancel(context)
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(operation(context))
    }

    suspend fun refresh(context: Context) = ClockUpdates.refresh(context)

    private fun operation(context: Context, expectedAt: Long = 0): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, ClockTickReceiver::class.java).setAction(TICK_ACTION).putExtra(EXPECTED_AT, expectedAt),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

class ClockTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MinuteScheduler.TICK_ACTION,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> Unit
            else -> return
        }
        ClockUpdates.receive(this, context, expectedAt = intent.getLongExtra(MinuteScheduler.EXPECTED_AT, 0))
    }
}
