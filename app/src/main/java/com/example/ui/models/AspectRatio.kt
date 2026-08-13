package com.example.ui.models

data class AspectOption(
    val label: String,
    val ratio: String, // e.g. "1:1", "16:9"
    val width: Int,
    val height: Int,
    val description: String
)

object AspectRatioPresets {
    val options = listOf(
        AspectOption("1:1", "1:1", 1024, 1024, "Persegi (Feed IG)"),
        AspectOption("9:16", "9:16", 768, 1344, "Potret (Story / Wallpaper)"),
        AspectOption("16:9", "16:9", 1344, 768, "Lansekap (Desktop / YouTube)"),
        AspectOption("4:3", "4:3", 1024, 768, "Standar Foto"),
        AspectOption("3:4", "3:4", 768, 1024, "Potret Cetak")
    )

    val defaultOption = options[0]
}
