package com.example.ui.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector

data class ArtStyle(
    val id: String,
    val name: String,
    val description: String,
    val promptSuffix: String,
    val negativePromptSuffix: String = "",
    val icon: ImageVector,
    val category: String = "Popular"
)

object ArtStylePresets {
    val defaultStyle = ArtStyle(
        id = "none",
        name = "Tanpa Gaya",
        description = "Standard dynamic output",
        promptSuffix = ", high detailed, masterpiece, 8k resolution",
        icon = Icons.Default.AutoAwesome,
        category = "General"
    )

    val list = listOf(
        defaultStyle,
        ArtStyle(
            id = "anime",
            name = "Anime & Manga",
            description = "Japanese anime illustration style",
            promptSuffix = ", anime style, detailed studio ghibli, makoto shinkai style, vibrant colors, crisp lines, 8k wallpaper",
            negativePromptSuffix = ", 3d, realistic photorealistic",
            icon = Icons.Default.Face,
            category = "Illustration"
        ),
        ArtStyle(
            id = "cinematic",
            name = "Cinematic Photo",
            description = "Hyper-realistic movie shot",
            promptSuffix = ", cinematic lighting, 35mm photograph, shot on Hasselblad, dramatic atmosphere, photorealistic, 8k ultra hd, depth of field",
            negativePromptSuffix = ", drawing, anime, cartoon, painting, illustration",
            icon = Icons.Default.CameraAlt,
            category = "Realistic"
        ),
        ArtStyle(
            id = "cyberpunk",
            name = "Cyberpunk Neon",
            description = "Futuristic neon city vibes",
            promptSuffix = ", cyberpunk style, glowing neon lights, futuristic city background, volumetric fog, rainy reflections, magenta and cyan color palette, octane render",
            icon = Icons.Default.FlashOn,
            category = "Sci-Fi"
        ),
        ArtStyle(
            id = "dark_fantasy",
            name = "Dark Fantasy",
            description = "Eldritch, gothic fantasy art",
            promptSuffix = ", dark fantasy art style, intricate gothic details, dark moody lighting, hyper-detailed, trending on artstation",
            icon = Icons.Default.Shield,
            category = "Fantasy"
        ),
        ArtStyle(
            id = "3d_render",
            name = "3D Render",
            description = "Blender / Unreal Engine 5",
            promptSuffix = ", 3d digital artwork, Pixar style, raytracing, smooth textures, subsurface scattering, octane render 8k",
            icon = Icons.Default.VideogameAsset,
            category = "Digital"
        ),
        ArtStyle(
            id = "oil_painting",
            name = "Lukisan Cat Minyak",
            description = "Classic oil impasto texture",
            promptSuffix = ", fine art oil painting, visible thick brushstrokes, rich canvas texture, masterwork fine art composition",
            icon = Icons.Default.Brush,
            category = "Artistic"
        ),
        ArtStyle(
            id = "pixel_art",
            name = "Pixel Art 16-Bit",
            description = "Retro game sprite art",
            promptSuffix = ", 16-bit pixel art style, crisp retro video game graphic, detailed pixel shading, vibrant palette",
            icon = Icons.Default.GridOn,
            category = "Retro"
        ),
        ArtStyle(
            id = "watercolor",
            name = "Cat Air (Watercolor)",
            description = "Soft translucent watercolor paper",
            promptSuffix = ", soft watercolor painting, elegant fluid paint splatters, textured paper canvas, pastel color tones",
            icon = Icons.Default.ColorLens,
            category = "Artistic"
        ),
        ArtStyle(
            id = "studio_light",
            name = "Studio Portrait",
            description = "Professional lighting setup",
            promptSuffix = ", studio lighting portrait, soft box rim light, neutral background, crisp details, commercial photo",
            icon = Icons.Default.Lightbulb,
            category = "Realistic"
        ),
        ArtStyle(
            id = "synthwave",
            name = "Synthwave 80s",
            description = "Retro synthwave aesthetic",
            promptSuffix = ", synthwave retro 80s aesthetic, grid sunset, neon glowing purple and orange, wireframe horizon",
            icon = Icons.Default.Palette,
            category = "Retro"
        ),
        ArtStyle(
            id = "landscape",
            name = "Epic Landscape",
            description = "Wide majestic scenery",
            promptSuffix = ", majestic landscape photograph, golden hour sunlight, sweeping vistas, national geographic shot, ultra wide angle",
            icon = Icons.Default.Landscape,
            category = "Nature"
        )
    )
}
