package com.keditv.ui.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.keditv.R
import com.keditv.domain.model.ContentType
import com.keditv.ui.common.*
import com.keditv.ui.theme.NeonBackground
import com.keditv.ui.theme.NeonCyan
import com.keditv.ui.theme.NeonSurfaceHigh
import com.keditv.ui.theme.NeonSurfaceRim
import com.keditv.ui.theme.NeonTextMuted
import com.keditv.ui.theme.NeonTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentListScreen(
    onNavigateToDetail: (String, Int) -> Unit,
    onNavigateToPlayer: (String, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: ContentListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isTv = isTV()
    var searchVisible by remember { mutableStateOf(!isTv) }
    var searchDraft by remember(searchVisible) { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    fun commitSearch() {
        viewModel.search(searchDraft)
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchVisible) {
                        OutlinedTextField(
                            value = searchDraft,
                            onValueChange = { searchDraft = it; if (!isTv) viewModel.search(it) },
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NeonSurfaceHigh,
                                unfocusedContainerColor = NeonSurfaceHigh,
                                focusedTextColor = NeonTextPrimary,
                                unfocusedTextColor = NeonTextPrimary,
                                cursorColor = NeonCyan,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = NeonSurfaceRim,
                                focusedPlaceholderColor = NeonTextMuted,
                                unfocusedPlaceholderColor = NeonTextMuted
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { commitSearch() })
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isTv && searchVisible) {
                            searchVisible = false
                            viewModel.search("")
                            searchDraft = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            if (isTv && searchVisible) Icons.Default.Close else Icons.Default.ArrowBack,
                            stringResource(R.string.back),
                            tint = NeonTextPrimary
                        )
                    }
                },
                actions = {
                    if (isTv && searchVisible) {
                        TextButton(onClick = { commitSearch() }) {
                            Text(stringResource(R.string.search), color = NeonCyan, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    if (isTv && !searchVisible) {
                        IconButton(onClick = { searchVisible = true }) {
                            Icon(Icons.Default.Search, stringResource(R.string.search), tint = NeonCyan)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBackground)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            state.filteredItems.isEmpty() -> EmptyState(stringResource(R.string.no_content))
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(if (isTv) 160.dp else 140.dp),
                    modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredItems, key = { it.streamId }) { item ->
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
