package com.missionassist.app.ministry.model

enum class MinistryCategory(val displayName: String) {
    SCRIPTURE("Scripture"),
    PRAYER_PROMPTS("Prayer prompts"),
    MINISTRY_CONVERSATION_AIDS("Ministry conversation aids"),
    SHORT_FIELD_NOTES("Short field notes")
}

data class MinistryResource(
    val id: String,
    val title: String,
    val category: MinistryCategory,
    val description: String,
    val content: String
)