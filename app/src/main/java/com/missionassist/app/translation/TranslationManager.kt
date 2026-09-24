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

class TranslationManager(private val translationClient: TranslationClient) {

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
        
        translationClient.downloadModelIfNeeded(
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            onSuccess = {
                if (sessionId != currentSessionId) return@downloadModelIfNeeded
                _state.value = TranslationState.Translating
                
                translationClient.translate(
                    text = text,
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    onSuccess = { translatedText ->
                        if (sessionId != currentSessionId) return@translate
                        _state.value = TranslationState.Success(translatedText, TranslationSource.ML_KIT)
                    },
                    onFailure = { errorMessage ->
                        if (sessionId != currentSessionId) return@translate
                        _state.value = TranslationState.Error(errorMessage)
                    }
                )
            },
            onFailure = { errorMessage ->
                if (sessionId != currentSessionId) return@downloadModelIfNeeded
                _state.value = TranslationState.Error("Model download failed: $errorMessage")
            }
        )
    }
    
    fun clear() {
        currentSessionId++ // Invalidate any pending callbacks immediately
        _state.value = TranslationState.Idle
    }

    fun setTranslation(text: String, source: TranslationSource = TranslationSource.VERIFIED_PHRASE) {
        currentSessionId++
        _state.value = TranslationState.Success(text, source)
    }

    fun close() {
        currentSessionId++
        translationClient.close()
    }
}
