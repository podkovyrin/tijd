package com.podkovyrin.tijd

/** Stable IDs keep saved styles independent of display names and palette ordering. */
internal object StyleCatalog {
    data class Color(val id: String, val name: String, val argb: Int, val label: Int)

    val colors: List<Color> = listOf(
        Color("white", "White", 0xFFFFFFFF.toInt(), R.string.color_white),
        Color("ivory", "Ivory", 0xFFFFF8E7.toInt(), R.string.color_ivory),
        Color("cream", "Cream", 0xFFF2E8CF.toInt(), R.string.color_cream),
        Color("sand", "Sand", 0xFFD8C3A5.toInt(), R.string.color_sand),
        Color("stone", "Stone", 0xFFB8B2A7.toInt(), R.string.color_stone),
        Color("silver", "Silver", 0xFFCFD4DA.toInt(), R.string.color_silver),
        Color("slate", "Slate", 0xFF64748B.toInt(), R.string.color_slate),
        Color("charcoal", "Charcoal", 0xFF343A40.toInt(), R.string.color_charcoal),
        Color("ink", "Ink", 0xFF17212B.toInt(), R.string.color_ink),
        Color("black", "Black", 0xFF101010.toInt(), R.string.color_black),
        Color("blush", "Blush", 0xFFF2C6C2.toInt(), R.string.color_blush),
        Color("rose", "Rose", 0xFFD9899E.toInt(), R.string.color_rose),
        Color("raspberry", "Raspberry", 0xFFB23A67.toInt(), R.string.color_raspberry),
        Color("burgundy", "Burgundy", 0xFF722F45.toInt(), R.string.color_burgundy),
        Color("coral", "Coral", 0xFFEE8B78.toInt(), R.string.color_coral),
        Color("terracotta", "Terracotta", 0xFFB9654D.toInt(), R.string.color_terracotta),
        Color("rust", "Rust", 0xFF934B32.toInt(), R.string.color_rust),
        Color("peach", "Peach", 0xFFFFD2AF.toInt(), R.string.color_peach),
        Color("apricot", "Apricot", 0xFFEAAF78.toInt(), R.string.color_apricot),
        Color("amber", "Amber", 0xFFDE9B35.toInt(), R.string.color_amber),
        Color("honey", "Honey", 0xFFEBCB75.toInt(), R.string.color_honey),
        Color("lemon", "Lemon", 0xFFF5E6A1.toInt(), R.string.color_lemon),
        Color("sage", "Sage", 0xFFAABDA0.toInt(), R.string.color_sage),
        Color("olive", "Olive", 0xFF7C8651.toInt(), R.string.color_olive),
        Color("moss", "Moss", 0xFF536B4F.toInt(), R.string.color_moss),
        Color("forest", "Forest", 0xFF254F3D.toInt(), R.string.color_forest),
        Color("mint", "Mint", 0xFFB9E4CF.toInt(), R.string.color_mint),
        Color("seafoam", "Seafoam", 0xFF83C5BE.toInt(), R.string.color_seafoam),
        Color("teal", "Teal", 0xFF287D82.toInt(), R.string.color_teal),
        Color("petrol", "Petrol", 0xFF1E505C.toInt(), R.string.color_petrol),
        Color("ice", "Ice", 0xFFD0E7F2.toInt(), R.string.color_ice),
        Color("sky", "Sky", 0xFF9CC8E2.toInt(), R.string.color_sky),
        Color("denim", "Denim", 0xFF5A85AD.toInt(), R.string.color_denim),
        Color("cobalt", "Cobalt", 0xFF355DA8.toInt(), R.string.color_cobalt),
        Color("navy", "Navy", 0xFF253B61.toInt(), R.string.color_navy),
        Color("periwinkle", "Periwinkle", 0xFFAAB6E8.toInt(), R.string.color_periwinkle),
        Color("lavender", "Lavender", 0xFFD2C4E8.toInt(), R.string.color_lavender),
        Color("lilac", "Lilac", 0xFFB497C4.toInt(), R.string.color_lilac),
        Color("plum", "Plum", 0xFF785578.toInt(), R.string.color_plum),
        Color("aubergine", "Aubergine", 0xFF4B354F.toInt(), R.string.color_aubergine),
    )
    private val colorsById = colors.associateBy { it.id }
    fun color(id: String): Color? = colorsById[id]

    val fontIds = setOf(
        "automatic", "sans", "rounded", "google", "serif", "mono", "condensed",
        "serif-mono", "sans-light", "sans-black", "casual", "cursive",
    )
}
