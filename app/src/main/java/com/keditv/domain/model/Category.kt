package com.keditv.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: ContentType,
    val imageUrl: String? = null
)
