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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.missionassist.app.ui.conversation.ConversationScreen
import com.missionassist.app.ui.situation.SituationScreen

import com.missionassist.app.assistance.model.AssistanceCategory
import com.missionassist.app.ministry.model.MinistryCategory

enum class Destination {
    CONVERSATION, SITUATION, ASSISTANCE, MINISTRY
}

@Composable
fun MissionAssistApp(modifier: Modifier = Modifier) {
    var currentDestination by rememberSaveable { mutableStateOf(Destination.CONVERSATION) }
    var assistanceInitialCategory by remember { mutableStateOf<AssistanceCategory?>(null) }
    var ministryInitialCategory by remember { mutableStateOf<MinistryCategory?>(null) }
    var conversationInitialText by remember { mutableStateOf<String?>(null) }
    var conversationInitialTamilText by remember { mutableStateOf<String?>(null) }

    val onNavigateToAssistance = remember {
        { category: AssistanceCategory ->
            assistanceInitialCategory = category
            currentDestination = Destination.ASSISTANCE
        }
    }

    val onNavigateToMinistry = remember {
        { category: MinistryCategory ->
            ministryInitialCategory = category
            currentDestination = Destination.MINISTRY
        }
    }

    val onUseInConversation = remember {
        { item: com.missionassist.app.assistance.model.AssistanceItem ->
            conversationInitialText = item.canonicalEnglishPhrase
            conversationInitialTamilText = item.canonicalTamilPhrase
            currentDestination = Destination.CONVERSATION
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("💬") },
                    label = { Text("Conversation") },
                    selected = currentDestination == Destination.CONVERSATION,
                    onClick = { 
                        conversationInitialText = null
                        conversationInitialTamilText = null
                        currentDestination = Destination.CONVERSATION 
                    }
                )
                NavigationBarItem(
                    icon = { Text("🚨") },
                    label = { Text("Situation") },
                    selected = currentDestination == Destination.SITUATION,
                    onClick = { currentDestination = Destination.SITUATION }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentDestination) {
                Destination.CONVERSATION -> ConversationScreen(
                    initialText = conversationInitialText,
                    initialTamilText = conversationInitialTamilText
                )
                Destination.SITUATION -> SituationScreen(
                    onNavigateToAssistance = onNavigateToAssistance,
                    onNavigateToMinistry = onNavigateToMinistry
                )
                Destination.ASSISTANCE -> com.missionassist.app.assistance.ui.AssistanceScreen(
                    initialCategory = assistanceInitialCategory,
                    onNavigateUp = { currentDestination = Destination.SITUATION },
                    onUseInConversation = { item -> onUseInConversation(item) }
                )
                Destination.MINISTRY -> com.missionassist.app.ministry.ui.MinistryScreen(
                    initialCategory = ministryInitialCategory,
                    onNavigateUp = { currentDestination = Destination.SITUATION }
                )
            }
        }
    }
}
