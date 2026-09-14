package com.podkovyrin.tijd

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/** Serializes all publications, including resize and wake recovery, without keeping a service alive. */
internal object ClockUpdates {
    private val publication = Mutex()
    private data class LayoutKey(
        val geometry: ClockViews.Geometry,
        val ink: ClockInk,
        val singleLine: Boolean,
        val configuration: Configuration,
        val style: WidgetStyle,
    )
    private val published = mutableMapOf<Int, LayoutKey>()

    fun receive(receiver: BroadcastReceiver, context: Context, forceFull: Boolean = false, expectedAt: Long = 0) {
        val pending = receiver.goAsync()
        val app = context.applicationContext
        val received = SystemClock.elapsedRealtime()
        ClockDiagnostics.event("received delay_ms=${if (expectedAt > 0) System.currentTimeMillis() - expectedAt else 0}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // The next non-waking alarm survives both a failed update and process eviction.
                MinuteScheduler.scheduleNext(app)
                withTimeout(8_000) { refresh(app, forceFull) }
            } catch (exception: Exception) {
                Log.e("DutchTimeWidget", "Unable to refresh the clock", exception)
            } finally {
                ClockDiagnostics.event("finished duration_ms=${SystemClock.elapsedRealtime() - received}")
                pending.finish()
            }
        }
    }

    suspend fun refresh(context: Context, forceFull: Boolean = false) = withContext(Dispatchers.IO) {
        publication.withLock {
            val ids = MinuteScheduler.widgetIds(context)
            published.keys.retainAll(ids.toSet())
            if (!context.getSystemService(PowerManager::class.java).isInteractive) {
                ClockDiagnostics.event("skipped screen_off")
                return@withLock
            }
            val manager = AppWidgetManager.getInstance(context)
            for (id in ids) {
                currentCoroutineContext().ensureActive()
                // One widget's failure must not prevent the others from receiving this minute.
                for (attempt in 0..1) {
                    try {
                        withTimeout(3_000) { publish(context, manager, id, forceFull || attempt > 0) }
                        break
                    } catch (exception: Exception) {
                        currentCoroutineContext().ensureActive()
                        if (exception is CancellationException && exception !is kotlinx.coroutines.TimeoutCancellationException) throw exception
                        published.remove(id)
                        Log.e("DutchTimeWidget", "Widget $id update failed (attempt ${attempt + 1})", exception)
                    }
                }
            }
        }
    }

    private suspend fun publish(context: Context, manager: AppWidgetManager, id: Int, forceFull: Boolean) {
        val started = SystemClock.elapsedRealtime()
        val info = manager.getAppWidgetInfo(id) ?: return
        val options = manager.getAppWidgetOptions(id)
        val configuration = Configuration(context.resources.configuration)
        val renderContext = context.createConfigurationContext(configuration)
        val key = LayoutKey(
            ClockViews.geometry(options),
            WallpaperAppearance.resolve(context, options),
            info.provider == ComponentName(context, DutchTimeRowWidgetReceiver::class.java),
            configuration,
            WidgetStyleStore(context).read(id),
        )
        val coroutine = currentCoroutineContext()
        ClockViews.prepare(renderContext, key.geometry, key.ink, key.singleLine, key.style) { coroutine.ensureActive() }
        // The framework does not merge size/orientation RemoteViews trees in partial updates.
        // Send those trees in full, with cached metrics, so rotation never revives old text.
        val partial = !forceFull && published[id] == key && key.geometry.sizes.size == 1
        var text = ClockViews.currentText(key.style.languageCode)
        var views = ClockViews.forGeometry(renderContext, key.geometry, text, key.ink, key.singleLine, partial, key.style)
        // Even a fast render can straddle a minute boundary. Re-sample after preparation.
        val latest = ClockViews.currentText(key.style.languageCode)
        if (latest != text) {
            text = latest
            views = ClockViews.forGeometry(renderContext, key.geometry, text, key.ink, key.singleLine, partial, key.style)
        }
        coroutine.ensureActive()
        if (partial) manager.partiallyUpdateAppWidget(id, views) else manager.updateAppWidget(id, views)
        published[id] = key
        ClockDiagnostics.event("published id=$id mode=${if (partial) "partial" else "full"} duration_ms=${SystemClock.elapsedRealtime() - started} text=$text")
    }
}
