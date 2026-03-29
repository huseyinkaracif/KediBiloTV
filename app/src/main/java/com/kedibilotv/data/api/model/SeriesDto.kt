package com.kedibilotv.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    @SerialName("series_id") val seriesId: Int? = null,
    @SerialName("name") val name: String = "",
    @SerialName("cover") val cover: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("plot") val plot: String? = null
)
