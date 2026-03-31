package com.keditv.domain.model

data class Season(
    val seasonNumber: Int,
    val name: String,
    val episodes: List<Episode>
)
