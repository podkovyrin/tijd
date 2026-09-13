package nl.nederlandstijd.widget

import android.content.Context
import org.json.JSONObject

/** One complete record per widget makes writes atomic and prevents settings leaking between widgets. */
internal class WidgetStyleStore(context: Context) {
    private val preferences = context.getSharedPreferences("widget_styles", Context.MODE_PRIVATE)

    fun read(id: Int): WidgetStyle = decode(preferences.getString(key(id), null))
    fun defaults(): WidgetStyle = decode(preferences.getString(DEFAULTS, null))
    fun contains(id: Int): Boolean = preferences.contains(key(id))

    fun save(id: Int, style: WidgetStyle, useAsDefault: Boolean = false): Boolean = synchronized(lock) {
        val editor = preferences.edit()
        if (useAsDefault) editor.putString(DEFAULTS, encode(style))
        editor.putString(key(id), encode(style)).commit()
    }

    fun saveDefaults(style: WidgetStyle): Boolean = synchronized(lock) {
        preferences.edit().putString(DEFAULTS, encode(style)).commit()
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

    private fun encode(style: WidgetStyle): String = style.let {
        JSONObject().put("color", it.colorId).put("font", it.fontId)
            .put("size", it.sizePercent).put("alignment", it.alignment.name)
            .put("background", it.backgroundId).put("opacity", it.backgroundOpacity)
            .put("radius", it.cornerRadius).put("language", it.languageCode).toString()
    }

    private fun decode(value: String?): WidgetStyle {
        if (value == null) return WidgetStyle()
        val data = JSONObject(value)
        return WidgetStyle(
            languageCode = data.getString("language"),
            colorId = data.getString("color"),
            fontId = data.getString("font"),
            sizePercent = data.getInt("size"),
            alignment = WidgetAlignment.valueOf(data.getString("alignment")),
            backgroundId = data.getString("background"),
            backgroundOpacity = data.getInt("opacity"),
            cornerRadius = data.getInt("radius"),
        )
    }

    private companion object {
        const val DEFAULTS = "defaults"
        val lock = Any()
    }
}
