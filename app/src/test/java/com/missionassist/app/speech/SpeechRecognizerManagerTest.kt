package com.missionassist.app.speech

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeSpeechClient : SpeechClient {
    var listener: SpeechClientListener? = null
    var startCount = 0
    var cancelCount = 0
    var destroyCount = 0

    var mockCapability: SpeechCapability = SpeechCapability.ON_DEVICE_AVAILABLE

    override fun isRecognitionAvailable(): Boolean = true

    override fun checkCapability(languageCode: String, onResult: (SpeechCapability) -> Unit) {
        onResult(mockCapability)
    }

    var triggerModelDownloadSuccess = true
    var triggerModelDownloadError: String? = null

    override fun triggerModelDownload(languageCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (triggerModelDownloadSuccess) {
            onSuccess()
        } else {
            onError(triggerModelDownloadError ?: "Unknown error")
        }
    }

    var openSpeechSettingsCount = 0
    var openSpeechSettingsSuccess = true

    override fun openSpeechSettings(): Boolean {
        openSpeechSettingsCount++
        return openSpeechSettingsSuccess
    }

    override fun startListening(languageCode: String, isFallback: Boolean, listener: SpeechClientListener) {
        startCount++
        this.listener = listener
    }

    override fun cancel() {
        cancelCount++
    }

    override fun destroy() {
        destroyCount++
    }

    fun simulateResults(text: String?) {
        listener?.onResults(text)
    }
}

class SpeechRecognizerManagerTest {

    @Test
    fun testStaleSpeechCallbackIgnoredAfterNewerSession() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        // Start first session
        manager.startListening("en-IN")
        val firstListener = fakeClient.listener

        // Start second session (newer)
        manager.startListening("en-IN")
        
        // Simulate late result from the first listener
        firstListener?.onResults("Stale result")

        // State should remain Idle (or Listening if we simulate ready event for second session, but default is Idle unless simulated)
        assertEquals(SpeechState.Listening, manager.state.value)
    }

    @Test
    fun testStaleSpeechCallbackIgnoredAfterClear() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        // Start session
        manager.startListening("en-IN")
        val listener = fakeClient.listener

        // Clear (simulate swap)
        manager.clear()

        // Simulate late result
        listener?.onResults("Stale result")

        // State should be Idle
        assertEquals(SpeechState.Idle, manager.state.value)
        assertEquals(1, fakeClient.cancelCount)
    }

    @Test
    fun testError11RetriesOnce() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        manager.startListening("en-IN")
        assertEquals(1, fakeClient.startCount)
        
        // Simulate error 11 (ERROR_SERVER_DISCONNECTED)
        fakeClient.listener?.onError(11)

        // It should retry, so startCount becomes 2, and state is Listening
        assertEquals(2, fakeClient.startCount)
        assertEquals(SpeechState.Listening, manager.state.value)
    }

    @Test
    fun testSecondError11StopsAndReportsFailure() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        manager.startListening("en-IN")
        
        // Simulate first error 11
        fakeClient.listener?.onError(11)
        
        // Simulate second error 11 (on the new listener)
        fakeClient.listener?.onError(11)

        // It should stop retrying and report failure
        assertEquals(2, fakeClient.startCount) // no third retry
        val state = manager.state.value
        assertTrue(state is SpeechState.Error)
        assertEquals("Server disconnected. Please try again.", (state as SpeechState.Error).message)
    }

    @Test
    fun testCheckCapabilityEmitsState() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        fakeClient.mockCapability = SpeechCapability.ON_DEVICE_DOWNLOADING
        manager.checkCapability("ta-IN")

        assertEquals(SpeechCapability.ON_DEVICE_DOWNLOADING, manager.capabilityState.value)
    }

    @Test
    fun testSuccessfulDownloadTriggersCapabilityCheck() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        // Given successful download
        fakeClient.triggerModelDownloadSuccess = true
        fakeClient.mockCapability = SpeechCapability.ON_DEVICE_AVAILABLE

        // When triggering download
        manager.triggerModelDownload("ta-IN")

        // Then download error is null, and capability is re-checked successfully
        assertNull(manager.downloadError.value)
        assertEquals(SpeechCapability.ON_DEVICE_AVAILABLE, manager.capabilityState.value)
    }

    @Test
    fun testFailedDownloadExposesError() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        // Given failed download
        fakeClient.triggerModelDownloadSuccess = false
        fakeClient.triggerModelDownloadError = "Download failed"
        manager.checkCapability("ta-IN") // Set initial state
        
        // When triggering download
        manager.triggerModelDownload("ta-IN")

        // Then error is exposed
        assertEquals("Download failed", manager.downloadError.value)
    }

    @Test
    fun testSettingsFallbackActionIsExposedCorrectly() {
        val fakeClient = FakeSpeechClient()
        val manager = SpeechRecognizerManager(fakeClient)

        fakeClient.openSpeechSettingsSuccess = true
        val result = manager.openSpeechSettings()

        assertTrue(result)
        assertEquals(1, fakeClient.openSpeechSettingsCount)
    }
}
