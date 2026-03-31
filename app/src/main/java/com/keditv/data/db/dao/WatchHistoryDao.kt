package com.keditv.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keditv.data.db.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("""
        SELECT * FROM watch_history
        WHERE positionMs > 0
          AND lastWatched = (
              SELECT MAX(w2.lastWatched) FROM watch_history AS w2
              WHERE w2.streamId = watch_history.streamId
                AND w2.type = watch_history.type
                AND w2.positionMs > 0
          )
        ORDER BY lastWatched DESC
        LIMIT 20
    """)
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId AND episodeId = :episodeId")
    suspend fun getProgress(streamId: Int, episodeId: Int = -1): WatchHistoryEntity?

    @Query("DELETE FROM watch_history WHERE streamId = :streamId AND type = :type AND episodeId = :episodeId")
    suspend fun delete(streamId: Int, type: String, episodeId: Int = -1)
}
