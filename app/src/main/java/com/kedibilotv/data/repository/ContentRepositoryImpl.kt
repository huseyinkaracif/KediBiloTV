package com.kedibilotv.data.repository

import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.domain.model.*
import com.kedibilotv.domain.repository.ContentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XtreamApiService
) : ContentRepository {

    private val categoryCache = mutableMapOf<ContentType, List<Category>>()
    private val contentCache = mutableMapOf<String, List<ContentItem>>()

    override suspend fun getCategories(type: ContentType): Result<List<Category>> {
        categoryCache[type]?.let { return Result.success(it) }

        return try {
            val dtos = when (type) {
                ContentType.LIVE -> api.getLiveCategories()
                ContentType.VOD -> api.getVodCategories()
                ContentType.SERIES -> api.getSeriesCategories()
            }
            val categories = dtos.map { Category(it.categoryId, it.categoryName, type) }
            categoryCache[type] = categories
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContentList(type: ContentType, categoryId: String): Result<List<ContentItem>> {
        val key = "${type.name}_$categoryId"
        contentCache[key]?.let { return Result.success(it) }

        return try {
            val items = when (type) {
                ContentType.LIVE -> api.getLiveStreams()
                    .filter { it.categoryId == categoryId }
                    .map { ContentItem(it.streamId, it.name, type, it.categoryId, it.streamIcon, it.rating) }
                ContentType.VOD -> api.getVodStreams()
                    .filter { it.categoryId == categoryId }
                    .map { ContentItem(it.streamId, it.name, type, it.categoryId, it.streamIcon, it.rating) }
                ContentType.SERIES -> api.getSeries()
                    .filter { it.categoryId == categoryId }
                    .map { ContentItem(it.seriesId, it.name, type, it.categoryId, it.cover, it.rating) }
            }
            contentCache[key] = items
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllVod(): Result<List<ContentItem>> {
        contentCache["ALL_VOD"]?.let { return Result.success(it) }
        return try {
            val items = api.getVodStreams()
                .map { ContentItem(it.streamId, it.name, ContentType.VOD, it.categoryId, it.streamIcon, it.rating) }
            contentCache["ALL_VOD"] = items
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSeriesInfo(seriesId: Int): Result<SeriesInfo> {
        return try {
            val dto = api.getSeriesInfo(seriesId)
            val seasons = dto.episodes.map { (seasonNum, episodes) ->
                Season(
                    seasonNumber = seasonNum.toIntOrNull() ?: 0,
                    name = "Sezon $seasonNum",
                    episodes = episodes.map { ep ->
                        Episode(
                            id = ep.id.toIntOrNull() ?: 0,
                            episodeNumber = ep.episodeNum,
                            title = ep.title,
                            posterUrl = ep.info?.movieImage,
                            plot = ep.info?.plot,
                            duration = ep.info?.duration
                        )
                    }
                )
            }.sortedBy { it.seasonNumber }

            Result.success(SeriesInfo(
                seriesId = seriesId,
                name = dto.info?.name ?: "",
                posterUrl = dto.info?.cover,
                plot = dto.info?.plot,
                cast = dto.info?.cast,
                rating = dto.info?.rating,
                seasons = seasons
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun buildStreamUrl(type: ContentType, streamId: Int): String {
        return when (type) {
            ContentType.LIVE -> api.buildStreamUrl("live", streamId)
            ContentType.VOD -> api.buildVodUrl(streamId)
            ContentType.SERIES -> api.buildSeriesUrl(streamId)
        }
    }

    override fun buildEpisodeUrl(episodeId: Int): String = api.buildSeriesUrl(episodeId)

    fun clearCache() {
        categoryCache.clear()
        contentCache.clear()
    }
}
