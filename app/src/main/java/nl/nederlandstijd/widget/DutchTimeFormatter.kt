package nl.nederlandstijd.widget

/** Formats the local time in lowercase Dutch words, without a prefix or day-part label. */
object DutchTimeFormatter {
    private val numberWords = listOf(
        "nul", "één", "twee", "drie", "vier", "vijf", "zes", "zeven", "acht", "negen",
        "tien", "elf", "twaalf", "dertien", "veertien", "vijftien", "zestien",
        "zeventien", "achttien", "negentien",
    )

    fun format(hourOfDay: Int, minute: Int): String {
        require(hourOfDay in 0..23) { "hourOfDay must be between 0 and 23" }
        require(minute in 0..59) { "minute must be between 0 and 59" }
        val currentHour = (hourOfDay + 11) % 12 + 1
        val nextHour = currentHour % 12 + 1
        val currentHourWord = numberWords[currentHour]
        val nextHourWord = numberWords[nextHour]

        return when (minute) {
            0 -> "$currentHourWord uur"
            15 -> "kwart over $currentHourWord"
            30 -> "half $nextHourWord"
            45 -> "kwart voor $nextHourWord"
            in 20..29 -> "${numberWords[30 - minute]} voor half $nextHourWord"
            in 31..40 -> "${numberWords[minute - 30]} over half $nextHourWord"
            in 1..19 -> "${numberWords[minute]} over $currentHourWord"
            else -> "${numberWords[60 - minute]} voor $nextHourWord"
        }
    }
}
