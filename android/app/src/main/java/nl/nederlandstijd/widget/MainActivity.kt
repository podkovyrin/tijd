package nl.nederlandstijd.widget

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Setup and an inventory of installed widget instances; editing lives in its own activity. */
class MainActivity : AppCompatActivity() {
    private lateinit var widgetList: LinearLayout
    private lateinit var permissionStatus: TextView
    private lateinit var permissionDescription: TextView
    private lateinit var permissionButton: Button
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = settingsContent()
        content.label(getString(R.string.welcome_title), heading = true).apply {
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_DisplaySmall)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setPadding(0, dp(8), 0, dp(4))
        }
        content.label(getString(R.string.setup_description)).setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
        content.action(getString(R.string.add_widget), primary = true) { chooseLayout() }.setIconResource(R.drawable.ic_add)
        content.label(getString(R.string.my_widgets), heading = true)
        content.label(getString(R.string.widget_collection_note))
        widgetList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(widgetList)
        val permissionCard = content.card()
        permissionStatus = permissionCard.label(getString(R.string.permission_missing)).apply {
            setTextColor(getColor(R.color.ui_ink))
            textSize = 16f
        }
        permissionDescription = permissionCard.label(getString(R.string.permission_description))
        permissionButton = permissionCard.action(getString(R.string.allow_updates)) { requestMinutePermission() }
        content.action(getString(R.string.privacy_policy)) {
            showDocumentText(R.string.privacy_policy, R.raw.privacy_policy)
        }.setIconResource(R.drawable.ic_info)
        content.action(getString(R.string.license_acknowledgments)) {
            showDocumentText(R.string.license_acknowledgments, R.raw.project_notice, true)
        }.setIconResource(R.drawable.ic_info)
    }

    private fun showDocumentText(title: Int, resource: Int, showFullLicense: Boolean = false) {
        val text = TextView(this).apply {
            text = resources.openRawResource(resource).bufferedReader().use { it.readText() } +
                if (resource == R.raw.project_notice) "\n\n" + resources.openRawResource(R.raw.material_icons_license)
                    .bufferedReader().use {
                        // The appendix is an authoring template, not part of the reading view.
                        // Keep the bundled upstream license complete.
                        it.readText().substringBefore("\n   APPENDIX:").trimEnd()
                    } else ""
            setPadding(dp(20), dp(16), dp(20), dp(16))
            setTextIsSelectable(true)
            Linkify.addLinks(this, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES)
            movementMethod = LinkMovementMethod.getInstance()
        }
        val scroll = ScrollView(this).apply { addView(text) }
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .apply {
                if (showFullLicense) {
                    setNeutralButton(R.string.full_license) { _, _ ->
                        showDocumentText(R.string.full_license, R.raw.project_license)
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        val allowed = MinuteScheduler.canSchedule(this)
        permissionStatus.setText(if (allowed) R.string.permission_granted else R.string.permission_missing)
        permissionStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(if (allowed) R.drawable.ic_check else 0, 0, 0, 0)
        permissionStatus.compoundDrawablePadding = dp(8)
        permissionStatus.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.ui_accent))
        permissionDescription.visibility = if (allowed) View.GONE else View.VISIBLE
        permissionButton.visibility = if (allowed) View.GONE else View.VISIBLE
        showWidgets()
        MinuteScheduler.scheduleNext(this)
        activityScope.launch { MinuteScheduler.refresh(this@MainActivity) }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    private fun showWidgets() {
        widgetList.removeAllViews()
        val ids = MinuteScheduler.widgetIds(this).sorted()
        val manager = AppWidgetManager.getInstance(this)
        if (ids.isEmpty()) {
            val empty = widgetList.card()
            empty.label(getString(R.string.first_widget), heading = true)
            val preview = WidgetPreview(this, activityScope)
            empty.addView(preview, LinearLayout.LayoutParams(-1, dp(156)))
            preview.show(WidgetStyleStore(this).defaults(), Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 200)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 140)
            }, false)
            empty.label(getString(R.string.my_widgets_empty))
        }
        for (id in ids) {
            val info = manager.getAppWidgetInfo(id) ?: continue
            val row = info.provider == ComponentName(this, DutchTimeRowWidgetReceiver::class.java)
            val type = getString(if (row) R.string.single_row_widget else R.string.compact_widget)
            val style = WidgetStyleStore(this).read(id)
            val card = widgetList.card()
            card.label(type, heading = true).apply { setPadding(0, dp(4), 0, dp(4)); textSize = 20f }
            card.label(LanguagePicker.displayName(this, style.languageCode))
            val preview = WidgetPreview(this, activityScope)
            card.addView(preview, LinearLayout.LayoutParams(-1, dp(156)))
            preview.show(style, manager.getAppWidgetOptions(id), row)
            card.label(getString(R.string.tap_to_edit)).apply {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_edit, 0)
                compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.ui_accent))
                compoundDrawablePadding = dp(8)
                setTextColor(getColor(R.color.ui_accent))
                gravity = android.view.Gravity.END
            }
            // The Material card provides ripple, focus and a single accessible edit action.
            val target = card.parent as com.google.android.material.card.MaterialCardView
            target.isFocusable = true
            target.isClickable = true
            target.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            card.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            target.contentDescription = getString(
                R.string.widget_edit_accessibility, id, type, LanguagePicker.displayName(this, style.languageCode),
            )
            target.setOnClickListener {
                startActivity(Intent(this, WidgetConfigurationActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
            }
        }
    }

    private fun chooseLayout() {
        WidgetLayoutPicker.show(this, activityScope) { row ->
            addWidget(if (row) DutchTimeRowWidgetReceiver::class.java else DutchTimeWidgetReceiver::class.java)
        }
    }

    private fun requestMinutePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, R.string.permission_settings_unavailable, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addWidget(receiver: Class<out DutchTimeWidgetReceiver>) {
        val manager = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
            if (!manager.requestPinAppWidget(ComponentName(this, receiver), null, null)) {
                Toast.makeText(this, R.string.add_widget_manually, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, R.string.add_widget_manually, Toast.LENGTH_LONG).show()
        }
    }
}
