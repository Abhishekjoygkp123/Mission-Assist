package com.missionassist.app.ministry.ui

import com.missionassist.app.ministry.model.MinistryCategory
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
class MinistryViewModelTest {

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
        val viewModel = MinistryViewModel()
        assertEquals(MinistryNavState.Home, viewModel.navState.value)
    }

    @Test
    fun testInitialStateWithInitialCategory() {
        val viewModel = MinistryViewModel(initialCategory = MinistryCategory.SCRIPTURE)
        val state = viewModel.navState.value
        assertTrue(state is MinistryNavState.CategoryList)
        assertEquals(MinistryCategory.SCRIPTURE, (state as MinistryNavState.CategoryList).category)
        assertTrue(viewModel.isDeepLinked)
    }

    @Test
    fun testIsDeepLinkedIsFalseWhenNoInitialCategory() {
        val viewModel = MinistryViewModel()
        Assert.assertFalse(viewModel.isDeepLinked)
    }

    @Test
    fun testGetCategoriesReturnsAllEnums() {
        val viewModel = MinistryViewModel()
        val categories = viewModel.getCategories()
        assertEquals(MinistryCategory.entries.toList(), categories)
    }

    @Test
    fun testNavigateToCategory() {
        val viewModel = MinistryViewModel()
        viewModel.navigateToCategory(MinistryCategory.PRAYER_PROMPTS)
        val state = viewModel.navState.value
        assertTrue(state is MinistryNavState.CategoryList)
        assertEquals(MinistryCategory.PRAYER_PROMPTS, (state as MinistryNavState.CategoryList).category)
    }

    @Test
    fun testNavigateBackReturnsHome() {
        val viewModel = MinistryViewModel()
        viewModel.navigateToCategory(MinistryCategory.SCRIPTURE)
        viewModel.navigateBack()
        assertEquals(MinistryNavState.Home, viewModel.navState.value)
    }
}
