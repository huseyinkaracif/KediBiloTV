package com.kedibilotv.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VodStreamDto(
    @SerialName("stream_id") val streamId: Int,
    @SerialName("name") val name: String,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("rating") val rating: String? = null,
    @SerialName("plot") val plot: String? = null
)
