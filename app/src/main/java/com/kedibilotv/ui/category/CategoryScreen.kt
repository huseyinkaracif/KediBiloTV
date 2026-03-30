package com.kedibilotv.ui.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.ui.common.EmptyState
import com.kedibilotv.ui.common.ErrorState
import com.kedibilotv.ui.common.LoadingIndicator
import com.kedibilotv.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onNavigateToContent: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val title = when (state.contentType) {
        ContentType.LIVE -> "Canlı TV"
        ContentType.VOD -> "Filmler"
        ContentType.SERIES -> "Diziler"
    }

    LaunchedEffect(searchVisible) {
        if (searchVisible) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(visible = !searchVisible, enter = fadeIn(), exit = fadeOut()) {
                        Text(title, style = MaterialTheme.typography.headlineSmall, color = NeonTextPrimary)
                    }
                    AnimatedVisibility(visible = searchVisible, enter = fadeIn(), exit = fadeOut()) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = viewModel::search,
                            placeholder = { Text("Kategori ara...", color = NeonTextMuted) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = NeonSurfaceHigh,
                                unfocusedContainerColor = NeonSurfaceHigh,
                                focusedTextColor = NeonTextPrimary,
                                unfocusedTextColor = NeonTextPrimary,
                                cursorColor = NeonCyan,
                                focusedIndicatorColor = NeonCyan,
                                unfocusedIndicatorColor = NeonSurfaceRim
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Geri", tint = NeonTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) viewModel.search("")
                    }) {
                        Icon(
                            if (searchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchVisible) "Aramayı Kapat" else "Ara",
                            tint = if (searchVisible) NeonCoral else NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeonBackground)
            )
        },
        containerColor = NeonBackground
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            state.filteredCategories.isEmpty() -> EmptyState(
                if (state.searchQuery.isNotBlank()) "\"${state.searchQuery}\" için sonuç yok"
                else "Kategori bulunamadı"
            )
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredCategories) { category ->
                        CategoryCard(
                            name = category.name,
                            onClick = { onNavigateToContent(state.contentType.name, category.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NeonSurface)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(NeonCoral.copy(alpha = 0.4f), NeonSurfaceRim.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = NeonTextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
