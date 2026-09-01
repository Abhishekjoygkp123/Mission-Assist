package com.missionassist.app.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class DefaultTranslationClient : TranslationClient {
    private var currentTranslator: Translator? = null
    private var currentSourceLanguage: String? = null
    private var currentTargetLanguage: String? = null

    override fun downloadModelIfNeeded(
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
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
                onSuccess()
            }
            ?.addOnFailureListener { exception ->
                onFailure(exception.localizedMessage ?: "Model download failed")
            }
    }

    override fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Assume downloadModelIfNeeded was already called and set up the translator
        currentTranslator?.translate(text)
            ?.addOnSuccessListener { translatedText ->
                onSuccess(translatedText)
            }
            ?.addOnFailureListener { exception ->
                onFailure(exception.localizedMessage ?: "Translation failed")
            } ?: onFailure("Translator not initialized")
    }

    override fun close() {
        currentTranslator?.close()
        currentTranslator = null
    }
}
