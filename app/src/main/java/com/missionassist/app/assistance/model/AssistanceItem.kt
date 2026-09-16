package com.missionassist.app.assistance.model

data class AssistanceItem(
    val id: String,
    val title: String,
    val guidance: String,
    val canonicalEnglishPhrase: String,
    val canonicalTamilPhrase: String?,
    val category: AssistanceCategory,
    val criticality: AssistanceCriticality,
    val bilingualReviewStatus: String
)