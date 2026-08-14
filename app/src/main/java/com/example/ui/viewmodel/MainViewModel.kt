package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GeneratedImageEntity
import com.example.data.repository.ImageGeneratorEngine
import com.example.data.repository.ImageRepository
import com.example.ui.models.ArtStyle
import com.example.ui.models.ArtStylePresets
import com.example.ui.models.AspectOption
import com.example.ui.models.AspectRatioPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed interface GenerationUiState {
    object Idle : GenerationUiState
    data class Generating(val step: String = "Memproses...") : GenerationUiState
    data class Success(val imageEntity: GeneratedImageEntity) : GenerationUiState
    data class Error(val message: String) : GenerationUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ImageRepository

    init {
        val dao = AppDatabase.getDatabase(application).generatedImageDao()
        repository = ImageRepository(dao)
    }

    val historyImages: StateFlow<List<GeneratedImageEntity>> = repository.allImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteImages: StateFlow<List<GeneratedImageEntity>> = repository.favoriteImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form States
    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _selectedStyle = MutableStateFlow(ArtStylePresets.defaultStyle)
    val selectedStyle: StateFlow<ArtStyle> = _selectedStyle.asStateFlow()

    private val _selectedAspect = MutableStateFlow(AspectRatioPresets.defaultOption)
    val selectedAspect: StateFlow<AspectOption> = _selectedAspect.asStateFlow()

    private val _negativePrompt = MutableStateFlow("blurry, low quality, bad anatomy, deformed, watermark, bad lighting")
    val negativePrompt: StateFlow<String> = _negativePrompt.asStateFlow()

    private val _guidanceScale = MutableStateFlow(7.5f)
    val guidanceScale: StateFlow<Float> = _guidanceScale.asStateFlow()

    private val _seed = MutableStateFlow("428912")
    val seed: StateFlow<String> = _seed.asStateFlow()

    private val _useRandomSeed = MutableStateFlow(true)
    val useRandomSeed: StateFlow<Boolean> = _useRandomSeed.asStateFlow()

    private val _statusMessage = MutableStateFlow("Siap membuat gambar dengan model FLUX.1 Ultra HD")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    fun setPromptText(text: String) {
        _promptText.value = text
    }

    fun setSelectedStyle(style: ArtStyle) {
        _selectedStyle.value = style
    }

    fun setSelectedAspect(aspect: AspectOption) {
        _selectedAspect.value = aspect
    }

    fun setNegativePrompt(neg: String) {
        _negativePrompt.value = neg
    }

    fun setGuidanceScale(scale: Float) {
        _guidanceScale.value = scale
    }

    fun setSeed(s: String) {
        _seed.value = s
    }

    fun setUseRandomSeed(use: Boolean) {
        _useRandomSeed.value = use
    }

    fun setStatusMessage(status: String) {
        _statusMessage.value = status
    }

    fun enhancePrompt() {
        val current = _promptText.value.ifBlank { "Seekor kucing mistis bercahaya" }
        val style = _selectedStyle.value
        val enhanced = "$current, ${style.promptSuffix}, cinematic lighting, 8k resolution, highly detailed digital painting"
        _promptText.value = enhanced
        _statusMessage.value = "Prompt berhasil diperkaya dengan kata kunci artistik!"
    }

    /**
     * Generates image directly using FLUX.1 Neural Engine without any slow iframe/webview.
     */
    fun generateImage() {
        val prompt = _promptText.value.trim()
        if (prompt.isEmpty()) {
            _statusMessage.value = "Ketikkan deskripsi prompt terlebih dahulu"
            return
        }

        val currentSeed = if (_useRandomSeed.value) {
            (100000..999999).random().toString()
        } else {
            _seed.value.ifEmpty { "428912" }
        }

        _generationState.value = GenerationUiState.Generating("Menghubungi FLUX.1 Neural Engine...")
        _statusMessage.value = "Memproses gambar via model FLUX.1 Pro HD..."

        viewModelScope.launch {
            try {
                val (savedImagePath, engineUsed) = ImageGeneratorEngine.generateAndSaveImage(
                    context = getApplication(),
                    prompt = prompt,
                    style = _selectedStyle.value.name,
                    styleSuffix = _selectedStyle.value.promptSuffix,
                    negativePrompt = _negativePrompt.value,
                    aspectRatio = _selectedAspect.value.ratio,
                    width = _selectedAspect.value.width,
                    height = _selectedAspect.value.height,
                    seed = currentSeed,
                    onProgress = { progressStep ->
                        _generationState.value = GenerationUiState.Generating(progressStep)
                        _statusMessage.value = progressStep
                    }
                )

                val newEntity = GeneratedImageEntity(
                    prompt = prompt,
                    enhancedPrompt = prompt + " " + _selectedStyle.value.promptSuffix,
                    style = _selectedStyle.value.name,
                    aspectRatio = _selectedAspect.value.ratio,
                    imageUrl = savedImagePath,
                    engine = engineUsed,
                    negativePrompt = _negativePrompt.value
                )

                val id = repository.saveImage(newEntity)
                val savedEntity = newEntity.copy(id = id)

                _generationState.value = GenerationUiState.Success(savedEntity)
                _statusMessage.value = "Gambar berhasil dibuat & disimpan ke galeri lokal!"
            } catch (e: Exception) {
                _generationState.value = GenerationUiState.Error("Gagal membuat gambar: ${e.localizedMessage}")
                _statusMessage.value = "Terjadi kesalahan saat memproses gambar"
            }
        }
    }

    fun toggleFavorite(image: GeneratedImageEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(image)
        }
    }

    fun deleteImage(image: GeneratedImageEntity) {
        viewModelScope.launch {
            repository.deleteImage(image)
        }
    }

    fun dismissResultDialog() {
        _generationState.value = GenerationUiState.Idle
    }

    fun saveImageToGallery(context: Context, imageEntity: GeneratedImageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap: Bitmap? = if (imageEntity.imageUrl.startsWith("file://")) {
                    val filePath = imageEntity.imageUrl.removePrefix("file://")
                    BitmapFactory.decodeFile(filePath)
                } else {
                    val url = java.net.URL(imageEntity.imageUrl)
                    BitmapFactory.decodeStream(url.openStream())
                }

                if (bitmap == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Gagal memuat file gambar", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val filename = "AI_Craft_${System.currentTimeMillis()}.png"
                var success = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AICraft")
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            success = true
                        }
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(imagesDir, "AICraft").apply { if (!exists()) mkdirs() }
                    val imageFile = File(appDir, filename)
                    FileOutputStream(imageFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        success = true
                    }
                }

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Gambar berhasil disimpan ke Galeri HP!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Gagal menyimpan gambar ke Galeri", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
