package com.kedibilotv.domain.model

data class Episode(
    val id: Int,
    val episodeNumber: Int,
    val title: String,
    val posterUrl: String?,
    val plot: String?,
    val duration: String?,
    val streamUrl: String? = null
)
