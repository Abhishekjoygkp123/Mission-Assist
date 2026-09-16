package com.missionassist.app.assistance.data

import com.missionassist.app.assistance.model.AssistanceCategory
import com.missionassist.app.assistance.model.AssistanceCriticality
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistanceRepositoryTest {
    @Test
    fun testGetAssistanceItemsReturnsData() = runBlocking {
        val repo = AssistanceRepository()
        val items = repo.getAssistanceItems().first()
        assertTrue(items.isNotEmpty())
        
        val doctorReq = items.find { it.id == "m1" }
        assertEquals("Doctor Request", doctorReq?.title)
        assertEquals(AssistanceCategory.MEDICAL_HELP, doctorReq?.category)
        assertEquals(AssistanceCriticality.HIGH, doctorReq?.criticality)
    }
}
