package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prompt: String,
    val enhancedPrompt: String = "",
    val negativePrompt: String = "",
    val style: String,
    val aspectRatio: String,
    val imageUrl: String,
    val engine: String, // "perchance" or "direct"
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
