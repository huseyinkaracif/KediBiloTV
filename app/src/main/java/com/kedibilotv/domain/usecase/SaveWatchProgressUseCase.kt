package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.model.WatchHistory
import com.kedibilotv.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class SaveWatchProgressUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(
        streamId: Int,
        type: ContentType,
        name: String,
        posterUrl: String?,
        positionMs: Long,
        durationMs: Long,
        episodeId: Int? = null,
        episodeTitle: String? = null
    ) {
        repository.saveProgress(
            WatchHistory(
                streamId = streamId,
                type = type,
                name = name,
                posterUrl = posterUrl,
                positionMs = positionMs,
                durationMs = durationMs,
                lastWatched = System.currentTimeMillis(),
                episodeId = episodeId,
                episodeTitle = episodeTitle
            )
        )
    }
}
