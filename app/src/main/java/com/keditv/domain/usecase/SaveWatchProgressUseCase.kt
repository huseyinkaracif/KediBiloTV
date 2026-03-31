package com.keditv.domain.usecase

import com.keditv.domain.model.ContentType
import com.keditv.domain.model.WatchHistory
import com.keditv.domain.repository.WatchHistoryRepository
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
