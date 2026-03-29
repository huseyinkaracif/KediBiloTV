package com.kedibilotv.domain.model

data class WatchHistory(
    val streamId: Int,
    val type: ContentType,
    val name: String,
    val posterUrl: String?,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatched: Long,
    val episodeId: Int? = null,
    val episodeTitle: String? = null
)
