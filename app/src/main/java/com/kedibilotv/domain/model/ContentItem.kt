package com.kedibilotv.domain.model

data class ContentItem(
    val streamId: Int,
    val name: String,
    val type: ContentType,
    val categoryId: String,
    val posterUrl: String?,
    val rating: String?,
    val streamUrl: String? = null
)
