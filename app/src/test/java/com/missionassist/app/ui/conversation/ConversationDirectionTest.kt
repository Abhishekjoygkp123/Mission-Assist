package com.missionassist.app.ui.conversation

import com.google.mlkit.nl.translate.TranslateLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationDirectionTest {
    @Test
    fun testEnglishToTamilDirectionValues() {
        val direction = ConversationDirection.ENGLISH_TO_TAMIL
        assertEquals("English", direction.labelLeft)
        assertEquals("Tamil", direction.labelRight)
        assertEquals("en-IN", direction.speechLang)
        assertEquals(TranslateLanguage.ENGLISH, direction.sourceMlKitLang)
        assertEquals(TranslateLanguage.TAMIL, direction.targetMlKitLang)
        assertEquals("ta-IN", direction.ttsLang)
    }

    @Test
    fun testTamilToEnglishDirectionValues() {
        val direction = ConversationDirection.TAMIL_TO_ENGLISH
        assertEquals("Tamil", direction.labelLeft)
        assertEquals("English", direction.labelRight)
        assertEquals("ta-IN", direction.speechLang)
        assertEquals(TranslateLanguage.TAMIL, direction.sourceMlKitLang)
        assertEquals(TranslateLanguage.ENGLISH, direction.targetMlKitLang)
        assertEquals("en-IN", direction.ttsLang)
    }

    @Test
    fun testRepeatedSwapReturnsToOriginalDirection() {
        var direction = ConversationDirection.ENGLISH_TO_TAMIL
        direction = direction.swap()
        assertEquals(ConversationDirection.TAMIL_TO_ENGLISH, direction)
        direction = direction.swap()
        assertEquals(ConversationDirection.ENGLISH_TO_TAMIL, direction)
    }
}
