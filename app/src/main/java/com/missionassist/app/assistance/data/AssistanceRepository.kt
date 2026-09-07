package com.missionassist.app.assistance.data

import com.missionassist.app.assistance.model.AssistanceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AssistanceRepository {
    fun getAssistanceItems(): Flow<List<AssistanceItem>> = flow {
        emit(listOf(
            // Medical Help required items
            AssistanceItem("m1", "Doctor Request", "Use when you need general medical attention.", "I need a doctor", null, "Medical Help", "High", "Needs Bilingual Review"),
            AssistanceItem("m2", "Doctor Visit", "Use when asking to go to a clinic or doctor.", "I need to see a doctor", null, "Medical Help", "High", "Needs Bilingual Review"),
            AssistanceItem("m3", "Ambulance Request", "Use for immediate severe medical emergencies.", "I need an ambulance", null, "Medical Help", "Critical", "Needs Bilingual Review"),
            AssistanceItem("m4", "Pain Report", "Use to indicate physical suffering.", "I am in pain", null, "Medical Help", "Medium", "Needs Bilingual Review"),
            AssistanceItem("m5", "Hospital Location", "Use to find the closest medical facility.", "Where is the nearest hospital?", null, "Medical Help", "High", "Needs Bilingual Review"),
            AssistanceItem("m6", "Medicine Request", "Use to request medication or a pharmacy.", "I need medicine", null, "Medical Help", "Medium", "Needs Bilingual Review"),

            // Emergency Help category
            AssistanceItem("e1", "Police Request", "Use for security emergencies.", "I need the police", null, "Emergency Help", "Critical", "Needs Bilingual Review"),
            AssistanceItem("e2", "Fire Report", "Use to report a fire.", "There is a fire", null, "Emergency Help", "Critical", "Needs Bilingual Review"),

            // Essential Needs category
            AssistanceItem("en1", "Water Request", "Use for hydration.", "I need drinking water", null, "Essential Needs", "Medium", "Needs Bilingual Review"),
            AssistanceItem("en2", "Food Request", "Use for sustenance.", "I need food", null, "Essential Needs", "Medium", "Needs Bilingual Review")
        ))
    }
}
