package com.kedibilotv.domain.repository

import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<Favorite>>
    suspend fun isFavorite(streamId: Int, type: ContentType): Boolean
    suspend fun addFavorite(favorite: Favorite)
    suspend fun removeFavorite(streamId: Int, type: ContentType)
}
