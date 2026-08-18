package com.missionassist.app.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConversationScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MissionAssist",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "English", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { /* No functionality yet */ }) {
                Text(
                    text = "⇄",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(text = "Tamil", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "You said",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Text(
                    text = "Your speech will appear here",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Translation",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Text(
                    text = "Translated text will appear here",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { /* No functionality yet */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Start Conversation")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* No functionality yet */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Play Translation")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
