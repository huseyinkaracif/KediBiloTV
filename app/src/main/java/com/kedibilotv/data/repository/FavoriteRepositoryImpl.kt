package com.kedibilotv.data.repository

import com.kedibilotv.data.db.dao.FavoriteDao
import com.kedibilotv.data.db.entity.FavoriteEntity
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.model.Favorite
import com.kedibilotv.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<Favorite>> =
        dao.getAll().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun isFavorite(streamId: Int, type: ContentType): Boolean =
        dao.isFavorite(streamId, type.name)

    override suspend fun addFavorite(favorite: Favorite) {
        dao.insert(FavoriteEntity(
            streamId = favorite.streamId,
            type = favorite.type.name,
            name = favorite.name,
            posterUrl = favorite.posterUrl,
            categoryName = favorite.categoryName,
            addedAt = favorite.addedAt
        ))
    }

    override suspend fun removeFavorite(streamId: Int, type: ContentType) {
        dao.delete(streamId, type.name)
    }

    private fun FavoriteEntity.toDomain() = Favorite(
        streamId = streamId,
        type = ContentType.valueOf(type),
        name = name,
        posterUrl = posterUrl,
        categoryName = categoryName,
        addedAt = addedAt
    )
}
