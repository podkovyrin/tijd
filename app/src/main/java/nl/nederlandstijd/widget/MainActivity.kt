package nl.nederlandstijd.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
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

/** Only the setup needed to grant minute updates and add the widget. */
class MainActivity : Activity() {
    private lateinit var permissionStatus: TextView
    private lateinit var permissionButton: Button
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val spacing = (16 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(spacing, spacing, spacing, spacing)
        }
        fun label(resource: Int) = TextView(this).apply {
            setText(resource)
            setPadding(0, 0, 0, spacing)
            content.addView(this)
        }
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
        val scroll = ScrollView(this).apply { addView(content) }
        scroll.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            }
            insets
        }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        val allowed = MinuteScheduler.canSchedule(this)
        permissionStatus.setText(if (allowed) R.string.permission_granted else R.string.permission_missing)
        permissionButton.visibility = if (allowed) View.GONE else View.VISIBLE
        MinuteScheduler.scheduleNext(this)
        activityScope.launch { MinuteScheduler.refresh(this@MainActivity) }
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
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
