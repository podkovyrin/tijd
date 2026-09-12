package nl.nederlandstijd.widget

/** Stable IDs keep saved styles independent of display names and palette ordering. */
internal object StyleCatalog {
    data class Color(val id: String, val name: String, val argb: Int)

    val colors: List<Color> = listOf(
        Color("white", "White", 0xFFFFFFFF.toInt()),
        Color("ivory", "Ivory", 0xFFFFF8E7.toInt()),
        Color("cream", "Cream", 0xFFF2E8CF.toInt()),
        Color("sand", "Sand", 0xFFD8C3A5.toInt()),
        Color("stone", "Stone", 0xFFB8B2A7.toInt()),
        Color("silver", "Silver", 0xFFCFD4DA.toInt()),
        Color("slate", "Slate", 0xFF64748B.toInt()),
        Color("charcoal", "Charcoal", 0xFF343A40.toInt()),
        Color("ink", "Ink", 0xFF17212B.toInt()),
        Color("black", "Black", 0xFF101010.toInt()),
        Color("blush", "Blush", 0xFFF2C6C2.toInt()),
        Color("rose", "Rose", 0xFFD9899E.toInt()),
        Color("raspberry", "Raspberry", 0xFFB23A67.toInt()),
        Color("burgundy", "Burgundy", 0xFF722F45.toInt()),
        Color("coral", "Coral", 0xFFEE8B78.toInt()),
        Color("terracotta", "Terracotta", 0xFFB9654D.toInt()),
        Color("rust", "Rust", 0xFF934B32.toInt()),
        Color("peach", "Peach", 0xFFFFD2AF.toInt()),
        Color("apricot", "Apricot", 0xFFEAAF78.toInt()),
        Color("amber", "Amber", 0xFFDE9B35.toInt()),
        Color("honey", "Honey", 0xFFEBCB75.toInt()),
        Color("lemon", "Lemon", 0xFFF5E6A1.toInt()),
        Color("sage", "Sage", 0xFFAABDA0.toInt()),
        Color("olive", "Olive", 0xFF7C8651.toInt()),
        Color("moss", "Moss", 0xFF536B4F.toInt()),
        Color("forest", "Forest", 0xFF254F3D.toInt()),
        Color("mint", "Mint", 0xFFB9E4CF.toInt()),
        Color("seafoam", "Seafoam", 0xFF83C5BE.toInt()),
        Color("teal", "Teal", 0xFF287D82.toInt()),
        Color("petrol", "Petrol", 0xFF1E505C.toInt()),
        Color("ice", "Ice", 0xFFD0E7F2.toInt()),
        Color("sky", "Sky", 0xFF9CC8E2.toInt()),
        Color("denim", "Denim", 0xFF5A85AD.toInt()),
        Color("cobalt", "Cobalt", 0xFF355DA8.toInt()),
        Color("navy", "Navy", 0xFF253B61.toInt()),
        Color("periwinkle", "Periwinkle", 0xFFAAB6E8.toInt()),
        Color("lavender", "Lavender", 0xFFD2C4E8.toInt()),
        Color("lilac", "Lilac", 0xFFB497C4.toInt()),
        Color("plum", "Plum", 0xFF785578.toInt()),
        Color("aubergine", "Aubergine", 0xFF4B354F.toInt()),
    )
    private val colorsById = colors.associateBy { it.id }
    fun color(id: String): Color? = colorsById[id]

    val fontIds = setOf(
        "automatic", "sans", "rounded", "google", "serif", "mono", "condensed",
        "serif-mono", "sans-light", "sans-black", "casual", "cursive",
    )
}
