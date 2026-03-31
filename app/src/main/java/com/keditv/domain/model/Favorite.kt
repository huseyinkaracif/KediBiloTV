package com.keditv.domain.model

data class Favorite(
    val streamId: Int,
    val type: ContentType,
    val name: String,
    val posterUrl: String?,
    val categoryName: String?,
    val addedAt: Long = System.currentTimeMillis()
)
