package nl.nederlandstijd.widget

import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import java.util.Locale

/** Flags are visual hints; names and script labels remain the language identifiers. */
internal object LanguagePicker {
    private val regions = ("af:ZA sq:AL am:ET ar:SA hy:AM az:AZ eu:ES bn:BD pt-BR:BR br:FR bg:BG " +
        "my:MM yue:HK ca:ES ceb:PH cs:CZ da:DK nl:NL arz:EG en:GB et:EE pt-PT:PT fil:PH fi:FI " +
        "fr:FR ka:GE de:DE el:GR gu:IN ha:NG he:IL hi:IN hu:HU is:IS ig:NG id:ID ga:IE it:IT " +
        "ja:JP jv:ID kn:IN kk:KZ km:KH ko:KR lo:LA lv:LV apc:LB lt:LT ms:MY ml:IN mt:MT " +
        "zh-Hans:CN zh-Hant:TW mr:IN ne:NP nb:NO or:IN om:ET ps:AF fa:IR pl:PL pt:PT " +
        "pa-Guru:IN pa-Arab:PK ro:RO ru:RU gd:GB sd:PK si:LK sk:SK sl:SI so:SO es:ES su:ID " +
        "sw:TZ sv:SE ta:IN te:IN th:TH tr:TR uk:UA ur:PK uz:UZ vi:VN cy:GB yo:NG zu:ZA")
        .split(" ").associate { it.substringBefore(':') to it.substringAfter(':') }

    private fun flag(code: String): String = regions[code]?.map {
        String(Character.toChars(0x1F1E6 + it.code - 'A'.code))
    }?.joinToString("") ?: "🌐"

    fun displayName(code: String, locale: Locale = Locale.getDefault()): String {
        val language = SpokenTime.languages.first { it.code == code }
        val localized = Locale.forLanguageTag(code).getDisplayName(locale)
            .takeUnless { it == code || it == Locale.forLanguageTag(code).language } ?: language.name
        return "${flag(code)}  $localized"
    }

    fun displayName(context: Context, code: String): String {
        val config = context.resources.configuration
        @Suppress("DEPRECATION")
        val locale = if (android.os.Build.VERSION.SDK_INT >= 24) config.locales[0] else config.locale
        return displayName(code, locale)
    }

    private fun nativeName(language: SpokenTime.Language): String {
        val locale = Locale.forLanguageTag(language.code)
        return locale.getDisplayLanguage(locale).takeUnless { it == locale.language }.orEmpty()
    }

    fun show(context: Context, selected: String, changed: (String) -> Unit): AlertDialog {
        val body = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(20), 0, context.dp(20), context.dp(8))
        }
        val searchField = android.view.LayoutInflater.from(context).inflate(R.layout.language_search, body, false)
            as com.google.android.material.textfield.TextInputLayout
        val search = searchField.editText!!
        body.addView(searchField)
        val empty = TextView(context).apply { setText(R.string.no_languages); setPadding(0, context.dp(16), 0, 0) }
        body.addView(empty)
        val list = ListView(context).apply { choiceMode = ListView.CHOICE_MODE_SINGLE }
        // Keep the preferred height, but shrink the measured viewport when the dialog
        // has less room (small displays or the search keyboard).
        body.addView(list, LinearLayout.LayoutParams(-1, context.dp(320), 1f))
        list.emptyView = empty
        var filtered = SpokenTime.languages
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_single_choice, mutableListOf())
        list.adapter = adapter
        fun filter(query: String) {
            filtered = SpokenTime.languages.filter {
                "${it.name} ${displayName(context, it.code)} ${nativeName(it)} ${it.code}".contains(query.trim(), ignoreCase = true)
            }
            adapter.clear()
            adapter.addAll(filtered.map {
                val native = nativeName(it)
                displayName(context, it.code) + if (native.isNotBlank() && !it.name.equals(native, true)) " · $native" else ""
            })
            list.clearChoices()
            val index = filtered.indexOfFirst { it.code == selected }
            if (index >= 0) list.setItemChecked(index, true)
        }
        filter("")
        list.setSelection(SpokenTime.languages.indexOfFirst { it.code == selected }.coerceAtLeast(0))
        val dialog = MaterialAlertDialogBuilder(context).setTitle(R.string.language_title).setView(body)
            .setBackgroundInsetTop(context.dp(8)).setBackgroundInsetBottom(context.dp(8))
            .setNegativeButton(android.R.string.cancel, null).create()
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(body) { _, insets ->
            // The search label supplies context while typing; reserve scarce vertical
            // space for a selectable result and the Cancel button above the keyboard.
            dialog.findViewById<android.view.View>(androidx.appcompat.R.id.topPanel)?.visibility =
                if (insets.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime())) android.view.View.GONE
                else android.view.View.VISIBLE
            insets
        }
        list.setOnItemClickListener { _, _, position, _ ->
            changed(filtered[position].code)
            dialog.dismiss()
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { filter(s.toString()) }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        dialog.show()
        return dialog
    }
}
