package com.missionassist.app.assistance.data

import com.google.mlkit.nl.translate.TranslateLanguage
import com.missionassist.app.translation.PhraseResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistanceDataConsistencyTest {

    @Test
    fun testAssistanceRepositorySanityItemCountAndUniqueIds() = runBlocking {
        val repo = AssistanceRepository()
        val items = repo.getAssistanceItems().first()

        assertEquals(10, items.size)
        val uniqueIds = items.map { it.id }.toSet()
        assertEquals(10, uniqueIds.size)
    }

    @Test
    fun testCanonicalTamilPhraseMatchesPhraseResolver() = runBlocking {
        val repo = AssistanceRepository()
        val items = repo.getAssistanceItems().first()
        val verifiedItems = items.filter { it.canonicalTamilPhrase != null }

        assertTrue("Expected verified items with canonicalTamilPhrase to not be empty", verifiedItems.isNotEmpty())

        for (item in verifiedItems) {
            val resolved = PhraseResolver.resolve(
                item.canonicalEnglishPhrase,
                TranslateLanguage.ENGLISH,
                TranslateLanguage.TAMIL
            )
            assertNotNull("PhraseResolver failed to resolve: ${item.canonicalEnglishPhrase}", resolved)
            assertEquals("PhraseResolver resolution must match canonicalTamilPhrase for item ${item.id}", item.canonicalTamilPhrase, resolved)
        }
    }

    @Test
    fun testBilingualReviewStatusConsistency() = runBlocking {
        val repo = AssistanceRepository()
        val items = repo.getAssistanceItems().first()

        for (item in items) {
            if (item.bilingualReviewStatus == "Reviewed") {
                assertNotNull("Item ${item.id} with status 'Reviewed' must have non-null canonicalTamilPhrase", item.canonicalTamilPhrase)
            }
            if (item.canonicalTamilPhrase != null) {
                assertEquals("Item ${item.id} with non-null canonicalTamilPhrase must have status 'Reviewed'", "Reviewed", item.bilingualReviewStatus)
            }
        }
    }

    @Test
    fun testTamilTextIntegrityNoCorruptionCharactersAndValidUnicodeBlock() = runBlocking {
        val repo = AssistanceRepository()
        val items = repo.getAssistanceItems().first()
        val tamilPhrases = items.mapNotNull { it.canonicalTamilPhrase }

        assertTrue(tamilPhrases.isNotEmpty())

        for (phrase in tamilPhrases) {
            assertFalse("Tamil phrase must not contain replacement character U+FFFD", phrase.contains('\uFFFD'))
            assertFalse("Tamil phrase must not contain '?' substitution character", phrase.contains('?'))
            assertTrue("Tamil phrase must contain at least one character in Tamil Unicode block (U+0B80..U+0BFF)", phrase.any { it in '\u0B80'..'\u0BFF' })
        }
    }
}
