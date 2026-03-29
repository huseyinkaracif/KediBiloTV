package com.kedibilotv.domain.model

data class SeriesInfo(
    val seriesId: Int,
    val name: String,
    val posterUrl: String?,
    val plot: String?,
    val cast: String?,
    val rating: String?,
    val seasons: List<Season>
)
