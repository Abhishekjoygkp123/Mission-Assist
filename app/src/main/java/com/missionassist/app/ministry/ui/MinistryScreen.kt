package com.missionassist.app.ministry.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.missionassist.app.ministry.model.MinistryCategory
import com.missionassist.app.ministry.model.MinistryResource

@Composable
fun MinistryScreen(
    modifier: Modifier = Modifier,
    viewModel: MinistryViewModel = remember { MinistryViewModel() },
    onNavigateUp: () -> Unit
) {
    val navState by viewModel.navState.collectAsState()

    BackHandler(enabled = true) {
        if (navState is MinistryNavState.Home) {
            onNavigateUp()
        } else {
            viewModel.navigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val title = when (val state = navState) {
                is MinistryNavState.Home -> "Ministry Resources"
                is MinistryNavState.CategoryList -> state.category.label
                is MinistryNavState.ResourceDetail -> "Resource Details"
            }
            MinistryTopBar(
                title = title,
                showBackButton = true,
                onBackClick = { 
                    if (navState is MinistryNavState.Home) {
                        onNavigateUp()
                    } else {
                        viewModel.navigateBack()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = navState) {
                is MinistryNavState.Home -> {
                    val categories = viewModel.getCategories()
                    MinistryCategoryListScreen(
                        categories = categories,
                        onCategoryClick = { viewModel.navigateToCategory(it) }
                    )
                }
                is MinistryNavState.CategoryList -> {
                    val resources = viewModel.getResourcesForCategory(state.category)
                    MinistryResourceListScreen(
                        resources = resources,
                        onResourceClick = { viewModel.navigateToResourceDetail(it) }
                    )
                }
                is MinistryNavState.ResourceDetail -> {
                    MinistryResourceDetailScreen(resource = state.resource)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinistryTopBar(title: String, showBackButton: Boolean, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                TextButton(onClick = onBackClick) {
                    Text("< Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun MinistryCategoryListScreen(categories: List<MinistryCategory>, onCategoryClick: (MinistryCategory) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick(category) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MinistryResourceListScreen(resources: List<MinistryResource>, onResourceClick: (MinistryResource) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(resources) { resource ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResourceClick(resource) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = resource.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MinistryResourceDetailScreen(resource: MinistryResource) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = resource.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Category: ${resource.category.label}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = resource.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Content", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = resource.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
