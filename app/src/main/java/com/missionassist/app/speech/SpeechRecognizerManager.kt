package com.missionassist.app.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SpeechState {
    object Idle : SpeechState()
    object Listening : SpeechState()
    data class Result(val text: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}

class SpeechRecognizerManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    
    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var currentSessionId: Long = 0L

    fun startListening(languageCode: String = "en-IN") {
        executeListening(languageCode, isFallback = false)
    }

    private fun executeListening(languageCode: String, isFallback: Boolean) {
        val sessionId = ++currentSessionId
        
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechState.Error("Recognizer unavailable")
            return
        }

        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        
        val isOnDeviceAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        
        speechRecognizer = if (isOnDeviceAvailable && !isFallback) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                if (sessionId != currentSessionId) return
                _state.value = SpeechState.Listening
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if (sessionId != currentSessionId) return
                
                val ERROR_LANGUAGE_NOT_SUPPORTED = 12
                val ERROR_LANGUAGE_UNAVAILABLE = 13
                
                if (!isFallback && (error == ERROR_LANGUAGE_NOT_SUPPORTED || error == ERROR_LANGUAGE_UNAVAILABLE)) {
                    executeListening(languageCode, isFallback = true)
                    return
                }
                
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    ERROR_LANGUAGE_UNAVAILABLE, ERROR_LANGUAGE_NOT_SUPPORTED -> "Requested language speech recognition is unavailable on this device."
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Error code: $error"
                }
                _state.value = SpeechState.Error(errorMessage)
            }
            override fun onResults(results: Bundle?) {
                if (sessionId != currentSessionId) return
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _state.value = SpeechState.Result(matches[0])
                } else {
                    _state.value = SpeechState.Error("No match")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            speechRecognizer?.startListening(intent)
            if (sessionId == currentSessionId) {
                _state.value = SpeechState.Listening
            }
        } catch (e: Exception) {
            if (sessionId == currentSessionId) {
                _state.value = SpeechState.Error("Failed to start recognizer")
            }
        }
    }
    
    fun clear() {
        currentSessionId++ // Invalidate any pending callbacks immediately
        speechRecognizer?.cancel()
        _state.value = SpeechState.Idle
    }

    fun setPermissionDenied() {
        currentSessionId++
        _state.value = SpeechState.Error("Permission denied")
    }

    fun destroy() {
        currentSessionId++
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
