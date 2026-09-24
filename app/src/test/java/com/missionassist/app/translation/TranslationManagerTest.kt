package com.missionassist.app.translation

import com.google.mlkit.nl.translate.TranslateLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeTranslationClient : TranslationClient {
    var onSuccessDownload: (() -> Unit)? = null
    var onSuccessTranslate: ((String) -> Unit)? = null

    override fun downloadModelIfNeeded(
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        onSuccessDownload = onSuccess
    }

    override fun translate(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        onSuccessTranslate = onSuccess
    }

    override fun close() {}

    fun simulateDownloadSuccess() {
        onSuccessDownload?.invoke()
    }

    fun simulateTranslationSuccess(text: String) {
        onSuccessTranslate?.invoke(text)
    }
}

class TranslationManagerTest {

    @Test
    fun testStaleTranslationCallbackIgnoredAfterNewerRequest() {
        val fakeClient = FakeTranslationClient()
        val manager = TranslationManager(fakeClient)

        // First request
        manager.translate("Hello", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL)
        val firstDownloadSuccess = fakeClient.onSuccessDownload

        // Second request
        manager.translate("World", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL)
        
        // Simulate late download success for the first request
        firstDownloadSuccess?.invoke()

        // It should NOT transition to Translating state from the first request's callback, 
        // since the second request already set it back to DownloadingModel.
        // Wait, the second request will have its own download request.
        // Let's check state.
        assertEquals(TranslationState.DownloadingModel, manager.state.value)
    }

    @Test
    fun testStaleTranslationCallbackIgnoredAfterClear() {
        val fakeClient = FakeTranslationClient()
        val manager = TranslationManager(fakeClient)

        // First request
        manager.translate("Test ML Kit", TranslateLanguage.ENGLISH, TranslateLanguage.TAMIL)
        
        fakeClient.simulateDownloadSuccess()
        val translateSuccess = fakeClient.onSuccessTranslate
        
        // Simulate a clear (swap)
        manager.clear()

        // Simulate late translation success
        translateSuccess?.invoke("Translated via ML Kit")

        // State should remain Idle
        assertEquals(TranslationState.Idle, manager.state.value)
    }

    @Test
    fun testSetTranslationCreatesSuccessState() {
        val fakeClient = FakeTranslationClient()
        val manager = TranslationManager(fakeClient)

        manager.setTranslation("எனக்கு வலி இருக்குது", TranslationSource.VERIFIED_PHRASE)

        val state = manager.state.value as TranslationState.Success
        assertEquals("எனக்கு வலி இருக்குது", state.text)
        assertEquals(TranslationSource.VERIFIED_PHRASE, state.source)
    }
}
