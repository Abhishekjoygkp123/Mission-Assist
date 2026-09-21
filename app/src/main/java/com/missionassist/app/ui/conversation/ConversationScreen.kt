package com.missionassist.app.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.missionassist.app.speech.DefaultSpeechClient
import com.missionassist.app.speech.SpeechCapability
import com.missionassist.app.speech.SpeechRecognizerManager
import com.missionassist.app.speech.SpeechState
import com.missionassist.app.translation.DefaultTranslationClient
import com.missionassist.app.translation.TranslationManager
import com.missionassist.app.translation.TranslationSource
import com.missionassist.app.translation.TranslationState
import com.missionassist.app.tts.TtsManager
import com.missionassist.app.tts.TtsState

@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
    initialText: String? = null
) {
    val context = LocalContext.current
    val speechManager = remember { SpeechRecognizerManager(DefaultSpeechClient(context)) }
    val speechState by speechManager.state.collectAsState()
    val speechCapabilityState by speechManager.capabilityState.collectAsState()
    val speechDownloadError by speechManager.downloadError.collectAsState()

    val translationManager = remember { TranslationManager(DefaultTranslationClient()) }
    val translationState by translationManager.state.collectAsState()

    val ttsManager = remember { TtsManager(context) }
    val ttsState by ttsManager.state.collectAsState()

    var conversationDirection by remember { mutableStateOf(ConversationDirection.ENGLISH_TO_TAMIL) }
    var inputText by remember { mutableStateOf(initialText ?: "") }

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
            inputText = currentState.text
        }
    }

    LaunchedEffect(conversationDirection) {
        val lang = conversationDirection.speechLang
        val cached = com.missionassist.app.speech.SpeechCapabilityCache.get(lang)
        if (cached != null && com.missionassist.app.speech.SpeechCapabilityCache.isGood(cached)) {
            speechManager.setCapabilityState(cached)
        } else {
            speechManager.checkCapability(lang)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            speechManager.startListening(conversationDirection.speechLang)
        } else {
            speechManager.setPermissionDenied()
        }
    }

    val startConversation = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            speechManager.startListening(conversationDirection.speechLang)
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
            Text(text = conversationDirection.labelLeft, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { 
                conversationDirection = conversationDirection.swap()
                speechManager.clear()
                translationManager.clear()
                inputText = ""
            }) {
                Text(
                    text = "↔",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(text = conversationDirection.labelRight, style = MaterialTheme.typography.titleMedium)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val langName = conversationDirection.labelLeft
                val capabilityText = when (speechCapabilityState) {
                    SpeechCapability.ON_DEVICE_AVAILABLE -> "$langName speech is ready for offline use."
                    SpeechCapability.ON_DEVICE_NOT_DOWNLOADED -> "$langName speech can work offline if its speech model is downloaded."
                    SpeechCapability.ON_DEVICE_DOWNLOADING -> "$langName speech model download is pending/in progress."
                    SpeechCapability.ONLINE_ONLY -> "$langName speech currently requires internet on this device."
                    SpeechCapability.UNSUPPORTED -> "$langName speech is not supported by the active speech service."
                    SpeechCapability.DETECTION_UNAVAILABLE -> "Detailed offline speech support cannot be checked on this Android version."
                    null -> "Checking speech support..."
                }
                
                Text(
                    text = capabilityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (speechDownloadError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = speechDownloadError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (speechCapabilityState == SpeechCapability.ON_DEVICE_NOT_DOWNLOADED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { speechManager.triggerModelDownload(conversationDirection.speechLang) }) {
                            Text("Download for Offline Use")
                        }
                        Button(onClick = {
                            com.missionassist.app.speech.SpeechCapabilityCache.remove(conversationDirection.speechLang)
                            val success = speechManager.openSpeechSettings()
                            if (!success) {
                                Toast.makeText(context, "Cannot open settings on this device.", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Open Speech Settings")
                        }
                    }
                } else if (speechCapabilityState == SpeechCapability.ON_DEVICE_DOWNLOADING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { speechManager.checkCapability(conversationDirection.speechLang) }) {
                        Text("Check Again")
                    }
                } else if (
                    speechCapabilityState == SpeechCapability.ONLINE_ONLY ||
                    speechCapabilityState == SpeechCapability.UNSUPPORTED ||
                    speechCapabilityState == SpeechCapability.DETECTION_UNAVAILABLE
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        com.missionassist.app.speech.SpeechCapabilityCache.remove(conversationDirection.speechLang)
                        val success = speechManager.openSpeechSettings()
                        if (!success) {
                            Toast.makeText(context, "Cannot open settings on this device.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Open Speech Settings")
                    }
                }
            }
        }

        // Input Section
        Column(modifier = Modifier.fillMaxWidth()) {
            val statusText = when (val state = speechState) {
                is SpeechState.Idle -> ""
                is SpeechState.Listening -> "Listening..."
                is SpeechState.Result -> "Speech captured"
                is SpeechState.Error -> state.message
            }
            androidx.compose.material3.OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("You said") },
                supportingText = if (statusText.isNotEmpty()) { { Text(statusText) } } else null,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
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
            
            val currentState = translationState
            if (currentState is TranslationState.Success) {
                Spacer(modifier = Modifier.height(4.dp))
                if (currentState.source == TranslationSource.VERIFIED_PHRASE) {
                    Text(
                        text = "Curated field phrase",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (currentState.source == TranslationSource.ML_KIT) {
                    Text(
                        text = "Offline AI translation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "AI translation may lose context. Verify critical information.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                    if (inputText.isNotBlank()) {
                        translationManager.translate(
                            inputText, 
                            conversationDirection.sourceMlKitLang, 
                            conversationDirection.targetMlKitLang
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Translate")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val currentTranslation = translationState
                    if (currentTranslation is TranslationState.Success) {
                        val currentTtsState = ttsState
                        if (currentTtsState is TtsState.Ready) {
                            ttsManager.speak(currentTranslation.text, conversationDirection.ttsLang)
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
