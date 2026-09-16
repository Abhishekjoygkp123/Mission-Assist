package com.missionassist.app.ui.situation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SituationScreen(
    modifier: Modifier = Modifier,
    onNavigateToAssistance: () -> Unit,
    onNavigateToMinistry: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Situation",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToAssistance) {
                Text("Assistance")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToMinistry) {
                Text("Ministry")
            }
        }
    }
}
