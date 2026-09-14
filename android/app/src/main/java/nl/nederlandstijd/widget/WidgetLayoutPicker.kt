package nl.nederlandstijd.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope

/** Native horizontal paging, with the actual footprint visible against a neutral sheet. */
internal object WidgetLayoutPicker {
    fun show(context: Context, scope: CoroutineScope, add: (Boolean) -> Unit): BottomSheetDialog {
        val dialog = BottomSheetDialog(context)
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(8), context.dp(8), context.dp(8), context.dp(16))
        }
        val toolbar = MaterialToolbar(context).apply {
            setTitle(R.string.choose_layout)
            setNavigationIcon(R.drawable.ic_close)
            setNavigationIconTint(context.getColor(R.color.ui_ink))
            setNavigationContentDescription(R.string.close_picker)
            setNavigationOnClickListener { dialog.dismiss() }
        }
        body.addView(toolbar, LinearLayout.LayoutParams(-1, context.dp(56)))
        val tabs = TabLayout(context)
        body.addView(tabs, LinearLayout.LayoutParams(-1, context.dp(48)))
        val pagerHeight = (context.resources.configuration.screenHeightDp - 240).coerceIn(120, 280)
        val pager = ViewPager2(context).apply {
            id = R.id.layout_pager
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
            adapter = object : RecyclerView.Adapter<Page>() {
                override fun getItemCount() = 2
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Page = Page(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = ViewGroup.LayoutParams(-1, -1)
                    setPadding(context.dp(16), context.dp(12), context.dp(16), 0)
                })
                override fun onBindViewHolder(holder: Page, position: Int) {
                    val page = holder.itemView as LinearLayout
                    page.removeAllViews()
                    val row = position == 1
                    val stage = FrameLayout(context)
                    page.addView(stage, LinearLayout.LayoutParams(-1, 0, 1f))
                    val preview = WidgetPreview(context, scope)
                    preview.minimumHeight = 0
                    // The colored surface belongs to the widget footprint, not the whole page.
                    val compactSize = (pagerHeight - 76).coerceIn(64, 192)
                    stage.addView(preview, FrameLayout.LayoutParams(
                        if (row) -1 else context.dp(compactSize),
                        context.dp(if (row) 56 else compactSize), Gravity.CENTER,
                    ))
                    val options = Bundle().apply {
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, if (row) 300 else compactSize)
                        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, if (row) 56 else compactSize)
                    }
                    preview.show(WidgetStyleStore(context).defaults(), options, row)
                    page.label(context.getString(if (row) R.string.row_description else R.string.compact_description)).apply {
                        gravity = Gravity.CENTER
                    }
                }
            }
        }
        body.addView(pager, LinearLayout.LayoutParams(-1, context.dp(pagerHeight)))
        val mediator = TabLayoutMediator(tabs, pager) { tab, position ->
            tab.setText(if (position == 0) R.string.compact_widget else R.string.single_row_widget)
        }
        mediator.attach()
        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), 0, context.dp(16), 0)
        }
        body.addView(actions)
        actions.action(context.getString(R.string.add_this_widget), primary = true) {
            val row = pager.currentItem == 1
            dialog.dismiss()
            add(row)
        }.setIconResource(R.drawable.ic_add)
        dialog.setContentView(body)
        dialog.setOnDismissListener { mediator.detach() }
        dialog.show()
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private class Page(view: LinearLayout) : RecyclerView.ViewHolder(view)
}
