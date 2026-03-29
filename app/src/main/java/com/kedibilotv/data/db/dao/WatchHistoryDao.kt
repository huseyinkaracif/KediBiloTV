package com.kedibilotv.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kedibilotv.data.db.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE positionMs > 0 ORDER BY lastWatched DESC LIMIT 20")
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId AND episodeId = :episodeId")
    suspend fun getProgress(streamId: Int, episodeId: Int = -1): WatchHistoryEntity?
}
