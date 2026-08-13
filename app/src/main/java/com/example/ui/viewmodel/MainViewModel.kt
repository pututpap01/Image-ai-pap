package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.GeneratedImageEntity
import com.example.data.repository.ImageRepository
import com.example.ui.components.triggerPerchanceGenerationInWebView
import com.example.ui.models.ArtStyle
import com.example.ui.models.ArtStylePresets
import com.example.ui.models.AspectOption
import com.example.ui.models.AspectRatioPresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URLEncoder

sealed interface GenerationUiState {
    object Idle : GenerationUiState
    object Generating : GenerationUiState
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

    private val _selectedEngine = MutableStateFlow("perchance") // "perchance" or "direct"
    val selectedEngine: StateFlow<String> = _selectedEngine.asStateFlow()

    private val _isIframeExpanded = MutableStateFlow(false)
    val isIframeExpanded: StateFlow<Boolean> = _isIframeExpanded.asStateFlow()

    private val _statusMessage = MutableStateFlow("Siap membuat gambar AI")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    var activeWebView: WebView? = null

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

    fun setSelectedEngine(engine: String) {
        _selectedEngine.value = engine
    }

    fun toggleIframeExpanded() {
        _isIframeExpanded.value = !_isIframeExpanded.value
    }

    fun setStatusMessage(status: String) {
        _statusMessage.value = status
    }

    fun enhancePrompt() {
        val current = _promptText.value.ifBlank { "A beautiful mystical artwork" }
        val style = _selectedStyle.value
        val enhanced = "$current${style.promptSuffix}, cinematic lighting, highly detailed, octane render, 8k wallpaper"
        _promptText.value = enhanced
        _statusMessage.value = "Prompt berhasil ditingkatkan dengan kata kunci artistik!"
    }

    fun generateImage() {
        val prompt = _promptText.value.trim()
        if (prompt.isEmpty()) {
            _statusMessage.value = "Ketikkan deskripsi/prompt terlebih dahulu"
            return
        }

        _generationState.value = GenerationUiState.Generating
        _statusMessage.value = "Proses membuat gambar AI..."

        val currentSeed = if (_useRandomSeed.value) {
            (100000..999999).random().toString()
        } else {
            _seed.value.ifEmpty { "12345" }
        }

        if (_selectedEngine.value == "perchance") {
            // Send JS postMessage/evaluation to Perchance WebView
            triggerPerchanceGenerationInWebView(
                webView = activeWebView,
                prompt = prompt,
                styleSuffix = _selectedStyle.value.promptSuffix,
                negativePrompt = _negativePrompt.value,
                aspectRatio = _selectedAspect.value.ratio
            )

            // Fallback & direct sync preview generation after triggering
            val directGeneratedUrl = buildDirectImageUrl(
                prompt = prompt + " " + _selectedStyle.value.promptSuffix,
                width = _selectedAspect.value.width,
                height = _selectedAspect.value.height,
                seed = currentSeed
            )

            val newEntity = GeneratedImageEntity(
                prompt = prompt,
                style = _selectedStyle.value.name,
                aspectRatio = _selectedAspect.value.ratio,
                imageUrl = directGeneratedUrl,
                engine = "Perchance AI",
                negativePrompt = _negativePrompt.value
            )

            viewModelScope.launch {
                val id = repository.saveImage(newEntity)
                val savedEntity = newEntity.copy(id = id)
                _generationState.value = GenerationUiState.Success(savedEntity)
                _statusMessage.value = "Gambar berhasil diproses!"
            }
        } else {
            // Direct Engine Generation
            val imageUrl = buildDirectImageUrl(
                prompt = prompt + " " + _selectedStyle.value.promptSuffix,
                width = _selectedAspect.value.width,
                height = _selectedAspect.value.height,
                seed = currentSeed
            )

            val newEntity = GeneratedImageEntity(
                prompt = prompt,
                style = _selectedStyle.value.name,
                aspectRatio = _selectedAspect.value.ratio,
                imageUrl = imageUrl,
                engine = "Direct AI Engine",
                negativePrompt = _negativePrompt.value
            )

            viewModelScope.launch {
                val id = repository.saveImage(newEntity)
                val savedEntity = newEntity.copy(id = id)
                _generationState.value = GenerationUiState.Success(savedEntity)
                _statusMessage.value = "Gambar AI berhasil dibuat!"
            }
        }
    }

    fun handleWebViewExtractedImage(url: String) {
        if (url.startsWith("http")) {
            val entity = GeneratedImageEntity(
                prompt = _promptText.value.ifEmpty { "Hasil Perchance AI" },
                style = _selectedStyle.value.name,
                aspectRatio = _selectedAspect.value.ratio,
                imageUrl = url,
                engine = "Perchance AI (Iframe Extracted)"
            )

            viewModelScope.launch {
                val id = repository.saveImage(entity)
                val saved = entity.copy(id = id)
                _generationState.value = GenerationUiState.Success(saved)
                _statusMessage.value = "Mendapatkan gambar dari Perchance Iframe!"
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

    private fun buildDirectImageUrl(prompt: String, width: Int, height: Int, seed: String): String {
        val encodedPrompt = try {
            URLEncoder.encode(prompt, "UTF-8")
        } catch (e: Exception) {
            prompt.replace(" ", "%20")
        }
        return "https://image.pollinations.ai/prompt/$encodedPrompt?width=$width&height=$height&seed=$seed&nologo=true"
    }
}
