package com.missionassist.app.assistance.ui

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
import com.missionassist.app.assistance.model.AssistanceCategory
import com.missionassist.app.assistance.model.AssistanceItem

@Composable
fun AssistanceScreen(
    modifier: Modifier = Modifier,
    initialCategory: AssistanceCategory? = null,
    viewModel: AssistanceViewModel = remember(initialCategory) { AssistanceViewModel(initialCategory) },
    onNavigateUp: () -> Unit,
    onUseInConversation: (AssistanceItem) -> Unit
) {
    val navState by viewModel.navState.collectAsState()
    
    val shouldExitOnBack = navState is AssistanceNavState.Home || 
        (navState is AssistanceNavState.CategoryList && viewModel.isDeepLinked)

    // Handle system back button
    BackHandler(enabled = true) {
        if (shouldExitOnBack) {
            onNavigateUp()
        } else {
            viewModel.navigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            val title = when (val state = navState) {
                is AssistanceNavState.Home -> "Assistance Categories"
                is AssistanceNavState.CategoryList -> state.category.label
                is AssistanceNavState.ItemDetail -> "Details"
            }
            AssistanceTopBar(
                title = title,
                showBackButton = true,
                onBackClick = { 
                    if (shouldExitOnBack) {
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
                is AssistanceNavState.Home -> {
                    val categories = viewModel.getCategories()
                    CategoryListScreen(
                        categories = categories,
                        onCategoryClick = { viewModel.navigateToCategory(it) }
                    )
                }
                is AssistanceNavState.CategoryList -> {
                    val itemsForCategory = viewModel.getItemsForCategory(state.category)
                    AssistanceItemListScreen(
                        items = itemsForCategory,
                        onItemClick = { viewModel.navigateToItemDetail(it) }
                    )
                }
                is AssistanceNavState.ItemDetail -> {
                    ItemDetailScreen(
                        item = state.item,
                        onUseInConversation = { onUseInConversation(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistanceTopBar(title: String, showBackButton: Boolean, onBackClick: () -> Unit) {
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
fun CategoryListScreen(categories: List<AssistanceCategory>, onCategoryClick: (AssistanceCategory) -> Unit) {
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
fun AssistanceItemListScreen(items: List<AssistanceItem>, onItemClick: (AssistanceItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.canonicalEnglishPhrase,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ItemDetailScreen(item: AssistanceItem, onUseInConversation: (AssistanceItem) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = item.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        DetailRow("Category", item.category.label)
        DetailRow("Guidance", item.guidance)
        DetailRow("English Phrase", item.canonicalEnglishPhrase)

        item.canonicalTamilPhrase?.let {
            DetailRow("Tamil Phrase", it)
        }

        item.criticality?.let {
            DetailRow("Criticality", it.label)
        }

        DetailRow("Review Status", item.bilingualReviewStatus)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onUseInConversation(item) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Use in Conversation", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}
