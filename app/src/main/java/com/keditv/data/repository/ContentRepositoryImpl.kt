package com.keditv.data.repository

import com.keditv.data.api.XtreamApiService
import com.keditv.data.api.model.LiveStreamDto
import com.keditv.data.api.model.SeriesDto
import com.keditv.data.api.model.VodStreamDto
import com.keditv.domain.model.*
import com.keditv.domain.repository.ContentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XtreamApiService
) : ContentRepository {

    // Kategori cache — tür başına bir kez çekilir
    private val categoryCache = mutableMapOf<ContentType, List<Category>>()

    // Ham DTO cache — aynı tür için API yalnızca bir kez çağrılır
    // Bu sayede N kategori açmak N API isteği değil 1 API isteği yapar
    private var rawLiveStreams: List<LiveStreamDto>? = null
    private var rawVodStreams: List<VodStreamDto>? = null
    private var rawSeriesStreams: List<SeriesDto>? = null

    // Kategori filtreli sonuç cache — her kategori için bir kez map'lenir
    private val contentCache = mutableMapOf<String, List<ContentItem>>()

    // Dizi detay cache — her dizi için bir kez çekilir (ağır istek)
    private val seriesInfoCache = mutableMapOf<Int, SeriesInfo>()

    // Extension cache — streamId/episodeId başına container extension
    private val vodExtCache = mutableMapOf<Int, String>()      // streamId -> extension
    private val episodeExtCache = mutableMapOf<Int, String>()  // episodeId -> extension

    // ── Ham DTO erişimi: cache'e bak, yoksa API'den çek, cache'e yaz ──

    private suspend fun fetchLive(): List<LiveStreamDto> =
        rawLiveStreams ?: api.getLiveStreams().also { rawLiveStreams = it }

    private suspend fun fetchVod(): List<VodStreamDto> =
        rawVodStreams ?: api.getVodStreams().also { streams ->
            rawVodStreams = streams
            streams.forEach { it.streamId?.let { id -> vodExtCache[id] = it.containerExtension } }
        }

    private suspend fun fetchSeries(): List<SeriesDto> =
        rawSeriesStreams ?: api.getSeries().also { rawSeriesStreams = it }

    // ─────────────────────────────────────────────────────────────

    override suspend fun getCategories(type: ContentType): Result<List<Category>> {
        categoryCache[type]?.let { return Result.success(it) }
        return try {
            val dtos = when (type) {
                ContentType.LIVE -> api.getLiveCategories()
                ContentType.VOD -> api.getVodCategories()
                ContentType.SERIES -> api.getSeriesCategories()
            }
            val categories = dtos.map {
                Category(it.categoryId, it.categoryName, type, it.categoryImage?.takeIf { img -> img.startsWith("http") })
            }
            categoryCache[type] = categories
            Result.success(categories)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepo", "getCategories hata:", e)
            Result.failure(e)
        }
    }

    override suspend fun getContentList(type: ContentType, categoryId: String): Result<List<ContentItem>> {
        val key = "${type.name}_$categoryId"
        contentCache[key]?.let { return Result.success(it) }
        return try {
            val items = when (type) {
                ContentType.LIVE -> fetchLive()
                    .filter { it.streamId != null && it.categoryId == categoryId }
                    .map { ContentItem(it.streamId!!, it.name, type, it.categoryId ?: "", it.streamIcon, it.rating) }
                ContentType.VOD -> fetchVod()
                    .filter { it.streamId != null && it.categoryId == categoryId }
                    .map { ContentItem(it.streamId!!, it.name, type, it.categoryId ?: "", it.streamIcon, it.rating, it.plot) }
                ContentType.SERIES -> fetchSeries()
                    .filter { it.seriesId != null && it.categoryId == categoryId }
                    .map { ContentItem(it.seriesId!!, it.name, type, it.categoryId ?: "", it.cover, it.rating) }
            }
            contentCache[key] = items
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepo", "getContentList hata:", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllLive(): Result<List<ContentItem>> {
        contentCache["ALL_LIVE"]?.let { return Result.success(it) }
        return try {
            val items = fetchLive()
                .filter { it.streamId != null }
                .map { ContentItem(it.streamId!!, it.name, ContentType.LIVE, it.categoryId ?: "", it.streamIcon, it.rating) }
            contentCache["ALL_LIVE"] = items
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepo", "getAllLive hata:", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllVod(): Result<List<ContentItem>> {
        contentCache["ALL_VOD"]?.let { return Result.success(it) }
        return try {
            val items = fetchVod()
                .filter { it.streamId != null }
                .map { ContentItem(it.streamId!!, it.name, ContentType.VOD, it.categoryId ?: "", it.streamIcon, it.rating, it.plot) }
            contentCache["ALL_VOD"] = items
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepo", "getAllVod hata:", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllSeries(): Result<List<ContentItem>> {
        contentCache["ALL_SERIES"]?.let { return Result.success(it) }
        return try {
            val items = fetchSeries()
                .filter { it.seriesId != null }
                .map { ContentItem(it.seriesId!!, it.name, ContentType.SERIES, it.categoryId ?: "", it.cover, it.rating) }
            contentCache["ALL_SERIES"] = items
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepo", "getAllSeries hata:", e)
            Result.failure(e)
        }
    }

    override suspend fun getStreamInfo(type: ContentType, streamId: Int): ContentItem? {
        // Önce mevcut cache'lerden ara — API isteği yapmadan
        val allKey = when (type) {
            ContentType.LIVE -> "ALL_LIVE"
            ContentType.VOD -> "ALL_VOD"
            ContentType.SERIES -> "ALL_SERIES"
        }
        contentCache[allKey]?.find { it.streamId == streamId }?.let { return it }
        contentCache.entries
            .filter { it.key.startsWith("${type.name}_") }
            .flatMap { it.value }
            .find { it.streamId == streamId }
            ?.let { return it }

        // Cache'de yok — ham DTO listesinden ara (bu da cache'li)
        return try {
            when (type) {
                ContentType.LIVE -> fetchLive().find { it.streamId == streamId }
                    ?.let { ContentItem(it.streamId!!, it.name, type, it.categoryId ?: "", it.streamIcon, it.rating) }
                ContentType.VOD -> fetchVod().find { it.streamId == streamId }
                    ?.let { ContentItem(it.streamId!!, it.name, type, it.categoryId ?: "", it.streamIcon, it.rating, it.plot) }
                ContentType.SERIES -> fetchSeries().find { it.seriesId == streamId }
                    ?.let { ContentItem(it.seriesId!!, it.name, type, it.categoryId ?: "", it.cover, it.rating) }
            }
        } catch (e: Exception) { null }
    }

    override suspend fun getSeriesInfo(seriesId: Int): Result<SeriesInfo> {
        // Dizi detay cache — her açılışta network isteği yapmayı önler
        seriesInfoCache[seriesId]?.let { return Result.success(it) }
        return try {
            val dto = api.getSeriesInfo(seriesId)
            val seasons = dto.episodes.map { (seasonNum, episodes) ->
                Season(
                    seasonNumber = seasonNum.toIntOrNull() ?: 0,
                    name = "Sezon $seasonNum",
                    episodes = episodes.map { ep ->
                        val epId = ep.id.toIntOrNull() ?: 0
                        episodeExtCache[epId] = ep.containerExtension
                        Episode(
                            id = epId,
                            episodeNumber = ep.episodeNum,
                            title = ep.title,
                            posterUrl = ep.info?.movieImage,
                            plot = ep.info?.plot,
                            duration = ep.info?.duration
                        )
                    }
                )
            }.sortedBy { it.seasonNumber }

            val info = SeriesInfo(
                seriesId = seriesId,
                name = dto.info?.name ?: "",
                posterUrl = dto.info?.cover,
                plot = dto.info?.plot,
                cast = dto.info?.cast,
                rating = dto.info?.rating,
                seasons = seasons
            )
            seriesInfoCache[seriesId] = info
            Result.success(info)
        } catch (e: Exception) {
            android.util.Log.e("ContentRepo", "getSeriesInfo hata:", e)
            Result.failure(e)
        }
    }

    override fun buildStreamUrl(type: ContentType, streamId: Int): String = when (type) {
        ContentType.LIVE -> api.buildStreamUrl("live", streamId)
        ContentType.VOD -> api.buildVodUrl(streamId, vodExtCache[streamId] ?: "mp4")
        ContentType.SERIES -> api.buildSeriesUrl(streamId)
    }

    override fun buildEpisodeUrl(episodeId: Int): String =
        api.buildSeriesUrl(episodeId, episodeExtCache[episodeId] ?: "mp4")

    fun clearCache() {
        categoryCache.clear()
        contentCache.clear()
        rawLiveStreams = null
        rawVodStreams = null
        rawSeriesStreams = null
        seriesInfoCache.clear()
    }
}
