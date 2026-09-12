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
) {
    fun sanitized() = copy(
        colorId = colorId.takeIf { it == "automatic" || StyleCatalog.color(it) != null } ?: "automatic",
        fontId = fontId.takeIf { it in StyleCatalog.fontIds } ?: "automatic",
        sizePercent = sizePercent.takeIf { it in setOf(70, 85, 100) } ?: 100,
        backgroundId = backgroundId.takeIf { it == "transparent" || StyleCatalog.color(it) != null } ?: "transparent",
        backgroundOpacity = backgroundOpacity.coerceIn(0, 100),
        cornerRadius = cornerRadius.takeIf { it in setOf(0, 16, 32) } ?: 16,
    )
}
