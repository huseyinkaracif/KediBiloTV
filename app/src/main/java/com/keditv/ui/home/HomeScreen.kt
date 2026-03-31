package com.keditv.ui.home

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.keditv.ui.common.ErrorState
import com.keditv.ui.common.LoadingIndicator
import com.keditv.ui.common.isTV

@Composable
fun HomeScreen(
    onNavigateToCategory: (String) -> Unit,
    onNavigateToDetail: (String, Int) -> Unit,
    onNavigateToPlayer: (String, Int, Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingIndicator()
        state.error != null -> ErrorState(message = state.error!!, onRetry = viewModel::retry)
        else -> {
            if (isTV()) {
                HomeTvContent(
                    state = state,
                    onCategoryClick = onNavigateToCategory,
                    onItemClick = onNavigateToDetail,
                    onContinueClick = onNavigateToPlayer,
                    onSettingsClick = onNavigateToSettings
                )
            } else {
                HomeMobileContent(
                    state = state,
                    onCategoryClick = onNavigateToCategory,
                    onItemClick = onNavigateToDetail,
                    onContinueClick = onNavigateToPlayer,
                    onDeleteFromHistory = viewModel::deleteFromHistory,
                    onSettingsClick = onNavigateToSettings
                )
            }
        }
    }
}
