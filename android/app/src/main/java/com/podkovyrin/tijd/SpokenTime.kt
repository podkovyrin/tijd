package com.podkovyrin.tijd

import java.util.Locale

/** The native catalog is the source of truth; only stable language codes are persisted. */
internal object SpokenTime {
    init { System.loadLibrary("spoken-time-jni") }

    data class Language(val code: String, val name: String)

    val languages: List<Language> = nativeLanguages().toList().chunked(2).map { Language(it[0], it[1]) }
    private val indices = languages.mapIndexed { index, language -> language.code to index }.toMap()

    fun defaultLanguage(locale: Locale = Locale.getDefault()): String {
        val language = locale.language
        val candidates = listOf(
            "$language-${locale.script}-${locale.country}",
            "$language-${locale.script}",
            "$language-${locale.country}",
            when (language) {
                "zh" -> if (locale.country in setOf("TW", "HK", "MO")) "zh-Hant" else "zh-Hans"
                "pa" -> if (locale.country == "PK") "pa-Arab" else "pa-Guru"
                // The native catalog shares one entry for these supported languages.
                "sr", "hr", "bs" -> "sh"
                else -> language
            },
            language,
        )
        return candidates.firstOrNull { it in indices } ?: "en"
    }

    fun format(code: String, hourOfDay: Int, minute: Int): String {
        require(hourOfDay in 0..23 && minute in 0..59) { "Invalid time" }
        val index = indices.getValue(code)
        return nativeFormat(index, hourOfDay, minute).toString(Charsets.UTF_8)
    }

    private external fun nativeLanguages(): Array<String>
    private external fun nativeFormat(index: Int, hour: Int, minute: Int): ByteArray
}
