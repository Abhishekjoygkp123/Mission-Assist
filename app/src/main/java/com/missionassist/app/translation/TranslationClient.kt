package com.missionassist.app.translation

interface TranslationClient {
    fun downloadModelIfNeeded(
        sourceLanguage: String, 
        targetLanguage: String, 
        onSuccess: () -> Unit, 
        onFailure: (String) -> Unit
    )
    
    fun translate(
        text: String, 
        sourceLanguage: String, 
        targetLanguage: String, 
        onSuccess: (String) -> Unit, 
        onFailure: (String) -> Unit
    )
    
    fun close()
}
