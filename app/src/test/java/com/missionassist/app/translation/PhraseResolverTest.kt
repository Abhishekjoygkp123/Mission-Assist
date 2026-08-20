package com.missionassist.app.translation

import com.google.mlkit.nl.translate.TranslateLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhraseResolverTest {

    @Test
    fun testExactCuratedEnglishPhraseMatchesTamil() {
        val result = PhraseResolver.resolve("Hello", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL)
        assertEquals("வணக்கம்", result)
    }

    @Test
    fun testExactCuratedTamilPhraseMatchesEnglish() {
        val result = PhraseResolver.resolve("நன்றி", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH)
        assertEquals("Thank you", result)
    }

    @Test
    fun testNormalizationHandling() {
        // Leading/trailing spaces, repeated spaces, punctuation, case differences
        val input = "   HOW   are YOU?!.  "
        val result = PhraseResolver.resolve(input, TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL)
        assertEquals("நீங்கள் எப்படி இருக்கிறீர்கள்", result)
    }

    @Test
    fun testNeedToSeeDoctorIntentMatches() {
        val testCases = listOf(
            // "எனக்கு டாக்டர் தேவை" and "எனக்கு டாக்டர் வேண்டும்" are exact variants for "I need a doctor"
            "எனக்கு டாக்டரை பார்க்கணும்",
            "எனக்கு டாக்டரை பாக்கணும்",
            "மருத்துவரை பார்க்க வேண்டும்"
        )
        
        for (testCase in testCases) {
            val result = PhraseResolver.resolve(testCase, TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH)
            assertEquals("I need to see a doctor.", result) // The exact output from our intent logic
        }
    }

    @Test
    fun testDoctorIntentDoesNotMatch() {
        val testCases = listOf(
            "டாக்டர்",
            "டாக்டர் நல்லவர்",
            "எனக்கு வேலை வேண்டும்"
        )

        for (testCase in testCases) {
            val result = PhraseResolver.resolve(testCase, TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH)
            assertNull("Expected null for input: $testCase", result)
        }
    }

    @Test
    fun testUnknownPhrasesReturnNull() {
        val result = PhraseResolver.resolve("This is completely unknown", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL)
        assertNull(result)
    }
}
