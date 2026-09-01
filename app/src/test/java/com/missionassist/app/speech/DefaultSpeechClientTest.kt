package com.missionassist.app.speech

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSpeechClientTest {

    @Test
    fun testShouldUseOnDevice() {
        // SDK >= S (31), available, not fallback -> true
        assertTrue(DefaultSpeechClient.shouldUseOnDevice(Build.VERSION_CODES.S, isOnDeviceAvailable = true, isFallback = false))
        
        // Fallback -> false
        assertFalse(DefaultSpeechClient.shouldUseOnDevice(Build.VERSION_CODES.S, isOnDeviceAvailable = true, isFallback = true))
        
        // Not available -> false
        assertFalse(DefaultSpeechClient.shouldUseOnDevice(Build.VERSION_CODES.S, isOnDeviceAvailable = false, isFallback = false))
        
        // SDK < S -> false
        assertFalse(DefaultSpeechClient.shouldUseOnDevice(Build.VERSION_CODES.R, isOnDeviceAvailable = true, isFallback = false))
    }

    @Test
    fun testShouldPreferOffline() {
        // useOnDevice = true, SDK >= M (23) -> true
        assertTrue(DefaultSpeechClient.shouldPreferOffline(Build.VERSION_CODES.M, useOnDevice = true))
        
        // useOnDevice = false -> false
        assertFalse(DefaultSpeechClient.shouldPreferOffline(Build.VERSION_CODES.M, useOnDevice = false))
        
        // SDK < M -> false
        assertFalse(DefaultSpeechClient.shouldPreferOffline(Build.VERSION_CODES.LOLLIPOP_MR1, useOnDevice = true))
    }
}
