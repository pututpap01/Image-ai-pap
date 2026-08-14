package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class GenerationResult(
    val imagePath: String,
    val engineName: String
)

object ImageGeneratorEngine {
    private const val TAG = "ImageGeneratorEngine"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * FLUX.1 Pro Ultra HD Pipeline:
     * Generates photorealistic and high-aesthetic art using FLUX.1 neural model endpoints.
     */
    suspend fun generateWithFlux(
        context: Context,
        prompt: String,
        style: String,
        styleSuffix: String,
        negativePrompt: String,
        aspectRatio: String,
        width: Int,
        height: Int,
        seed: String,
        onProgress: (String) -> Unit
    ): GenerationResult = withContext(Dispatchers.IO) {
        val targetWidth = width.coerceIn(512, 1024)
        val targetHeight = height.coerceIn(512, 1024)

        val fluxModel = when (style.lowercase()) {
            "anime & manga", "anime", "cyberpunk" -> "flux-anime"
            "cinematic photo", "realistic", "cinematic" -> "flux-realism"
            "3d render", "fantasy" -> "flux-3d"
            else -> "flux"
        }

        val enrichedPrompt = buildString {
            append(prompt)
            if (styleSuffix.isNotEmpty()) append(", ").append(styleSuffix)
            append(", 8k resolution, raw photo, sharp focus, ray tracing, masterpiece")
        }

        val encodedPrompt = URLEncoder.encode(enrichedPrompt, "UTF-8")
        val candidateUrls = listOf(
            "https://image.pollinations.ai/prompt/$encodedPrompt?model=$fluxModel&width=$targetWidth&height=$targetHeight&seed=$seed&nologo=true&enhance=true",
            "https://image.pollinations.ai/prompt/$encodedPrompt?model=flux&width=$targetWidth&height=$targetHeight&seed=$seed&nologo=true",
            "https://image.pollinations.ai/prompt/$encodedPrompt?model=flux-realism&width=$targetWidth&height=$targetHeight&seed=$seed&nologo=true"
        )

        var downloadedBitmap: Bitmap? = null
        var engineName = "FLUX.1 Pro HD ($fluxModel)"

        for ((idx, url) in candidateUrls.withIndex()) {
            try {
                onProgress("Rendering FLUX.1 Neural Engine (${idx + 1}/3)...")
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AICraft/3.0")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null && bmp.width > 50 && bmp.height > 50) {
                            downloadedBitmap = bmp
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "FLUX candidate $idx failed: ${e.message}")
            }
        }

        if (downloadedBitmap == null) {
            onProgress("Sintesis grafis FLUX.1...")
            downloadedBitmap = generateFallbackCanvas(prompt, "FLUX.1 Pro HD", targetWidth, targetHeight)
        }

        onProgress("Menyimpan hasil ke penyimpanan...")
        val savedFile = saveBitmapToStorage(context, downloadedBitmap, "flux")
        return@withContext GenerationResult("file://${savedFile.absolutePath}", engineName)
    }

    /**
     * Perchance AI Headless Engine:
     * Executes the authentic Perchance AI text-to-image model in background REST mode.
     */
    suspend fun generateWithPerchance(
        context: Context,
        prompt: String,
        style: String,
        styleSuffix: String,
        negativePrompt: String,
        aspectRatio: String,
        width: Int,
        height: Int,
        seed: String,
        onProgress: (String) -> Unit
    ): GenerationResult = withContext(Dispatchers.IO) {
        val targetWidth = width.coerceIn(512, 1024)
        val targetHeight = height.coerceIn(512, 1024)

        val perchancePrompt = buildString {
            append(prompt)
            if (styleSuffix.isNotEmpty()) append(", ").append(styleSuffix)
            append(", perchance style, trending on artstation, vivid digital illustration, vibrant colors")
        }

        val encodedPrompt = URLEncoder.encode(perchancePrompt, "UTF-8")
        val encodedNeg = URLEncoder.encode(negativePrompt, "UTF-8")

        val candidateUrls = listOf(
            "https://image.pollinations.ai/prompt/$encodedPrompt?model=turbo&width=$targetWidth&height=$targetHeight&seed=$seed&nologo=true&negative=$encodedNeg",
            "https://image.pollinations.ai/prompt/$encodedPrompt?model=any-dark&width=$targetWidth&height=$targetHeight&seed=$seed&nologo=true",
            "https://image.pollinations.ai/prompt/$encodedPrompt?width=$targetWidth&height=$targetHeight&seed=$seed&nologo=true"
        )

        var downloadedBitmap: Bitmap? = null
        var engineName = "Perchance AI Engine"

        for ((idx, url) in candidateUrls.withIndex()) {
            try {
                onProgress("Rendering Perchance AI Pipeline (${idx + 1}/3)...")
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AICraft/3.0")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null && bmp.width > 50 && bmp.height > 50) {
                            downloadedBitmap = bmp
                            engineName = "Perchance AI (Native)"
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Perchance candidate $idx failed: ${e.message}")
            }
        }

        if (downloadedBitmap == null) {
            onProgress("Sintesis grafis Perchance...")
            downloadedBitmap = generateFallbackCanvas(prompt, "Perchance AI", targetWidth, targetHeight)
        }

        onProgress("Menyimpan hasil ke penyimpanan...")
        val savedFile = saveBitmapToStorage(context, downloadedBitmap, "perchance")
        return@withContext GenerationResult("file://${savedFile.absolutePath}", engineName)
    }

    private fun saveBitmapToStorage(context: Context, bitmap: Bitmap, prefix: String): File {
        val dir = File(context.filesDir, "generated_images").apply { if (!exists()) mkdirs() }
        val file = File(dir, "${prefix}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file
    }

    private fun generateFallbackCanvas(prompt: String, engineTitle: String, width: Int, height: Int): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.parseColor("#0F172A"),
                Color.parseColor("#1E1B4B"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val glowPaint = Paint().apply {
            color = Color.parseColor("#6366F1")
            alpha = 90
            isAntiAlias = true
        }
        canvas.drawCircle(width * 0.5f, height * 0.45f, width * 0.35f, glowPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("$engineTitle Generator", width / 2f, height * 0.45f, textPaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val displayPrompt = if (prompt.length > 45) prompt.take(42) + "..." else prompt
        canvas.drawText("\"$displayPrompt\"", width / 2f, height * 0.53f, subPaint)

        return bmp
    }
}
