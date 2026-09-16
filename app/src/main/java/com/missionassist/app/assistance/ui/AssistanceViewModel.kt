package com.missionassist.app.assistance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.missionassist.app.assistance.data.AssistanceRepository
import com.missionassist.app.assistance.model.AssistanceCategory
import com.missionassist.app.assistance.model.AssistanceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AssistanceNavState {
    object Home : AssistanceNavState()
    data class CategoryList(val category: AssistanceCategory) : AssistanceNavState()
    data class ItemDetail(val item: AssistanceItem) : AssistanceNavState()
}

class AssistanceViewModel(
    private val initialCategory: AssistanceCategory? = null
) : ViewModel() {
    private val repository = AssistanceRepository()

    val isDeepLinked: Boolean = initialCategory != null

    private val _allItems = MutableStateFlow<List<AssistanceItem>>(emptyList())
    val allItems: StateFlow<List<AssistanceItem>> = _allItems.asStateFlow()

    private val _navState = MutableStateFlow<AssistanceNavState>(
        initialCategory?.let { AssistanceNavState.CategoryList(it) } ?: AssistanceNavState.Home
    )
    val navState: StateFlow<AssistanceNavState> = _navState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            repository.getAssistanceItems().collect { items ->
                _allItems.value = items
            }
        }
    }

    fun getCategories(): List<AssistanceCategory> {
        return _allItems.value.map { it.category }.distinct()
    }

    fun getItemsForCategory(category: AssistanceCategory): List<AssistanceItem> {
        return _allItems.value.filter { it.category == category }
    }

    fun navigateToCategory(category: AssistanceCategory) {
        _navState.value = AssistanceNavState.CategoryList(category)
    }

    fun navigateToItemDetail(item: AssistanceItem) {
        _navState.value = AssistanceNavState.ItemDetail(item)
    }

    fun navigateBack() {
        val currentState = _navState.value
        _navState.value = when (currentState) {
            is AssistanceNavState.ItemDetail -> AssistanceNavState.CategoryList(currentState.item.category)
            is AssistanceNavState.CategoryList -> AssistanceNavState.Home
            AssistanceNavState.Home -> AssistanceNavState.Home
        }
    }
}
