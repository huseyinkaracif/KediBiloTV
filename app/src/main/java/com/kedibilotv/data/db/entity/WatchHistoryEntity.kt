package com.kedibilotv.data.db.entity

import androidx.room.Entity

// episodeId = -1 means "no episode" (VOD/LIVE). Never use 0 as sentinel
// since real episode IDs can be 0 per Xtream API.
@Entity(tableName = "watch_history", primaryKeys = ["streamId", "type", "episodeId"])
data class WatchHistoryEntity(
    val streamId: Int,
    val type: String,
    val name: String,
    val posterUrl: String?,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatched: Long,
    val episodeId: Int = -1,
    val episodeTitle: String? = null
)
