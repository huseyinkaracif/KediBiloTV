package com.keditv.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keditv.domain.model.ContentType
import com.keditv.domain.repository.ContentRepository
import com.keditv.domain.repository.WatchHistoryRepository
import com.keditv.domain.usecase.SaveWatchProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val streamUrl: String = "",
    val startPositionMs: Long = 0,
    val contentType: ContentType = ContentType.LIVE,
    val streamId: Int = 0,
    val episodeId: Int? = null,
    val contentName: String = "",
    val contentPosterUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val saveProgressUseCase: SaveWatchProgressUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val streamId = savedStateHandle.get<Int>("streamId")!!
    private val episodeId = savedStateHandle.get<Int>("episodeId")?.takeIf { it != -1 }

    private val _state = MutableStateFlow(PlayerUiState(contentType = type, streamId = streamId, episodeId = episodeId))
    val state: StateFlow<PlayerUiState> = _state

    init { loadStreamUrl() }

    private fun loadStreamUrl() {
        viewModelScope.launch {
            val url = if (episodeId != null) {
                contentRepository.buildEpisodeUrl(episodeId)
            } else {
                contentRepository.buildStreamUrl(type, streamId)
            }

            val history = watchHistoryRepository.getProgress(streamId, episodeId)
            val info = contentRepository.getStreamInfo(type, streamId)
            _state.value = _state.value.copy(
                streamUrl = url,
                startPositionMs = history?.positionMs ?: 0,
                contentName = history?.name?.takeIf { it.isNotBlank() } ?: info?.name ?: "",
                contentPosterUrl = history?.posterUrl ?: info?.posterUrl
            )
        }
    }

    fun saveProgress(positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            saveProgressUseCase(
                streamId = streamId,
                type = type,
                name = _state.value.contentName,
                posterUrl = _state.value.contentPosterUrl,
                positionMs = positionMs,
                durationMs = durationMs,
                episodeId = episodeId
            )
        }
    }
}
