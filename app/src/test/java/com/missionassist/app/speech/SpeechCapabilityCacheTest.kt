package com.missionassist.app.speech

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SpeechCapabilityCacheTest {

    private val testLangs = listOf("en-test", "ta-test", "fr-test", "lang1-test", "lang2-test")

    @Before
    @After
    fun cleanup() {
        testLangs.forEach { SpeechCapabilityCache.remove(it) }
    }

    @Test
    fun testGoodResultIsStoredAndReturned() {
        val lang = "en-test"
        SpeechCapabilityCache.update(lang, SpeechCapability.ON_DEVICE_AVAILABLE)
        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, SpeechCapabilityCache.get(lang))
    }

    @Test
    fun testBadResultDoesNotOverwriteGoodResult() {
        val lang = "en-test"
        SpeechCapabilityCache.update(lang, SpeechCapability.ON_DEVICE_AVAILABLE)

        // Attempt overwrite with UNSUPPORTED
        SpeechCapabilityCache.update(lang, SpeechCapability.UNSUPPORTED)
        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, SpeechCapabilityCache.get(lang))

        // Attempt overwrite with DETECTION_UNAVAILABLE
        SpeechCapabilityCache.update(lang, SpeechCapability.DETECTION_UNAVAILABLE)
        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, SpeechCapabilityCache.get(lang))
    }

    @Test
    fun testBadResultStoredWhenNoGoodResultAndGoodOverwritesIt() {
        val lang = "ta-test"
        // Initial bad result stored
        SpeechCapabilityCache.update(lang, SpeechCapability.UNSUPPORTED)
        assertEquals(SpeechCapability.UNSUPPORTED, SpeechCapabilityCache.get(lang))

        // Later good result overwrites it
        SpeechCapabilityCache.update(lang, SpeechCapability.ON_DEVICE_AVAILABLE)
        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, SpeechCapabilityCache.get(lang))
    }

    @Test
    fun testRemoveClearsEntryAfterWhichBadResultCanBeStored() {
        val lang = "fr-test"
        SpeechCapabilityCache.update(lang, SpeechCapability.ON_DEVICE_AVAILABLE)
        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, SpeechCapabilityCache.get(lang))

        // Remove clears entry
        SpeechCapabilityCache.remove(lang)
        assertNull(SpeechCapabilityCache.get(lang))

        // Bad result can now be stored because no good result exists
        SpeechCapabilityCache.update(lang, SpeechCapability.UNSUPPORTED)
        assertEquals(SpeechCapability.UNSUPPORTED, SpeechCapabilityCache.get(lang))
    }

    @Test
    fun testEntriesAreIndependentPerLanguageCode() {
        val lang1 = "lang1-test"
        val lang2 = "lang2-test"

        SpeechCapabilityCache.update(lang1, SpeechCapability.ON_DEVICE_AVAILABLE)
        SpeechCapabilityCache.update(lang2, SpeechCapability.ONLINE_ONLY)

        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, SpeechCapabilityCache.get(lang1))
        assertEquals(SpeechCapability.ONLINE_ONLY, SpeechCapabilityCache.get(lang2))

        SpeechCapabilityCache.remove(lang1)
        assertNull(SpeechCapabilityCache.get(lang1))
        assertEquals(SpeechCapability.ONLINE_ONLY, SpeechCapabilityCache.get(lang2))
    }
}
