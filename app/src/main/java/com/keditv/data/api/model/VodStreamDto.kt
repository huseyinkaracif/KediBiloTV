package com.keditv.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VodStreamDto(
    @SerialName("stream_id") val streamId: Int? = null,
    @SerialName("name") val name: String = "",
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("container_extension") val containerExtension: String = "mp4"
)
