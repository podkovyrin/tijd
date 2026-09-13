package nl.nederlandstijd.widget

import android.app.Activity
import android.app.AlertDialog
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
class MainActivity : Activity() {
    private lateinit var widgetList: LinearLayout
    private lateinit var permissionStatus: TextView
    private lateinit var permissionButton: Button
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = settingsContent()
        fun label(resource: Int) = content.label(getString(resource))
        label(R.string.app_name).setTextAppearance(android.R.style.TextAppearance_Material_Headline)
        label(R.string.setup_description)
        label(R.string.permission_description)
        permissionStatus = label(R.string.permission_missing)
        permissionButton = Button(this).apply {
            setText(R.string.allow_updates)
            setOnClickListener { requestMinutePermission() }
            content.addView(this)
        }
        Button(this).apply {
            setText(R.string.add_widget)
            setOnClickListener { addWidget(DutchTimeWidgetReceiver::class.java) }
            content.addView(this)
        }
        Button(this).apply {
            setText(R.string.add_row_widget)
            setOnClickListener { addWidget(DutchTimeRowWidgetReceiver::class.java) }
            content.addView(this)
        }
        label(R.string.lock_screen_note)
        content.label(getString(R.string.my_widgets), heading = true)
        widgetList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(widgetList)
        Button(this).apply {
            setText(R.string.license_acknowledgments)
            setOnClickListener { showLicenseText(R.string.license_acknowledgments, R.raw.project_notice, true) }
            content.addView(this)
        }
    }

    private fun showLicenseText(title: Int, resource: Int, showFullLicense: Boolean = false) {
        val text = TextView(this).apply {
            text = resources.openRawResource(resource).bufferedReader().use { it.readText() }
            setPadding(dp(20), dp(16), dp(20), dp(16))
            setTextIsSelectable(true)
            Linkify.addLinks(this, Linkify.WEB_URLS)
            movementMethod = LinkMovementMethod.getInstance()
        }
        val scroll = ScrollView(this).apply { addView(text) }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .apply {
                if (showFullLicense) {
                    setNeutralButton(R.string.full_license) { _, _ ->
                        showLicenseText(R.string.full_license, R.raw.project_license)
                    }
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        val allowed = MinuteScheduler.canSchedule(this)
        permissionStatus.setText(if (allowed) R.string.permission_granted else R.string.permission_missing)
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
        if (ids.isEmpty()) widgetList.label(getString(R.string.my_widgets_empty))
        for (id in ids) {
            val info = manager.getAppWidgetInfo(id) ?: continue
            val row = info.provider == ComponentName(this, DutchTimeRowWidgetReceiver::class.java)
            val type = getString(if (row) R.string.single_row_widget else R.string.compact_widget)
            widgetList.label(getString(R.string.widget_card, type, id))
            val preview = WidgetPreview(this, activityScope)
            widgetList.addView(preview, LinearLayout.LayoutParams(-1, dp(140)))
            preview.show(WidgetStyleStore(this).read(id), manager.getAppWidgetOptions(id), row)
            widgetList.addView(Button(this).apply {
                setText(R.string.edit_style)
                contentDescription = getString(R.string.edit_widget_title, id)
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, WidgetConfigurationActivity::class.java)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                }
            })
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
            manager.requestPinAppWidget(ComponentName(this, receiver), null, null)
        } else {
            Toast.makeText(this, R.string.add_widget_manually, Toast.LENGTH_LONG).show()
        }
    }
}
