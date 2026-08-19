package com.missionassist.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class TtsState {
    object Idle : TtsState()
    object Initializing : TtsState()
    object Ready : TtsState()
    data class Error(val message: String) : TtsState()
}

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private val _state = MutableStateFlow<TtsState>(TtsState.Initializing)
    val state: StateFlow<TtsState> = _state.asStateFlow()
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                _state.value = TtsState.Ready
            } else {
                _state.value = TtsState.Error("Text-to-Speech initialization failed.")
            }
        }
    }

    fun speak(text: String, languageCode: String = "ta-IN") {
        if (!isInitialized) return
        
        val locale = if (languageCode == "ta-IN") Locale("ta", "IN") else Locale("en", "IN")
        val result = tts?.setLanguage(locale)
        
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _state.value = TtsState.Error("Requested language is not supported or missing data on this device.")
            return
        }
        
        _state.value = TtsState.Ready
        tts?.stop() // Stop any ongoing speech before starting new speech
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
