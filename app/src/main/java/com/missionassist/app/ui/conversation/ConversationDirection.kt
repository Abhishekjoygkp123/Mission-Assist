package com.missionassist.app.ui.conversation

import com.google.mlkit.nl.translate.TranslateLanguage

enum class ConversationDirection(
    val labelLeft: String,
    val labelRight: String,
    val speechLang: String,
    val sourceMlKitLang: String,
    val targetMlKitLang: String,
    val ttsLang: String
) {
    ENGLISH_TO_TAMIL(
        labelLeft = "English",
        labelRight = "Tamil",
        speechLang = "en-IN",
        sourceMlKitLang = TranslateLanguage.ENGLISH,
        targetMlKitLang = TranslateLanguage.TAMIL,
        ttsLang = "ta-IN"
    ),
    TAMIL_TO_ENGLISH(
        labelLeft = "Tamil",
        labelRight = "English",
        speechLang = "ta-IN",
        sourceMlKitLang = TranslateLanguage.TAMIL,
        targetMlKitLang = TranslateLanguage.ENGLISH,
        ttsLang = "en-IN"
    );

    fun swap(): ConversationDirection {
        return if (this == ENGLISH_TO_TAMIL) TAMIL_TO_ENGLISH else ENGLISH_TO_TAMIL
    }
}
