package com.missionassist.app.ministry.data

import com.missionassist.app.ministry.model.MinistryCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MinistryRepositoryTest {
    @Test
    fun testGetMinistryResourcesReturnsData() = runBlocking {
        val repo = MinistryRepository()
        val resources = repo.getResources().first()
        assertTrue(resources.isNotEmpty())
        
        val firstResource = resources.find { it.id == "s1" }
        assertEquals("John 3:16", firstResource?.title)
        assertEquals(MinistryCategory.SCRIPTURE, firstResource?.category)
    }
}
