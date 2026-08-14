package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdvancedSettingsSheet
import com.example.ui.components.AspectRatioSelector
import com.example.ui.components.ImageResultDialog
import com.example.ui.components.PerchanceWebView
import com.example.ui.components.PromptPresetChips
import com.example.ui.components.StyleSelector
import com.example.ui.theme.AccentGradientEnd
import com.example.ui.theme.AccentGradientStart
import com.example.ui.viewmodel.GenerationUiState
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val promptText by viewModel.promptText.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedAspect by viewModel.selectedAspect.collectAsState()
    val negativePrompt by viewModel.negativePrompt.collectAsState()
    val guidanceScale by viewModel.guidanceScale.collectAsState()
    val seed by viewModel.seed.collectAsState()
    val useRandomSeed by viewModel.useRandomSeed.collectAsState()
    val selectedEngine by viewModel.selectedEngine.collectAsState()
    val isIframeExpanded by viewModel.isIframeExpanded.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val generationState by viewModel.generationState.collectAsState()

    var showAdvancedSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AccentGradientStart, AccentGradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "AI Image Craft",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Perchance Iframe & Direct Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = { showAdvancedSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Pengaturan Lanjutan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Engine Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedEngine == "perchance",
                            onClick = { viewModel.setSelectedEngine("perchance") },
                            label = { Text("Iframe Perchance AI") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("engine_chip_perchance")
                        )

                        FilterChip(
                            selected = selectedEngine == "direct",
                            onClick = { viewModel.setSelectedEngine("direct") },
                            label = { Text("Direct AI Engine") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.testTag("engine_chip_direct")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Prompt Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Masukan Deskripsi/Prompt Gambar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { viewModel.setPromptText(it) },
                    placeholder = {
                        Text("Contoh: Seekor kucing cerdas mengenakan jas koki di dapur restoran megah...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_input_field"),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        if (promptText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setPromptText("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus Prompt")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.enhancePrompt() },
                        modifier = Modifier.testTag("enhance_prompt_btn"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Prompt Enhancer", style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${promptText.length} Karakter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prompt Presets Inspiration Chips
            PromptPresetChips(
                onPromptSelected = { selected ->
                    viewModel.setPromptText(selected)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Style Selector Section
            StyleSelector(
                selectedStyle = selectedStyle,
                onStyleSelected = { viewModel.setSelectedStyle(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Aspect Ratio Selector
            AspectRatioSelector(
                selectedAspect = selectedAspect,
                onAspectSelected = { viewModel.setSelectedAspect(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Embedded Iframe Section (Perchance WebView)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sematkan Frame Perchance AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { viewModel.toggleIframeExpanded() }) {
                        Icon(
                            imageVector = if (isIframeExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Perbesar Viewport",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                PerchanceWebView(
                    onWebViewCreated = { webView ->
                        viewModel.activeWebView = webView
                    },
                    onImageGenerated = { imageUrl ->
                        viewModel.handleWebViewExtractedImage(imageUrl)
                    },
                    onStatusChange = { msg ->
                        viewModel.setStatusMessage(msg)
                    },
                    isExpanded = isIframeExpanded,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Message Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Floating Bottom Action Button ("Buat Gambar AI")
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background)
                    )
                )
                .padding(16.dp)
        ) {
            Button(
                onClick = { viewModel.generateImage() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_image_button"),
                enabled = generationState !is GenerationUiState.Generating,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (generationState is GenerationUiState.Generating) {
                    val stepText = (generationState as GenerationUiState.Generating).step
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stepText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Buat Gambar AI Sekarang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom Sheets & Dialogs
        if (showAdvancedSheet) {
            AdvancedSettingsSheet(
                negativePrompt = negativePrompt,
                onNegativePromptChange = { viewModel.setNegativePrompt(it) },
                guidanceScale = guidanceScale,
                onGuidanceScaleChange = { viewModel.setGuidanceScale(it) },
                seed = seed,
                onSeedChange = { viewModel.setSeed(it) },
                useRandomSeed = useRandomSeed,
                onUseRandomSeedChange = { viewModel.setUseRandomSeed(it) },
                onDismissRequest = { showAdvancedSheet = false }
            )
        }

        if (generationState is GenerationUiState.Success) {
            val successState = generationState as GenerationUiState.Success
            val currentContext = androidx.compose.ui.platform.LocalContext.current
            ImageResultDialog(
                imageItem = successState.imageEntity,
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onSaveToGallery = { viewModel.saveImageToGallery(currentContext, it) },
                onDismissRequest = { viewModel.dismissResultDialog() }
            )
        }
    }
}
