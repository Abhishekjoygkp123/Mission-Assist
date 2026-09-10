package com.missionassist.app.ministry.data

import com.missionassist.app.ministry.model.MinistryCategory
import com.missionassist.app.ministry.model.MinistryResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MinistryRepository {
    fun getResources(): Flow<List<MinistryResource>> = flow {
        emit(listOf(
            // Scripture
            MinistryResource("s1", "John 3:16", MinistryCategory.SCRIPTURE, "Core Gospel Message", "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life."),
            MinistryResource("s2", "Psalm 23", MinistryCategory.SCRIPTURE, "Comfort and Protection", "The Lord is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul."),
            
            // Prayer prompts
            MinistryResource("p1", "Morning Dedication", MinistryCategory.PRAYER_PROMPTS, "Start of day dedication", "Lord, guide my steps today and grant me wisdom in my interactions. Let me reflect Your love to everyone I meet."),
            MinistryResource("p2", "Healing Prayer", MinistryCategory.PRAYER_PROMPTS, "Prayer for the sick", "Lord, we ask for your healing hand upon this person. Grant them strength, comfort, and swift recovery."),
            
            // Ministry conversation aids
            MinistryResource("mca1", "Sharing Personal Testimony", MinistryCategory.MINISTRY_CONVERSATION_AIDS, "How to share your story", "1. Share your life before Christ.\n2. Explain how you met Christ.\n3. Describe how your life has changed."),
            MinistryResource("mca2", "Approaching Strangers", MinistryCategory.MINISTRY_CONVERSATION_AIDS, "Icebreakers for ministry", "Start with a friendly greeting, ask how their day is going, and gently transition to spiritual topics by asking if there is anything they would like prayer for."),
            
            // Short field notes
            MinistryResource("fn1", "Village A Demographics", MinistryCategory.SHORT_FIELD_NOTES, "Quick stats for Village A", "Population: ~500. Main occupation: Agriculture. Friendly but hesitant to discuss religion openly."),
            MinistryResource("fn2", "Next Week's Goals", MinistryCategory.SHORT_FIELD_NOTES, "Upcoming outreach targets", "Focus on visiting the southern district families and distributing the remaining water filters.")
        ))
    }
}
