package nl.nederlandstijd.widget

internal enum class WidgetAlignment { AUTO, START, CENTER, END }

internal data class WidgetStyle(
    val colorId: String = "automatic",
    val fontId: String = "automatic",
    val sizePercent: Int = 100,
    val alignment: WidgetAlignment = WidgetAlignment.AUTO,
    val backgroundId: String = "transparent",
    val backgroundOpacity: Int = 60,
    val cornerRadius: Int = 16,
    val languageCode: String = SpokenTime.defaultLanguage(),
) {
    init {
        require(SpokenTime.languages.any { it.code == languageCode }) { "Unknown language: $languageCode" }
        require(colorId == "automatic" || StyleCatalog.color(colorId) != null) { "Unknown color: $colorId" }
        require(fontId in StyleCatalog.fontIds) { "Unknown font: $fontId" }
        require(sizePercent in setOf(70, 85, 100)) { "Invalid text size: $sizePercent" }
        require(backgroundId == "transparent" || StyleCatalog.color(backgroundId) != null) { "Unknown background: $backgroundId" }
        require(backgroundOpacity in 0..100) { "Invalid background opacity: $backgroundOpacity" }
        require(cornerRadius in setOf(0, 16, 32)) { "Invalid corner radius: $cornerRadius" }
    }
}
