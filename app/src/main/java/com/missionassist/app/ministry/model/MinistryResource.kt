package com.missionassist.app.ministry.model

data class MinistryResource(
    val id: String,
    val title: String,
    val category: MinistryCategory,
    val description: String,
    val content: String
)