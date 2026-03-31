package com.keditv.domain.repository

import com.keditv.domain.model.Category
import com.keditv.domain.model.ContentItem
import com.keditv.domain.model.ContentType
import com.keditv.domain.model.SeriesInfo

interface ContentRepository {
    suspend fun getCategories(type: ContentType): Result<List<Category>>
    suspend fun getContentList(type: ContentType, categoryId: String): Result<List<ContentItem>>
    /** Returns full VOD list without category filter — used for home screen featured banner. */
    suspend fun getAllVod(): Result<List<ContentItem>>
    suspend fun getSeriesInfo(seriesId: Int): Result<SeriesInfo>
    fun buildStreamUrl(type: ContentType, streamId: Int): String
    fun buildEpisodeUrl(episodeId: Int): String
}
