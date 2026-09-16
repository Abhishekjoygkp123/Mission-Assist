package com.missionassist.app.speech

object SpeechCapabilityCache {
    private val cache = mutableMapOf<String, SpeechCapability>()

    fun get(languageCode: String): SpeechCapability? {
        return cache[languageCode]
    }

    fun isGood(capability: SpeechCapability): Boolean {
        return capability != SpeechCapability.UNSUPPORTED && 
               capability != SpeechCapability.DETECTION_UNAVAILABLE
    }

    fun update(languageCode: String, capability: SpeechCapability) {
        val existing = cache[languageCode]
        if (existing != null && isGood(existing) && !isGood(capability)) {
            // Never let a transient bad response erase a previously confirmed good one.
            return
        }
        cache[languageCode] = capability
    }

    fun remove(languageCode: String) {
        cache.remove(languageCode)
    }
}
