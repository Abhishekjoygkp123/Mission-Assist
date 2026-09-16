package com.missionassist.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.missionassist.app.ui.theme.MissionAssistTheme
import com.missionassist.app.ui.navigation.MissionAssistApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MissionAssistTheme {
                MissionAssistApp()
            }
        }
    }
}