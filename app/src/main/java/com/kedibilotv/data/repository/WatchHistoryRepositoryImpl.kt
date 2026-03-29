package com.kedibilotv.data.repository

import com.kedibilotv.data.db.dao.WatchHistoryDao
import com.kedibilotv.data.db.entity.WatchHistoryEntity
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.model.WatchHistory
import com.kedibilotv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val dao: WatchHistoryDao
) : WatchHistoryRepository {

    override fun getContinueWatching(): Flow<List<WatchHistory>> =
        dao.getContinueWatching().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun saveProgress(history: WatchHistory) {
        dao.upsert(WatchHistoryEntity(
            streamId = history.streamId,
            type = history.type.name,
            name = history.name,
            posterUrl = history.posterUrl,
            positionMs = history.positionMs,
            durationMs = history.durationMs,
            lastWatched = history.lastWatched,
            episodeId = history.episodeId ?: -1,
            episodeTitle = history.episodeTitle
        ))
    }

    override suspend fun getProgress(streamId: Int, episodeId: Int?): WatchHistory? {
        return dao.getProgress(streamId, episodeId ?: 0)?.toDomain()
    }

    private fun WatchHistoryEntity.toDomain() = WatchHistory(
        streamId = streamId,
        type = ContentType.valueOf(type),
        name = name,
        posterUrl = posterUrl,
        positionMs = positionMs,
        durationMs = durationMs,
        lastWatched = lastWatched,
        episodeId = if (episodeId == -1) null else episodeId,
        episodeTitle = episodeTitle
    )
}
