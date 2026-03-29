package com.kedibilotv.domain.repository

import com.kedibilotv.domain.model.WatchHistory
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun getContinueWatching(): Flow<List<WatchHistory>>
    suspend fun saveProgress(history: WatchHistory)
    suspend fun getProgress(streamId: Int, episodeId: Int? = null): WatchHistory?
}
