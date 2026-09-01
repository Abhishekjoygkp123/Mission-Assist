package com.missionassist.app.speech

enum class SpeechCapability {
    ON_DEVICE_AVAILABLE,
    ON_DEVICE_NOT_DOWNLOADED,
    ON_DEVICE_DOWNLOADING,
    ONLINE_ONLY,
    UNSUPPORTED,
    DETECTION_UNAVAILABLE
}

interface SpeechClient {
    fun isRecognitionAvailable(): Boolean
    fun checkCapability(languageCode: String, onResult: (SpeechCapability) -> Unit)
    fun triggerModelDownload(languageCode: String, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun openSpeechSettings(): Boolean
    fun startListening(languageCode: String, isFallback: Boolean, listener: SpeechClientListener)
    fun cancel()
    fun destroy()
}

interface SpeechClientListener {
    fun onReadyForSpeech()
    fun onError(error: Int)
    fun onResults(text: String?)
}
