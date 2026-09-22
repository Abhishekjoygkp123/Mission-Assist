package com.missionassist.app.translation

import com.google.mlkit.nl.translate.TranslateLanguage

data class PhraseEntry(
    val canonicalEnglish: String,
    val canonicalTamil: String,
    val englishVariants: List<String> = emptyList(),
    val tamilVariants: List<String> = emptyList()
)

object PhraseResolver {
    private val phraseEntries = listOf(
        PhraseEntry("Hello", "வணக்கம்"),
        PhraseEntry("How are you?", "நீங்கள் எப்படி இருக்கிறீர்கள்"),
        PhraseEntry("Thank you", "நன்றி"),
        PhraseEntry("I need an ambulance", "எனக்கு ஆம்புலன்ஸ் தேவை"),
        PhraseEntry("I am in pain", "எனக்கு வலி இருக்குது"),
        PhraseEntry("I need medicine", "எனக்கு மருந்து வேண்டும்"),
        PhraseEntry("Call the police", "போலீஸை கூப்பிடுங்க"),
        PhraseEntry("There is a fire", "அங்கே தீ பிடித்துவிட்டது"),
        PhraseEntry("I need drinking water", "எனக்கு குடிக்கத்தண்ணீர் வேண்டும்"),
        PhraseEntry("I need water", "எனக்கு தண்ணீர் வேண்டும்"),
        PhraseEntry("I need food", "எனக்கு உணவு வேண்டும்"),
        PhraseEntry(
            canonicalEnglish = "I need a doctor",
            canonicalTamil = "எனக்கு ஒரு மருத்துவர் வேண்டும்",
            tamilVariants = listOf(
                "எனக்கு டாக்டர் தேவை",
                "எனக்கு ஒரு டாக்டர் தேவை",
                "எனக்கு மருத்துவர் தேவை",
                "டாக்டர் வேண்டும்",
                "எனக்கு டாக்டர் வேண்டும்"
            )
        ),
        PhraseEntry("I need to see a doctor", "எனக்கு மருத்துவரை பார்க்க வேண்டும்"),
        PhraseEntry("Please call an ambulance", "தயவுசெய்து ஆம்புலன்ஸை அழைக்கவும்"),
        PhraseEntry("Where is the nearest hospital?", "அருகில் உள்ள மருத்துவமனை எங்கே"),
        PhraseEntry("Where is the bathroom?", "கழிப்பறை எங்கே"),
        PhraseEntry("Please help me", "தயவுசெய்து எனக்கு உதவுங்கள்"),
        PhraseEntry("Are you in pain?", "உங்களுக்கு வலிக்கிறதா"),
        PhraseEntry("What happened?", "என்ன நடந்தது"),
        PhraseEntry("Do you understand?", "உங்களுக்கு புரிகிறதா"),
        PhraseEntry("Please speak slowly", "தயவுசெய்து மெதுவாக பேசுங்கள்"),
        PhraseEntry("I do not understand", "எனக்கு புரியவில்லை"),
        PhraseEntry(
            canonicalEnglish = "What are you doing?",
            canonicalTamil = "என்ன செய்து கொண்டிருக்கிறீர்கள்",
            tamilVariants = listOf(
                "என்ன பண்ணிட்டு இருக்க",
                "என்னடா பண்ணிட்டு இருக்க",
                "என்ன பண்ணுறீங்க"
            )
        ),
        PhraseEntry(
            canonicalEnglish = "Don't you have anything else to do?",
            canonicalTamil = "வேற வேலை எதுவும் இல்லையா",
            tamilVariants = listOf(
                "வேற வேலை கிடையாதா உனக்கு",
                "வேற வேலை இல்லையா உனக்கு"
            )
        ),
        PhraseEntry(
            canonicalEnglish = "What are you doing? Don't you have anything else to do?",
            canonicalTamil = "என்னடா பண்ணிட்டு இருக்க வேற வேலை கிடையாதா உனக்கு"
        )
    )

    private val englishToTamilMap = mutableMapOf<String, String>()
    private val tamilToEnglishMap = mutableMapOf<String, String>()

    init {
        for (entry in phraseEntries) {
            val canonicalTamilNormalized = normalize(entry.canonicalTamil)
            val canonicalEnglishNormalized = normalize(entry.canonicalEnglish)

            // Map English -> Tamil
            englishToTamilMap[canonicalEnglishNormalized] = entry.canonicalTamil
            for (variant in entry.englishVariants) {
                englishToTamilMap[normalize(variant)] = entry.canonicalTamil
            }

            // Map Tamil -> English
            tamilToEnglishMap[canonicalTamilNormalized] = entry.canonicalEnglish
            for (variant in entry.tamilVariants) {
                tamilToEnglishMap[normalize(variant)] = entry.canonicalEnglish
            }
        }
    }

    private val doctorTerms = listOf("டாக்டர்", "டாக்டரை", "மருத்துவர்", "மருத்துவரை")
    private val needSeeTerms = listOf("தேவை", "வேண்டும்", "பார்க்கணும்", "பாக்கணும்", "பார்க்க வேண்டும்")

    fun resolve(text: String, sourceLanguage: String, targetLanguage: String): String? {
        val normalizedText = normalize(text)

        // 1. Exact curated phrase/variant
        val exactMatch = if (sourceLanguage == TranslateLanguage.ENGLISH && targetLanguage == TranslateLanguage.TAMIL) {
            englishToTamilMap[normalizedText]
        } else if (sourceLanguage == TranslateLanguage.TAMIL && targetLanguage == TranslateLanguage.ENGLISH) {
            tamilToEnglishMap[normalizedText]
        } else {
            null
        }

        if (exactMatch != null) return exactMatch

        // 2. Curated intent rule
        if (sourceLanguage == TranslateLanguage.TAMIL && targetLanguage == TranslateLanguage.ENGLISH) {
            val hasDoctor = doctorTerms.any { normalizedText.contains(it) }
            val hasNeedSee = needSeeTerms.any { normalizedText.contains(it) }
            if (hasDoctor && hasNeedSee) {
                return "I need to see a doctor."
            }
        }

        // 3. Existing ML Kit fallback
        return null
    }

    private fun normalize(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("""[?.!,;:'"“”‘’]"""), "")
            .replace(Regex("""\s+"""), " ")
    }
}
