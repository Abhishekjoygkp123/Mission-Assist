package com.missionassist.app.assistance.data

import com.missionassist.app.assistance.model.AssistanceItem
import com.missionassist.app.assistance.model.AssistanceCategory
import com.missionassist.app.assistance.model.AssistanceCriticality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AssistanceRepository {
    fun getAssistanceItems(): Flow<List<AssistanceItem>> = flow {
        emit(listOf(
            // Medical Help required items
            AssistanceItem("m1", "Doctor Request", "Use when you need general medical attention.", "I need a doctor", null, AssistanceCategory.MEDICAL_HELP, AssistanceCriticality.HIGH, "Needs Bilingual Review"),
            AssistanceItem("m2", "Doctor Visit", "Use when asking to go to a clinic or doctor.", "I need to see a doctor", null, AssistanceCategory.MEDICAL_HELP, AssistanceCriticality.HIGH, "Needs Bilingual Review"),
            AssistanceItem("m3", "Ambulance Request", "Use for immediate severe medical emergencies.", "I need an ambulance", "எனக்கு ஆம்புலன்ஸ் தேவை", AssistanceCategory.MEDICAL_HELP, AssistanceCriticality.CRITICAL, "Reviewed"),
            AssistanceItem("m4", "Pain Report", "Use to indicate physical suffering.", "I am in pain", "எனக்கு வலி இருக்குது", AssistanceCategory.MEDICAL_HELP, AssistanceCriticality.MEDIUM, "Reviewed"),
            AssistanceItem("m5", "Hospital Location", "Use to find the closest medical facility.", "Where is the nearest hospital?", null, AssistanceCategory.MEDICAL_HELP, AssistanceCriticality.HIGH, "Needs Bilingual Review"),
            AssistanceItem("m6", "Medicine Request", "Use to request medication or a pharmacy.", "I need medicine", "எனக்கு மருந்து வேண்டும்", AssistanceCategory.MEDICAL_HELP, AssistanceCriticality.MEDIUM, "Reviewed"),

            // Emergency Help category
            AssistanceItem("e1", "Police Request", "Use for security emergencies.", "Call the police", "போலீஸை கூப்பிடுங்க", AssistanceCategory.EMERGENCY_HELP, AssistanceCriticality.CRITICAL, "Reviewed"),
            AssistanceItem("e2", "Fire Report", "Use to report a fire.", "There is a fire", "அங்கே தீ பிடித்துவிட்டது", AssistanceCategory.EMERGENCY_HELP, AssistanceCriticality.CRITICAL, "Reviewed"),

            // Essential Needs category
            AssistanceItem("en1", "Water Request", "Use for hydration.", "I need drinking water", "எனக்கு குடிக்கத்தண்ணீர் வேண்டும்", AssistanceCategory.ESSENTIAL_NEEDS, AssistanceCriticality.MEDIUM, "Reviewed"),
            AssistanceItem("en2", "Food Request", "Use for sustenance.", "I need food", null, AssistanceCategory.ESSENTIAL_NEEDS, AssistanceCriticality.MEDIUM, "Needs Bilingual Review")
        ))
    }
}
