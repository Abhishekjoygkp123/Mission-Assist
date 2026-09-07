package com.missionassist.app.assistance.model

data class AssistanceItem(
    val id: String,
    val title: String,
    val guidance: String,
    val canonicalEnglishPhrase: String,
    val canonicalTamilPhrase: String?,
    val category: String,
    val criticality: String?,
    val bilingualReviewStatus: String
)