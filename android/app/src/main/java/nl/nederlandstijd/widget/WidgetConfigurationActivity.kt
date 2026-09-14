package nl.nederlandstijd.widget

import androidx.appcompat.app.AppCompatActivity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/** Every style change is persisted atomically and published while the editor stays open. */
class WidgetConfigurationActivity : AppCompatActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var draft = WidgetStyle()
    private lateinit var preview: WidgetPreview
    private lateinit var controls: LinearLayout
    private lateinit var useAsDefault: CheckBox
    private var options = Bundle()
    private var singleLine = false
    private lateinit var saveStatus: android.widget.TextView
    private var hasSaveFailure = false
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
        if (savedInstanceState != null) {
            require(listOf("color", "font", "size", "alignment", "background", "opacity", "radius", "language",
                "lightPreview", "useAsDefault").all(savedInstanceState::containsKey))
        }
        val store = WidgetStyleStore(this)
        draft = if (savedInstanceState == null) {
            if (store.contains(widgetId)) store.read(widgetId) else store.defaults()
        } else WidgetStyle(
            requireNotNull(savedInstanceState.getString("color")),
            requireNotNull(savedInstanceState.getString("font")),
            savedInstanceState.getInt("size"),
            WidgetAlignment.valueOf(requireNotNull(savedInstanceState.getString("alignment"))),
            requireNotNull(savedInstanceState.getString("background")),
            savedInstanceState.getInt("opacity"),
            savedInstanceState.getInt("radius"),
            requireNotNull(savedInstanceState.getString("language")),
        )
        lightPreview = savedInstanceState?.getBoolean("lightPreview") ?: false

        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val toolbar = com.google.android.material.appbar.MaterialToolbar(this).apply {
            setTitle(R.string.edit_widget_title)
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(getColor(R.color.ui_ink))
            setNavigationContentDescription(R.string.editor_done)
            setNavigationOnClickListener { if (!hasSaveFailure || persist()) finish() }
        }
        header.addView(toolbar, LinearLayout.LayoutParams(-1, dp(64)))
        val previewArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)
        }
        header.addView(previewArea)
        preview = WidgetPreview(this, scope)
        previewArea.addView(preview, LinearLayout.LayoutParams(-1, dp(if (resources.configuration.screenHeightDp < 500) 72 else 140)))
        saveStatus = previewArea.label(getString(R.string.saved_status)).apply {
            textSize = 12f
            accessibilityLiveRegion = android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        val content = settingsContent(header)
        controls = content.card()
        useAsDefault = com.google.android.material.checkbox.MaterialCheckBox(this).apply {
            setText(R.string.use_as_default)
            isChecked = savedInstanceState?.getBoolean("useAsDefault") ?: false
            setTextColor(getColor(R.color.ui_ink))
            content.addView(this)
            setOnCheckedChangeListener { _, _ -> persist() }
        }
        buildControls()
        content.choice(getString(R.string.preview_surface), listOf(
            StyleOption("dark", getString(R.string.dark_surface)),
            StyleOption("light", getString(R.string.light_surface)),
        ), if (lightPreview) "light" else "dark") {
            lightPreview = it == "light"
            updatePreview()
        }
        content.label(getString(R.string.preview_note))
        content.action(getString(R.string.reset_style)) {
            change(WidgetStyle())
            buildControls()
        }
        updatePreview()
        // New widget configuration must deliver RESULT_OK even without a style edit.
        // Returning from this screen completes setup; no separate Save step is needed.
        persist()
    }

    private fun buildControls() {
        controls.removeAllViews()
        controls.label(getString(R.string.language))
        val languageButton = controls.action(LanguagePicker.displayName(this, draft.languageCode)) { }
        languageButton.setIconResource(R.drawable.ic_chevron_right)
        languageButton.iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_END
        languageButton.contentDescription = getString(R.string.language_accessibility, LanguagePicker.displayName(this, draft.languageCode))
        languageButton.setOnClickListener {
            LanguagePicker.show(this, draft.languageCode) { code ->
                change(draft.copy(languageCode = code))
                languageButton.text = LanguagePicker.displayName(this, code)
                languageButton.contentDescription = getString(R.string.language_accessibility, LanguagePicker.displayName(this, code))
            }
        }
        controls.label(getString(R.string.appearance), heading = true)
        val palette = StyleCatalog.colors.map { StyleOption(it.id, getString(it.label), color = it.argb) }
        controls.choice(getString(R.string.text_color), listOf(StyleOption("automatic", getString(R.string.automatic))) + palette, draft.colorId) {
            change(draft.copy(colorId = it))
        }
        controls.choice(getString(R.string.typeface), ClockFont.available().map {
            StyleOption(it.id, getString(it.label), typeface = it.typeface)
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
        persist()
        updatePreview()
    }

    private fun updatePreview() {
        preview.background = android.graphics.drawable.GradientDrawable().apply {
            setColor(if (lightPreview) Color.rgb(229, 225, 214) else Color.rgb(43, 53, 59))
            cornerRadius = dp(18).toFloat()
        }
        preview.show(draft, options, singleLine)
    }

    private fun persist(): Boolean {
        if (!ownsWidget()) { finish(); return false }
        // Commit before navigation; publication is owned by the receiver so it survives exit.
        val store = WidgetStyleStore(this)
        val changed = !store.contains(widgetId) || store.read(widgetId) != draft
        val saved = store.save(widgetId, draft, useAsDefault.isChecked)
        hasSaveFailure = !saved
        saveStatus.setText(if (saved) R.string.saved_status else R.string.saving_failed_status)
        if (saved) {
            if (changed) {
                val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider
                if (provider != null) {
                    sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).setComponent(provider)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId)))
                }
            }
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        } else {
            setResult(RESULT_CANCELED)
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show()
        }
        return saved
    }

    private fun ownsWidget(): Boolean {
        val provider = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId)?.provider ?: return false
        return provider == ComponentName(this, DutchTimeWidgetReceiver::class.java) ||
            provider == ComponentName(this, DutchTimeRowWidgetReceiver::class.java)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("language", draft.languageCode)
        outState.putString("color", draft.colorId)
        outState.putString("font", draft.fontId)
        outState.putInt("size", draft.sizePercent)
        outState.putString("alignment", draft.alignment.name)
        outState.putString("background", draft.backgroundId)
        outState.putInt("opacity", draft.backgroundOpacity)
        outState.putInt("radius", draft.cornerRadius)
        outState.putBoolean("lightPreview", lightPreview)
        if (::useAsDefault.isInitialized) outState.putBoolean("useAsDefault", useAsDefault.isChecked)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
