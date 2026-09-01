package com.missionassist.app.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class DefaultSpeechClient(private val context: Context) : SpeechClient {
    private var speechRecognizer: SpeechRecognizer? = null

    override fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun checkCapability(languageCode: String, onResult: (SpeechCapability) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            }
            
            try {
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizer.checkRecognitionSupport(
                    intent,
                    context.mainExecutor,
                    object : android.speech.RecognitionSupportCallback {
                        override fun onSupportResult(support: android.speech.RecognitionSupport) {
                            val capability = when {
                                support.installedOnDeviceLanguages.contains(languageCode) -> SpeechCapability.ON_DEVICE_AVAILABLE
                                support.pendingOnDeviceLanguages.contains(languageCode) -> SpeechCapability.ON_DEVICE_DOWNLOADING
                                support.supportedOnDeviceLanguages.contains(languageCode) -> SpeechCapability.ON_DEVICE_NOT_DOWNLOADED
                                support.onlineLanguages.contains(languageCode) -> SpeechCapability.ONLINE_ONLY
                                else -> SpeechCapability.UNSUPPORTED
                            }
                            onResult(capability)
                            recognizer.destroy()
                        }
                        
                        override fun onError(error: Int) {
                            onResult(SpeechCapability.UNSUPPORTED)
                            recognizer.destroy()
                        }
                    }
                )
            } catch (e: Exception) {
                onResult(SpeechCapability.UNSUPPORTED)
            }
        } else {
            onResult(SpeechCapability.DETECTION_UNAVAILABLE)
        }
    }

    override fun triggerModelDownload(languageCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (Build.VERSION.SDK_INT >= 34) { // UPSIDE_DOWN_CAKE
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            }
            try {
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizer.triggerModelDownload(
                    intent,
                    context.mainExecutor,
                    object : android.speech.ModelDownloadListener {
                        override fun onProgress(completedBytes: Int) {}
                        override fun onSuccess() {
                            onSuccess()
                            recognizer.destroy()
                        }
                        override fun onScheduled() {}
                        override fun onError(error: Int) {
                            onError("Download failed with error code $error")
                            recognizer.destroy()
                        }
                    }
                )
            } catch (e: Exception) {
                onError("Failed to initiate download: ${e.message}")
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            }
            try {
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizer.triggerModelDownload(intent)
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to initiate download: ${e.message}")
            }
        } else {
            onError("Model download not supported on this Android version")
        }
    }

    override fun openSpeechSettings(): Boolean {
        val intent = Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun startListening(languageCode: String, isFallback: Boolean, listener: SpeechClientListener) {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()

        val isOnDeviceAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        val useOnDevice = shouldUseOnDevice(Build.VERSION.SDK_INT, isOnDeviceAvailable, isFallback)

        speechRecognizer = if (useOnDevice) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            if (shouldPreferOffline(Build.VERSION.SDK_INT, useOnDevice)) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listener.onReadyForSpeech()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                listener.onError(error)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                listener.onResults(matches?.firstOrNull())
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    override fun cancel() {
        speechRecognizer?.cancel()
    }

    override fun destroy() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    companion object {
        fun shouldUseOnDevice(sdkInt: Int, isOnDeviceAvailable: Boolean, isFallback: Boolean): Boolean {
            return sdkInt >= Build.VERSION_CODES.S && isOnDeviceAvailable && !isFallback
        }

        fun shouldPreferOffline(sdkInt: Int, useOnDevice: Boolean): Boolean {
            return useOnDevice && sdkInt >= Build.VERSION_CODES.M
        }
    }
}
