package nl.nederlandstijd.widget

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

/** One complete record per widget makes writes atomic and prevents settings leaking between widgets. */
internal class WidgetStyleStore(context: Context) {
    private val context = context.applicationContext
    private val preferences = context.getSharedPreferences("widget_styles", Context.MODE_PRIVATE)

    // Existing widgets keep the original appearance until explicitly configured.
    fun read(id: Int): WidgetStyle = decode(preferences.all[key(id)] as? String)
    fun defaults(): WidgetStyle = decode(preferences.all[DEFAULTS] as? String)
    fun contains(id: Int): Boolean = preferences.contains(key(id))

    fun save(id: Int, style: WidgetStyle, useAsDefault: Boolean = false): Boolean = synchronized(lock) {
        val editor = preferences.edit()
        if (useAsDefault) writeDefaults(editor, style)
        editor.putString(key(id), encode(style)).commit()
    }

    fun saveDefaults(style: WidgetStyle): Boolean = synchronized(lock) {
        val editor = preferences.edit()
        writeDefaults(editor, style)
        editor.commit()
    }

    private fun writeDefaults(editor: SharedPreferences.Editor, style: WidgetStyle) {
        // Freeze existing widgets before changing the appearance inherited by future widgets.
        val previous = encode(defaults())
        MinuteScheduler.widgetIds(context).filterNot(::contains).forEach { editor.putString(key(it), previous) }
        editor.putString(DEFAULTS, encode(style))
    }

    /** Each instance snapshots defaults once, never follows them live. */
    fun ensureInitialized(ids: IntArray): Boolean = synchronized(lock) {
        val missing = ids.filterNot(::contains)
        if (missing.isEmpty()) return@synchronized true
        val initial = encode(defaults())
        val editor = preferences.edit()
        missing.forEach { editor.putString(key(it), initial) }
        editor.commit()
    }

    fun delete(ids: IntArray): Boolean = synchronized(lock) {
        val editor = preferences.edit()
        ids.forEach { editor.remove(key(it)) }
        editor.commit()
    }

    fun remap(oldIds: IntArray, newIds: IntArray): Boolean = synchronized(lock) {
        require(oldIds.size == newIds.size) { "Restored widget ID arrays must have equal lengths" }
        // Read every source before writing, including when old and new IDs overlap.
        val restored = oldIds.map { encode(read(it)) }
        val editor = preferences.edit()
        oldIds.forEach { editor.remove(key(it)) }
        newIds.forEachIndexed { index, id -> editor.putString(key(id), restored[index]) }
        editor.commit()
    }

    private fun key(id: Int) = "widget_$id"

    private fun encode(style: WidgetStyle): String = style.sanitized().let {
        JSONObject().put("color", it.colorId).put("font", it.fontId)
            .put("size", it.sizePercent).put("alignment", it.alignment.name)
            .put("background", it.backgroundId).put("opacity", it.backgroundOpacity)
            .put("radius", it.cornerRadius).toString()
    }

    private fun decode(value: String?): WidgetStyle {
        if (value == null) return WidgetStyle()
        return try {
            val data = JSONObject(value)
            WidgetStyle(
                colorId = data.optString("color", "automatic"),
                fontId = data.optString("font", "automatic"),
                sizePercent = data.optInt("size", 100),
                alignment = WidgetAlignment.entries.firstOrNull { it.name == data.optString("alignment") }
                    ?: WidgetAlignment.AUTO,
                backgroundId = data.optString("background", "transparent"),
                backgroundOpacity = data.optInt("opacity", 60),
                cornerRadius = data.optInt("radius", 16),
            ).sanitized()
        } catch (_: org.json.JSONException) {
            WidgetStyle()
        }
    }

    private companion object {
        const val DEFAULTS = "defaults"
        val lock = Any()
    }
}
