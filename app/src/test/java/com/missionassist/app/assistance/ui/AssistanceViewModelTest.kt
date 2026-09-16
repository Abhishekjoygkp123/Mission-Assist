package com.missionassist.app.assistance.ui

import com.missionassist.app.assistance.model.AssistanceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssistanceViewModelTest {
    
    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateIsHome() {
        val viewModel = AssistanceViewModel()
        assertEquals(AssistanceNavState.Home, viewModel.navState.value)
    }

    @Test
    fun testInitialStateWithInitialCategory() {
        val viewModel = AssistanceViewModel(initialCategory = AssistanceCategory.EMERGENCY_HELP)
        val state = viewModel.navState.value
        assertTrue(state is AssistanceNavState.CategoryList)
        assertEquals(AssistanceCategory.EMERGENCY_HELP, (state as AssistanceNavState.CategoryList).category)
        assertTrue(viewModel.isDeepLinked)
    }

    @Test
    fun testIsDeepLinkedIsFalseWhenNoInitialCategory() {
        val viewModel = AssistanceViewModel()
        Assert.assertFalse(viewModel.isDeepLinked)
    }

    @Test
    fun testNavigationToCategory() {
        val viewModel = AssistanceViewModel()
        viewModel.navigateToCategory(AssistanceCategory.MEDICAL_HELP)
        val state = viewModel.navState.value
        assertTrue(state is AssistanceNavState.CategoryList)
        assertEquals(AssistanceCategory.MEDICAL_HELP, (state as AssistanceNavState.CategoryList).category)
    }

    @Test
    fun testNavigateBackFromCategoryList() {
        val viewModel = AssistanceViewModel()
        viewModel.navigateToCategory(AssistanceCategory.MEDICAL_HELP)
        viewModel.navigateBack()
        assertEquals(AssistanceNavState.Home, viewModel.navState.value)
    }
}
