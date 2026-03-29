package com.kedibilotv.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesInfoDto(
    @SerialName("info") val info: SeriesDetailDto? = null,
    @SerialName("episodes") val episodes: Map<String, List<EpisodeDto>> = emptyMap()
)

@Serializable
data class SeriesDetailDto(
    @SerialName("name") val name: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("cast") val cast: String? = null,
    @SerialName("rating") val rating: String? = null
)

@Serializable
data class EpisodeDto(
    @SerialName("id") val id: String,
    @SerialName("episode_num") val episodeNum: Int,
    @SerialName("title") val title: String,
    @SerialName("info") val info: EpisodeInfoDto? = null
)

@Serializable
data class EpisodeInfoDto(
    @SerialName("movie_image") val movieImage: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("duration") val duration: String? = null
)
