package com.missionassist.app.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.missionassist.app.speech.SpeechRecognizerManager
import com.missionassist.app.speech.SpeechState
import com.missionassist.app.translation.TranslationManager
import com.missionassist.app.translation.TranslationState
import com.missionassist.app.tts.TtsManager
import com.missionassist.app.tts.TtsState

@Composable
fun ConversationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val speechManager = remember { SpeechRecognizerManager(context) }
    val speechState by speechManager.state.collectAsState()

    val translationManager = remember { TranslationManager() }
    val translationState by translationManager.state.collectAsState()

    val ttsManager = remember { TtsManager(context) }
    val ttsState by ttsManager.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            speechManager.destroy()
            translationManager.close()
            ttsManager.shutdown()
        }
    }

    LaunchedEffect(speechState) {
        val currentState = speechState
        if (currentState is SpeechState.Result) {
            translationManager.translate(currentState.text)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            speechManager.startListening()
        } else {
            speechManager.setPermissionDenied()
        }
    }

    val startConversation = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            speechManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

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
                val youSaidText = when (val state = speechState) {
                    is SpeechState.Idle -> "Your speech will appear here"
                    is SpeechState.Listening -> "Listening..."
                    is SpeechState.Result -> state.text
                    is SpeechState.Error -> state.message
                }
                Text(
                    text = youSaidText,
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
                val translatedText = when (val state = translationState) {
                    is TranslationState.Idle -> "Translated text will appear here"
                    is TranslationState.DownloadingModel -> "Downloading translation model..."
                    is TranslationState.Translating -> "Translating..."
                    is TranslationState.Success -> state.text
                    is TranslationState.Error -> state.message
                }
                Text(
                    text = translatedText,
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
                onClick = startConversation,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Start Conversation")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val currentTranslation = translationState
                    if (currentTranslation is TranslationState.Success) {
                        val currentTtsState = ttsState
                        if (currentTtsState is TtsState.Ready) {
                            ttsManager.speak(currentTranslation.text)
                        } else if (currentTtsState is TtsState.Error) {
                            Toast.makeText(context, currentTtsState.message, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Text-to-Speech is initializing...", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No translation available to play.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Play Translation")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
