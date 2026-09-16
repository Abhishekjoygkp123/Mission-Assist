package com.missionassist.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.missionassist.app.ui.conversation.ConversationScreen
import com.missionassist.app.ui.situation.SituationScreen

enum class Destination {
    CONVERSATION, SITUATION, ASSISTANCE, MINISTRY
}

@Composable
fun MissionAssistApp(modifier: Modifier = Modifier) {
    var currentDestination by rememberSaveable { mutableStateOf(Destination.CONVERSATION) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("💬") },
                    label = { Text("Conversation") },
                    selected = currentDestination == Destination.CONVERSATION,
                    onClick = { currentDestination = Destination.CONVERSATION }
                )
                NavigationBarItem(
                    icon = { Text("📋") },
                    label = { Text("Situation") },
                    selected = currentDestination == Destination.SITUATION,
                    onClick = { currentDestination = Destination.SITUATION }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentDestination) {
                Destination.CONVERSATION -> ConversationScreen()
                Destination.SITUATION -> SituationScreen(
                    onNavigateToAssistance = { currentDestination = Destination.ASSISTANCE },
                    onNavigateToMinistry = { currentDestination = Destination.MINISTRY }
                )
                Destination.ASSISTANCE -> com.missionassist.app.assistance.ui.AssistanceScreen(
                    onNavigateUp = { currentDestination = Destination.SITUATION },
                    onUseInConversation = { /* Stage F */ }
                )
                Destination.MINISTRY -> com.missionassist.app.ministry.ui.MinistryScreen()
            }
        }
    }
}
