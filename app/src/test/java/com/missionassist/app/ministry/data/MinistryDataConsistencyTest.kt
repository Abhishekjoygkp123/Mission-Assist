package com.missionassist.app.ministry.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MinistryDataConsistencyTest {

    @Test
    fun testMinistryRepositorySanityItemCountAndUniqueIds() = runBlocking {
        val repo = MinistryRepository()
        val resources = repo.getResources().first()

        assertEquals(8, resources.size)
        val uniqueIds = resources.map { it.id }.toSet()
        assertEquals(8, uniqueIds.size)
    }

    @Test
    fun testTamilTextIntegrityNoCorruptionCharactersAndValidUnicodeBlock() = runBlocking {
        val repo = MinistryRepository()
        val resources = repo.getResources().first()
        val tamilTexts = resources.mapNotNull { it.canonicalTamilText }

        assertTrue("Expected non-empty list of resources with canonicalTamilText", tamilTexts.isNotEmpty())

        for (text in tamilTexts) {
            assertFalse("Tamil text must not contain replacement character U+FFFD", text.contains('\uFFFD'))
            assertFalse("Tamil text must not contain '?' substitution character", text.contains('?'))
            assertTrue("Tamil text must contain at least one character in Tamil Unicode block (U+0B80..U+0BFF)", text.any { it in '\u0B80'..'\u0BFF' })
        }
    }

    @Test
    fun testCanonicalTamilTextHasNonBlankSourceCitation() = runBlocking {
        val repo = MinistryRepository()
        val resources = repo.getResources().first()

        for (resource in resources) {
            if (resource.canonicalTamilText != null) {
                assertNotNull("Resource ${resource.id} with canonicalTamilText must have non-null sourceCitation", resource.sourceCitation)
                assertTrue("Resource ${resource.id} sourceCitation must not be blank", resource.sourceCitation!!.isNotBlank())
            }
        }
    }
}
