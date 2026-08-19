package com.missionassist.app.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class TranslationState {
    object Idle : TranslationState()
    object DownloadingModel : TranslationState()
    object Translating : TranslationState()
    data class Success(val text: String) : TranslationState()
    data class Error(val message: String) : TranslationState()
}

class TranslationManager {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.TAMIL)
        .build()

    private val translator: Translator = Translation.getClient(options)

    private val _state = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val state: StateFlow<TranslationState> = _state.asStateFlow()

    fun translate(text: String) {
        _state.value = TranslationState.DownloadingModel
        
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                _state.value = TranslationState.Translating
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        _state.value = TranslationState.Success(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        _state.value = TranslationState.Error(exception.localizedMessage ?: "Translation failed")
                    }
            }
            .addOnFailureListener { exception ->
                _state.value = TranslationState.Error("Model download failed: ${exception.localizedMessage}")
            }
    }

    fun close() {
        translator.close()
    }
}
