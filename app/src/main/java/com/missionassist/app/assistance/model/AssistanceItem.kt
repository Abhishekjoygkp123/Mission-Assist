package com.missionassist.app.assistance.model

enum class AssistanceCategory(val displayName: String) {
    MEDICAL("Medical Help"),
    EMERGENCY("Emergency Help"),
    ESSENTIAL_NEEDS("Essential Needs")
}

enum class Criticality(val displayName: String) {
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical")
}

data class AssistanceItem(
    val id: String,
    val title: String,
    val guidance: String,
    val canonicalEnglishPhrase: String,
    val canonicalTamilPhrase: String?,
    val category: AssistanceCategory,
    val criticality: Criticality?,
    val bilingualReviewStatus: String
)