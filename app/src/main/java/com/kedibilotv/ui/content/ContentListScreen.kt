package com.kedibilotv.ui.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kedibilotv.R
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentListScreen(
    onNavigateToDetail: (String, Int) -> Unit,
    onNavigateToPlayer: (String, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: ContentListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::search,
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            state.filteredItems.isEmpty() -> EmptyState(stringResource(R.string.no_content))
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredItems) { item ->
                        ContentCard(
                            name = item.name,
                            posterUrl = item.posterUrl,
                            onClick = {
                                if (item.type == ContentType.LIVE) {
                                    onNavigateToPlayer(item.type.name, item.streamId)
                                } else {
                                    onNavigateToDetail(item.type.name, item.streamId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
