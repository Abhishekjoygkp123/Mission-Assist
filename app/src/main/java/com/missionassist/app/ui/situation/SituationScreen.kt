package com.missionassist.app.ui.situation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.missionassist.app.assistance.model.AssistanceCategory
import com.missionassist.app.ministry.model.MinistryCategory

data class SituationPrompt(val text: String, val onClick: () -> Unit)

@Composable
fun SituationScreen(
    modifier: Modifier = Modifier,
    onNavigateToAssistance: (AssistanceCategory) -> Unit,
    onNavigateToMinistry: (MinistryCategory) -> Unit
) {
    val prompts = remember(onNavigateToAssistance, onNavigateToMinistry) {
        listOf(
            SituationPrompt("Someone is injured or needs medical help") { onNavigateToAssistance(AssistanceCategory.MEDICAL_HELP) },
            SituationPrompt("This is an emergency") { onNavigateToAssistance(AssistanceCategory.EMERGENCY_HELP) },
            SituationPrompt("Someone needs food, water, or shelter") { onNavigateToAssistance(AssistanceCategory.ESSENTIAL_NEEDS) },
            SituationPrompt("Someone wants to talk about faith or Scripture") { onNavigateToMinistry(MinistryCategory.SCRIPTURE) },
            SituationPrompt("I need a prayer prompt") { onNavigateToMinistry(MinistryCategory.PRAYER_PROMPTS) },
            SituationPrompt("I need help having a ministry conversation") { onNavigateToMinistry(MinistryCategory.CONVERSATION_AIDS) }
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Situation Detection",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            prompts.forEach { prompt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { prompt.onClick() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = prompt.text,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
