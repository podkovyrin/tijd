package com.podkovyrin.tijd

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.R as MaterialR

/** Shared Material 3 components for the app; widget rendering keeps its independent style. */
internal fun Activity.settingsContent(header: View? = null, footer: View? = null): LinearLayout {
    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(24), dp(16), dp(24), dp(28))
    }
    val scroll = ScrollView(this).apply { isFillViewport = true; addView(content) }
    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        header?.let { addView(it) }
        addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        footer?.let { addView(it) }
        setBackgroundColor(getColor(R.color.ui_screen))
        setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            }
            insets
        }
    }
    setContentView(root)
    return content
}

internal fun android.content.Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()

internal fun LinearLayout.label(text: CharSequence, heading: Boolean = false): TextView = MaterialTextView(context).apply {
    this.text = text
    setTextAppearance(if (heading) MaterialR.style.TextAppearance_Material3_HeadlineSmall else MaterialR.style.TextAppearance_Material3_BodyMedium)
    setPadding(0, context.dp(if (heading) 20 else 8), 0, context.dp(8))
    setTextColor(context.getColor(if (heading) R.color.ui_ink else R.color.ui_muted))
    this@label.addView(this)
}

internal data class StyleOption(val id: String, val name: String, val color: Int? = null, val typeface: Typeface? = null) {
    override fun toString() = name
}

internal fun LinearLayout.choice(label: String, choices: List<StyleOption>, selected: String, changed: (String) -> Unit): MaterialAutoCompleteTextView {
    val field = LayoutInflater.from(context).inflate(R.layout.style_dropdown, this, false) as TextInputLayout
    field.hint = label
    val menu = field.editText as MaterialAutoCompleteTextView
    menu.id = View.generateViewId()
    menu.contentDescription = label
    menu.setOnClickListener { menu.showDropDown() }
    field.setEndIconOnClickListener { menu.showDropDown() }
    menu.setAdapter(object : ArrayAdapter<StyleOption>(context, android.R.layout.simple_list_item_1, choices) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            (super.getView(position, convertView, parent) as TextView).apply {
                val option = getItem(position)!!
                text = option.name
                setTextAppearance(MaterialR.style.TextAppearance_Material3_BodyLarge)
                typeface = option.typeface ?: Typeface.DEFAULT
                setTextColor(context.getColor(R.color.ui_ink))
                minimumHeight = context.dp(48)
                compoundDrawablePadding = context.dp(12)
                val swatch = option.color?.let { color -> GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setStroke(context.dp(1), context.getColor(R.color.ui_outline))
                    setBounds(0, 0, context.dp(24), context.dp(24))
                } }
                setCompoundDrawablesRelative(swatch, null, null, null)
            }
    })
    val current = choices.firstOrNull { it.id == selected } ?: choices.first()
    menu.setText(current.name, false)
    menu.tag = current.id
    menu.setOnItemClickListener { parent, _, position, _ ->
        val option = parent.getItemAtPosition(position) as? StyleOption ?: return@setOnItemClickListener
        menu.tag = option.id
        changed(option.id)
    }
    addView(field, LinearLayout.LayoutParams(-1, -2).apply { topMargin = context.dp(12); bottomMargin = context.dp(4) })
    return menu
}

internal fun LinearLayout.card(): LinearLayout {
    val card = MaterialCardView(context).apply {
        setCardBackgroundColor(context.getColor(R.color.ui_card))
        cardElevation = 0f
        strokeWidth = 0
        radius = context.dp(16).toFloat()
    }
    val body = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(16))
    }
    card.addView(body)
    addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = context.dp(16) })
    return body
}

internal fun LinearLayout.action(text: String, primary: Boolean = false, clicked: () -> Unit): MaterialButton =
    (LayoutInflater.from(context).inflate(if (primary) R.layout.button_primary else R.layout.button_tonal, this, false) as MaterialButton).apply {
        this.text = text
        iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        setOnClickListener { clicked() }
        this@action.addView(this)
    }
