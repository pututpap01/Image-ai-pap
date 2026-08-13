package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PromptPreset(
    val title: String,
    val promptText: String
)

object PresetPrompts {
    val list = listOf(
        PromptPreset("🏰 Istana Melayang", "A majestic glowing castle floating in pastel clouds, golden hour sunset, intricate fantasy architecture"),
        PromptPreset("🐱 Kucing Cyberpunk", "Futuristic cyberpunk cat wearing glowing neon goggles in a rainy Tokyo alleyway, 8k render"),
        PromptPreset("🐉 Naga Kristal", "Ethereal dragon made of translucent amethyst crystals perched on dark cliff, glowing core"),
        PromptPreset("🚀 Stasiun Luar Angkasa", "Cinematic view of futuristic solar-powered space station orbiting a ringed blue planet"),
        PromptPreset("🌸 Samurai Anime", "Anime female samurai standing under blooming cherry blossom trees, dramatic wind, soft sunlight"),
        PromptPreset("🤖 Robot Solarpunk", "Friendly robot gardener tending to lush vibrant greenhouse flowers, solarpunk aesthetic")
    )
}

@Composable
fun PromptPresetChips(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Inspirasi Ide Prompt",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PresetPrompts.list) { item ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onPromptSelected(item.promptText) }
                        .testTag("preset_prompt_chip"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    tonalElevation = 1.dp
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
