package com.keditv.domain.repository

import com.keditv.domain.model.ContentType
import com.keditv.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<Favorite>>
    suspend fun isFavorite(streamId: Int, type: ContentType): Boolean
    suspend fun addFavorite(favorite: Favorite)
    suspend fun removeFavorite(streamId: Int, type: ContentType)
}
