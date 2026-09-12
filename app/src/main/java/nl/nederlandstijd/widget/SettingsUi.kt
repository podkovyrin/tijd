package nl.nederlandstijd.widget

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView

/** Small native control helpers shared by setup and the widget editor. */
internal fun Activity.settingsContent(header: View? = null, footer: View? = null): LinearLayout {
    val content = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(20), dp(20), dp(28))
    }
    val scroll = ScrollView(this).apply {
        isFillViewport = true
        addView(content)
    }
    val root = if (header == null && footer == null) scroll else LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        header?.let { addView(it) }
        addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        footer?.let { addView(it) }
    }
    root.apply {
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

internal fun LinearLayout.label(text: CharSequence, heading: Boolean = false): TextView = TextView(context).apply {
    this.text = text
    setTextAppearance(if (heading) android.R.style.TextAppearance_Material_Headline else android.R.style.TextAppearance_Material_Body1)
    setPadding(0, context.dp(if (heading) 20 else 8), 0, context.dp(8))
    this@label.addView(this)
}

internal data class StyleOption(val id: String, val name: String, val color: Int? = null, val typeface: Typeface? = null)

internal fun LinearLayout.choice(label: String, choices: List<StyleOption>, selected: String, changed: (String) -> Unit): Spinner {
    val title = label(label).apply { id = View.generateViewId() }
    return Spinner(context).apply {
        minimumHeight = context.dp(48)
        contentDescription = label
        id = View.generateViewId()
        title.labelFor = id
        adapter = object : ArrayAdapter<StyleOption>(context, android.R.layout.simple_spinner_item, choices) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View = decorate(
                super.getView(position, convertView, parent) as TextView, position,
            )
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View = decorate(
                super.getDropDownView(position, convertView, parent) as TextView, position,
            )
            private fun decorate(view: TextView, position: Int): TextView = view.apply {
                val option = choices[position]
                text = option.name
                typeface = option.typeface ?: Typeface.DEFAULT
                minimumHeight = context.dp(48)
                compoundDrawablePadding = context.dp(12)
                val swatch = option.color?.let { color ->
                    GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(color)
                        setStroke(context.dp(1).coerceAtLeast(1), 0x80808080.toInt())
                        setBounds(0, 0, context.dp(24), context.dp(24))
                    }
                }
                setCompoundDrawablesRelative(swatch, null, null, null)
            }
        }.apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        setSelection(choices.indexOfFirst { it.id == selected }.coerceAtLeast(0))
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = changed(choices[position].id)
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        this@choice.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }
}
