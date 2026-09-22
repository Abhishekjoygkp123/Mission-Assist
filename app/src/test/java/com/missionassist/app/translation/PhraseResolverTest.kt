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

    @Test
    fun testNewEmergencyPhrasesEnglishToTamil() {
        assertEquals("எனக்கு ஆம்புலன்ஸ் தேவை", PhraseResolver.resolve("I need an ambulance", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL))
        assertEquals("எனக்கு வலி இருக்குது", PhraseResolver.resolve("I am in pain", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL))
        assertEquals("எனக்கு மருந்து வேண்டும்", PhraseResolver.resolve("I need medicine", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL))
        assertEquals("போலீஸை கூப்பிடுங்க", PhraseResolver.resolve("Call the police", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL))
        assertEquals("அங்கே தீ பிடித்துவிட்டது", PhraseResolver.resolve("There is a fire", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL))
        assertEquals("எனக்கு குடிக்கத்தண்ணீர் வேண்டும்", PhraseResolver.resolve("I need drinking water", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL))
    }

    @Test
    fun testNewEmergencyPhrasesTamilToEnglish() {
        assertEquals("I need an ambulance", PhraseResolver.resolve("எனக்கு ஆம்புலன்ஸ் தேவை", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH))
        assertEquals("I am in pain", PhraseResolver.resolve("எனக்கு வலி இருக்குது", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH))
        assertEquals("I need medicine", PhraseResolver.resolve("எனக்கு மருந்து வேண்டும்", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH))
        assertEquals("Call the police", PhraseResolver.resolve("போலீஸை கூப்பிடுங்க", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH))
        assertEquals("There is a fire", PhraseResolver.resolve("அங்கே தீ பிடித்துவிட்டது", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH))
        assertEquals("I need drinking water", PhraseResolver.resolve("எனக்கு குடிக்கத்தண்ணீர் வேண்டும்", TranslateLanguage.TAMIL, TranslateLanguage.ENGLISH))
    }
}
