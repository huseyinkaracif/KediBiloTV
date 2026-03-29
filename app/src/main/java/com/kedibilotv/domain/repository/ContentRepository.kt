package com.kedibilotv.domain.repository

import com.kedibilotv.domain.model.Category
import com.kedibilotv.domain.model.ContentItem
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.model.SeriesInfo

interface ContentRepository {
    suspend fun getCategories(type: ContentType): Result<List<Category>>
    suspend fun getContentList(type: ContentType, categoryId: String): Result<List<ContentItem>>
    /** Returns full VOD list without category filter — used for home screen featured banner. */
    suspend fun getAllVod(): Result<List<ContentItem>>
    suspend fun getSeriesInfo(seriesId: Int): Result<SeriesInfo>
    fun buildStreamUrl(type: ContentType, streamId: Int): String
    fun buildEpisodeUrl(episodeId: Int): String
}
