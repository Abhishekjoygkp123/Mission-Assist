package com.missionassist.app.ministry.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.missionassist.app.ministry.data.MinistryRepository
import com.missionassist.app.ministry.model.MinistryCategory
import com.missionassist.app.ministry.model.MinistryResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MinistryNavState {
    object Home : MinistryNavState()
    data class CategoryList(val category: MinistryCategory) : MinistryNavState()
    data class ResourceDetail(val resource: MinistryResource) : MinistryNavState()
}

class MinistryViewModel : ViewModel() {
    private val repository = MinistryRepository()

    private val _allResources = MutableStateFlow<List<MinistryResource>>(emptyList())
    val allResources: StateFlow<List<MinistryResource>> = _allResources.asStateFlow()

    private val _navState = MutableStateFlow<MinistryNavState>(MinistryNavState.Home)
    val navState: StateFlow<MinistryNavState> = _navState.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch {
            repository.getResources().collect { resources ->
                _allResources.value = resources
            }
        }
    }

    fun getCategories(): List<MinistryCategory> {
        return MinistryCategory.values().toList()
    }

    fun getResourcesForCategory(category: MinistryCategory): List<MinistryResource> {
        return _allResources.value.filter { it.category == category }
    }

    fun navigateToCategory(category: MinistryCategory) {
        _navState.value = MinistryNavState.CategoryList(category)
    }

    fun navigateToResourceDetail(resource: MinistryResource) {
        _navState.value = MinistryNavState.ResourceDetail(resource)
    }

    fun navigateBack() {
        val currentState = _navState.value
        _navState.value = when (currentState) {
            is MinistryNavState.ResourceDetail -> MinistryNavState.CategoryList(currentState.resource.category)
            is MinistryNavState.CategoryList -> MinistryNavState.Home
            MinistryNavState.Home -> MinistryNavState.Home
        }
    }
}
