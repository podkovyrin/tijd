package nl.nederlandstijd.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/** Every lifecycle event uses the same publisher as the minute alarm. */
open class DutchTimeWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        ClockUpdates.receive(this, context, forceFull = true)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        ClockUpdates.receive(this, context, forceFull = true)
    }

    override fun onEnabled(context: Context) {
        MinuteScheduler.scheduleNext(context)
    }

    override fun onDisabled(context: Context) {
        MinuteScheduler.scheduleNext(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        MinuteScheduler.scheduleNext(context)
    }
}

class DutchTimeRowWidgetReceiver : DutchTimeWidgetReceiver()
