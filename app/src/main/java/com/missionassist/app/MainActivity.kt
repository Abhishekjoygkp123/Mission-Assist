package com.missionassist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.missionassist.app.ui.theme.MissionAssistTheme
import com.missionassist.app.ui.conversation.ConversationScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MissionAssistTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConversationScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}