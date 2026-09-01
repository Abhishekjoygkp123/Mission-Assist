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

class SpeechRecognizerManager(private val speechClient: SpeechClient) {
    
    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val state: StateFlow<SpeechState> = _state.asStateFlow()
    
    private val _capabilityState = MutableStateFlow<SpeechCapability?>(null)
    val capabilityState: StateFlow<SpeechCapability?> = _capabilityState.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError.asStateFlow()

    private var currentSessionId: Long = 0L

    fun checkCapability(languageCode: String) {
        _downloadError.value = null
        speechClient.checkCapability(languageCode) { capability ->
            _capabilityState.value = capability
        }
    }

    fun triggerModelDownload(languageCode: String) {
        _downloadError.value = null
        speechClient.triggerModelDownload(
            languageCode = languageCode,
            onSuccess = {
                checkCapability(languageCode)
            },
            onError = { errorMessage ->
                _downloadError.value = errorMessage
            }
        )
    }

    fun openSpeechSettings(): Boolean {
        return speechClient.openSpeechSettings()
    }

    fun startListening(languageCode: String = "en-IN") {
        executeListening(languageCode, isFallback = false, hasRetriedDisconnect = false)
    }

    private fun executeListening(languageCode: String, isFallback: Boolean, hasRetriedDisconnect: Boolean) {
        val sessionId = ++currentSessionId
        
        if (!speechClient.isRecognitionAvailable()) {
            _state.value = SpeechState.Error("Recognizer unavailable")
            return
        }

        try {
            speechClient.startListening(languageCode, isFallback, object : SpeechClientListener {
                override fun onReadyForSpeech() {
                    if (sessionId != currentSessionId) return
                    _state.value = SpeechState.Listening
                }

                override fun onError(error: Int) {
                    if (sessionId != currentSessionId) return
                    
                    val ERROR_SERVER_DISCONNECTED = 11
                    val ERROR_LANGUAGE_NOT_SUPPORTED = 12
                    val ERROR_LANGUAGE_UNAVAILABLE = 13
                    
                    if (!hasRetriedDisconnect && error == ERROR_SERVER_DISCONNECTED) {
                        executeListening(languageCode, isFallback = isFallback, hasRetriedDisconnect = true)
                        return
                    }
                    
                    if (!isFallback && (error == ERROR_LANGUAGE_NOT_SUPPORTED || error == ERROR_LANGUAGE_UNAVAILABLE)) {
                        executeListening(languageCode, isFallback = true, hasRetriedDisconnect = false)
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
                        ERROR_SERVER_DISCONNECTED -> "Server disconnected. Please try again."
                        ERROR_LANGUAGE_UNAVAILABLE, ERROR_LANGUAGE_NOT_SUPPORTED -> "Requested language speech recognition is unavailable on this device."
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Error code: $error"
                    }
                    _state.value = SpeechState.Error(errorMessage)
                }

                override fun onResults(text: String?) {
                    if (sessionId != currentSessionId) return
                    
                    if (!text.isNullOrEmpty()) {
                        _state.value = SpeechState.Result(text)
                    } else {
                        _state.value = SpeechState.Error("No match")
                    }
                }
            })
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
        speechClient.cancel()
        _state.value = SpeechState.Idle
    }

    fun setPermissionDenied() {
        currentSessionId++
        _state.value = SpeechState.Error("Permission denied")
    }

    fun destroy() {
        currentSessionId++
        speechClient.destroy()
    }
}
