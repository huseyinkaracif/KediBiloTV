package com.kedibilotv.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedibilotv.domain.model.*
import com.kedibilotv.domain.repository.ContentRepository
import com.kedibilotv.domain.repository.FavoriteRepository
import com.kedibilotv.domain.repository.WatchHistoryRepository
import com.kedibilotv.domain.usecase.GetSeriesInfoUseCase
import com.kedibilotv.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val contentType: ContentType = ContentType.VOD,
    val streamId: Int = 0,
    val name: String = "",
    val posterUrl: String? = null,
    val plot: String? = null,
    val rating: String? = null,
    val isFavorite: Boolean = false,
    val seriesInfo: SeriesInfo? = null,
    val selectedSeason: Int = 1,
    val watchHistory: WatchHistory? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val getSeriesInfoUseCase: GetSeriesInfoUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val streamId = savedStateHandle.get<Int>("streamId")!!
    private val initialName = savedStateHandle.get<String>("name") ?: ""
    private val initialPosterUrl = savedStateHandle.get<String>("posterUrl")?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(
        DetailUiState(contentType = type, streamId = streamId, name = initialName, posterUrl = initialPosterUrl)
    )
    val state: StateFlow<DetailUiState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val isFav = favoriteRepository.isFavorite(streamId, type)
            val history = watchHistoryRepository.getProgress(streamId)

            if (type == ContentType.SERIES) {
                getSeriesInfoUseCase(streamId).fold(
                    onSuccess = { info ->
                        _state.value = _state.value.copy(
                            isLoading = false, name = info.name, posterUrl = info.posterUrl,
                            plot = info.plot, rating = info.rating, seriesInfo = info,
                            isFavorite = isFav, watchHistory = history, error = null
                        )
                    },
                    onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false, isFavorite = isFav, watchHistory = history, error = null
                )
            }
        }
    }

    fun selectSeason(season: Int) {
        _state.value = _state.value.copy(selectedSeason = season)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val newState = toggleFavoriteUseCase(
                streamId, type, _state.value.name, _state.value.posterUrl, null
            )
            _state.value = _state.value.copy(isFavorite = newState)
        }
    }

    fun getStreamUrl(): String = contentRepository.buildStreamUrl(type, streamId)
    fun getEpisodeUrl(episodeId: Int): String = contentRepository.buildEpisodeUrl(episodeId)

    fun retry() = load()
}
