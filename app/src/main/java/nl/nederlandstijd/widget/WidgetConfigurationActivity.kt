package nl.nederlandstijd.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Owns an unsaved draft; only Save changes a widget or the defaults for future widgets. */
class WidgetConfigurationActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var draft = WidgetStyle()
    private lateinit var preview: WidgetPreview
    private lateinit var controls: LinearLayout
    private lateinit var useAsDefault: CheckBox
    private var options = Bundle()
    private var singleLine = false
    private var sample = "current"
    private var lightPreview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (!ownsWidget()) {
            finish()
            return
        }
        val manager = AppWidgetManager.getInstance(this)
        singleLine = manager.getAppWidgetInfo(widgetId).provider == ComponentName(this, DutchTimeRowWidgetReceiver::class.java)
        options = manager.getAppWidgetOptions(widgetId)
        val store = WidgetStyleStore(this)
        draft = if (savedInstanceState == null) {
            if (store.contains(widgetId)) store.read(widgetId) else store.defaults()
        } else WidgetStyle(
            savedInstanceState.getString("color") ?: "automatic",
            savedInstanceState.getString("font") ?: "automatic",
            savedInstanceState.getInt("size", 100),
            WidgetAlignment.entries.firstOrNull { it.name == savedInstanceState.getString("alignment") } ?: WidgetAlignment.AUTO,
            savedInstanceState.getString("background") ?: "transparent",
            savedInstanceState.getInt("opacity", 60),
            savedInstanceState.getInt("radius", 16),
        ).sanitized()
        sample = savedInstanceState?.getString("sample") ?: "current"
        lightPreview = savedInstanceState?.getBoolean("lightPreview") ?: false

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }
        header.label(getString(R.string.edit_widget_title, widgetId), heading = true)
        preview = WidgetPreview(this, scope)
        header.addView(preview, LinearLayout.LayoutParams(-1, dp(140)))
        val actions = LinearLayout(this).apply {
            setPadding(dp(20), dp(4), dp(20), dp(4))
        }
        val content = settingsContent(header, actions)
        content.label(getString(R.string.edit_widget_description))
        content.choice(getString(R.string.preview_phrase), listOf(
            StyleOption("current", getString(R.string.current_time)),
            StyleOption("short", "negen uur"),
            StyleOption("long", "negentien over twaalf"),
        ), sample) { sample = it; updatePreview() }
        content.choice(getString(R.string.preview_surface), listOf(
            StyleOption("dark", getString(R.string.dark_surface)),
            StyleOption("light", getString(R.string.light_surface)),
        ), if (lightPreview) "light" else "dark") {
            lightPreview = it == "light"
            updatePreview()
        }
        content.label(getString(R.string.preview_note))
        controls = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(controls)
        buildControls()
        useAsDefault = CheckBox(this).apply {
            setText(R.string.use_as_default)
            isChecked = savedInstanceState?.getBoolean("useAsDefault") ?: false
            content.addView(this)
        }
        Button(this).apply {
            setText(R.string.save_style)
            setOnClickListener { save(this) }
            actions.addView(this, LinearLayout.LayoutParams(0, -2, 1f))
        }
        Button(this).apply {
            setText(R.string.reset_style)
            setOnClickListener { draft = WidgetStyle(); buildControls(); updatePreview() }
            content.addView(this)
        }
        Button(this).apply {
            setText(android.R.string.cancel)
            setOnClickListener { finish() }
            actions.addView(this, LinearLayout.LayoutParams(0, -2, 1f))
        }
        updatePreview()
    }

    private fun buildControls() {
        controls.removeAllViews()
        val palette = StyleCatalog.colors.map { StyleOption(it.id, it.name, color = it.argb) }
        controls.choice(getString(R.string.text_color), listOf(StyleOption("automatic", getString(R.string.automatic))) + palette, draft.colorId) {
            change(draft.copy(colorId = it))
        }
        controls.choice(getString(R.string.typeface), ClockFont.available().map {
            StyleOption(it.id, it.name, typeface = it.typeface)
        }, draft.fontId) { change(draft.copy(fontId = it)) }
        controls.choice(getString(R.string.text_size), listOf(
            StyleOption("70", getString(R.string.size_small)), StyleOption("85", getString(R.string.size_medium)),
            StyleOption("100", getString(R.string.size_auto)),
        ), draft.sizePercent.toString()) { change(draft.copy(sizePercent = it.toInt())) }
        controls.choice(getString(R.string.alignment), listOf(
            StyleOption("AUTO", getString(R.string.automatic)), StyleOption("START", getString(R.string.align_left)),
            StyleOption("CENTER", getString(R.string.align_center)), StyleOption("END", getString(R.string.align_right)),
        ), draft.alignment.name) { change(draft.copy(alignment = WidgetAlignment.valueOf(it))) }
        controls.choice(getString(R.string.background), listOf(StyleOption("transparent", getString(R.string.transparent))) + palette, draft.backgroundId) {
            change(draft.copy(backgroundId = it))
        }
        controls.choice(getString(R.string.background_opacity), listOf(20, 40, 60, 80, 100).map {
            StyleOption(it.toString(), "$it%")
        }, draft.backgroundOpacity.toString()) { change(draft.copy(backgroundOpacity = it.toInt())) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            controls.choice(getString(R.string.corners), listOf(
                StyleOption("0", getString(R.string.corners_square)), StyleOption("16", getString(R.string.corners_soft)),
                StyleOption("32", getString(R.string.corners_round)),
            ), draft.cornerRadius.toString()) { change(draft.copy(cornerRadius = it.toInt())) }
        }
    }

    private fun change(style: WidgetStyle) {
        if (style == draft) return
        draft = style
        updatePreview()
    }

    private fun updatePreview() {
        val text = when (sample) {
            "short" -> "negen uur"
            "long" -> "negentien over twaalf"
            else -> ClockViews.currentText()
        }
        preview.setBackgroundColor(if (lightPreview) Color.rgb(229, 225, 214) else Color.rgb(43, 53, 59))
        preview.show(draft, options, singleLine, text)
    }

    private fun save(button: Button) {
        if (!ownsWidget()) { finish(); return }
        button.isEnabled = false
        // A single small durable write and result delivery complete together on the UI thread.
        // The receiver owns the longer refresh, which must survive this activity finishing.
        if (WidgetStyleStore(this).save(widgetId, draft, useAsDefault.isChecked)) {
            val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider
            if (provider != null) {
                sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setComponent(provider)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId)))
            }
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
        } else {
            button.isEnabled = true
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun ownsWidget(): Boolean {
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider ?: return false
        return provider == ComponentName(this, DutchTimeWidgetReceiver::class.java) ||
            provider == ComponentName(this, DutchTimeRowWidgetReceiver::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("color", draft.colorId)
        outState.putString("font", draft.fontId)
        outState.putInt("size", draft.sizePercent)
        outState.putString("alignment", draft.alignment.name)
        outState.putString("background", draft.backgroundId)
        outState.putInt("opacity", draft.backgroundOpacity)
        outState.putInt("radius", draft.cornerRadius)
        outState.putString("sample", sample)
        outState.putBoolean("lightPreview", lightPreview)
        if (::useAsDefault.isInitialized) outState.putBoolean("useAsDefault", useAsDefault.isChecked)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
