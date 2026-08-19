package com.missionassist.app.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TranslationSource { VERIFIED_PHRASE, ML_KIT }

sealed class TranslationState {
    object Idle : TranslationState()
    object DownloadingModel : TranslationState()
    object Translating : TranslationState()
    data class Success(val text: String, val source: TranslationSource) : TranslationState()
    data class Error(val message: String) : TranslationState()
}

class TranslationManager {
    private var currentTranslator: Translator? = null
    private var currentSourceLanguage: String? = null
    private var currentTargetLanguage: String? = null

    private val _state = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val state: StateFlow<TranslationState> = _state.asStateFlow()

    private var currentSessionId: Long = 0L

    fun translate(text: String, sourceLanguage: String = TranslateLanguage.ENGLISH, targetLanguage: String = TranslateLanguage.TAMIL) {
        val sessionId = ++currentSessionId
        
        val resolvedPhrase = PhraseResolver.resolve(text, sourceLanguage, targetLanguage)
        if (resolvedPhrase != null) {
            _state.value = TranslationState.Success(resolvedPhrase, TranslationSource.VERIFIED_PHRASE)
            return
        }

        _state.value = TranslationState.DownloadingModel
        
        if (currentTranslator == null || currentSourceLanguage != sourceLanguage || currentTargetLanguage != targetLanguage) {
            currentTranslator?.close()
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
            currentTranslator = Translation.getClient(options)
            currentSourceLanguage = sourceLanguage
            currentTargetLanguage = targetLanguage
        }

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        currentTranslator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                if (sessionId != currentSessionId) return@addOnSuccessListener
                _state.value = TranslationState.Translating
                currentTranslator?.translate(text)
                    ?.addOnSuccessListener { translatedText ->
                        if (sessionId != currentSessionId) return@addOnSuccessListener
                        _state.value = TranslationState.Success(translatedText, TranslationSource.ML_KIT)
                    }
                    ?.addOnFailureListener { exception ->
                        if (sessionId != currentSessionId) return@addOnFailureListener
                        _state.value = TranslationState.Error(exception.localizedMessage ?: "Translation failed")
                    }
            }
            ?.addOnFailureListener { exception ->
                if (sessionId != currentSessionId) return@addOnFailureListener
                _state.value = TranslationState.Error("Model download failed: ${exception.localizedMessage}")
            }
    }
    
    fun clear() {
        currentSessionId++ // Invalidate any pending callbacks immediately
        _state.value = TranslationState.Idle
    }

    fun close() {
        currentSessionId++
        currentTranslator?.close()
        currentTranslator = null
    }
}
