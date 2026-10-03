package com.missionassist.app.ministry.data

import com.missionassist.app.ministry.model.MinistryResource
import com.missionassist.app.ministry.model.MinistryCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MinistryRepository {
    fun getResources(): Flow<List<MinistryResource>> = flow {
        emit(listOf(
            // Scripture
            MinistryResource(
                "s1", "John 3:16", MinistryCategory.SCRIPTURE, "Core Gospel Message", "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.",
                canonicalTamilText = "தேவன், தம்முடைய ஒரேபேறான குமாரனை விசுவாசிக்கிறவன் எவனோ அவன் கெட்டுப்போகாமல் நித்தியஜீவனை அடையும்படிக்கு, அவரைத் தந்தருளி, இவ்வளவாய் உலகத்தில் அன்பு கூர்ந்தார்.",
                sourceCitation = "Copyright © 2017 by The Bible Society of India. Used by permission. All rights reserved worldwide."
            ),
            MinistryResource(
                "s2", "Psalm 23", MinistryCategory.SCRIPTURE, "Comfort and Protection", "The Lord is my shepherd, I lack nothing. He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul.",
                canonicalTamilText = "கர்த்தர் என் மேய்ப்பராயிருக்கிறார்; நான் தாழ்ச்சியடையேன். அவர் என்னைப் புல்லுள்ள இடங்களில் மேய்த்து, அமர்ந்த தண்ணீர்கள் அண்டையில் என்னைக் கொண்டுபோய் விடுகிறார். அவர் என் ஆத்துமாவைத் தேற்றி, தம்முடைய நாமத்தினிமித்தம் என்னை நீதியின் பாதைகளில் நடத்துகிறார்.",
                sourceCitation = "Copyright © 2017 by The Bible Society of India. Used by permission. All rights reserved worldwide."
            ),

            // Prayer prompts
            MinistryResource("p1", "Morning Dedication", MinistryCategory.PRAYER_PROMPTS, "Start of day dedication", "Lord, guide my steps today and grant me wisdom in my interactions. Let me reflect Your love to everyone I meet."),
            MinistryResource("p2", "Healing Prayer", MinistryCategory.PRAYER_PROMPTS, "Prayer for the sick", "Lord, we ask for your healing hand upon this person. Grant them strength, comfort, and swift recovery."),

            // Ministry conversation aids
            MinistryResource("mca1", "Sharing Personal Testimony", MinistryCategory.CONVERSATION_AIDS, "How to share your story", "1. Share your life before Christ.\n2. Explain how you met Christ.\n3. Describe how your life has changed."),
            MinistryResource("mca2", "Approaching Strangers", MinistryCategory.CONVERSATION_AIDS, "Icebreakers for ministry", "Start with a friendly greeting, ask how their day is going, and gently transition to spiritual topics by asking if there is anything they would like prayer for."),

            // Short field notes
            MinistryResource("fn1", "Village A Demographics", MinistryCategory.FIELD_NOTES, "Quick stats for Village A", "Population: ~500. Main occupation: Agriculture. Friendly but hesitant to discuss religion openly."),
            MinistryResource("fn2", "Next Week's Goals", MinistryCategory.FIELD_NOTES, "Upcoming outreach targets", "Focus on visiting the southern district families and distributing the remaining water filters.")
        ))
    }
}
