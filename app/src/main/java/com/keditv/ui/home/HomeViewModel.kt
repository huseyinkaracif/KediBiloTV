package com.keditv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keditv.domain.model.*
import com.keditv.domain.repository.ContentRepository
import com.keditv.domain.repository.FavoriteRepository
import com.keditv.domain.usecase.GetContinueWatchingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val featuredItems: List<ContentItem> = emptyList(),
    val currentFeaturedIndex: Int = 0,
    val continueWatching: List<WatchHistory> = emptyList(),
    val favorites: List<Favorite> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val getContinueWatching: GetContinueWatchingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        loadHome()
        observeContinueWatching()
        observeFavorites()
        rotateFeaturedBanner()
    }

    private fun loadHome() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            contentRepository.getAllVod().fold(
                onSuccess = { items ->
                    val featured = items.shuffled().take(5)
                    _state.value = _state.value.copy(isLoading = false, featuredItems = featured, error = null)
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    private fun rotateFeaturedBanner() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5_000)
                val items = _state.value.featuredItems
                if (items.size > 1) {
                    val next = (_state.value.currentFeaturedIndex + 1) % items.size
                    _state.value = _state.value.copy(currentFeaturedIndex = next)
                }
            }
        }
    }

    private fun observeContinueWatching() {
        getContinueWatching().onEach { list ->
            _state.value = _state.value.copy(continueWatching = list)
        }.launchIn(viewModelScope)
    }

    private fun observeFavorites() {
        favoriteRepository.getAllFavorites().onEach { list ->
            _state.value = _state.value.copy(favorites = list)
        }.launchIn(viewModelScope)
    }

    fun retry() = loadHome()
}
