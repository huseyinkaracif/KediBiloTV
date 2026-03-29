package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.model.Favorite
import com.kedibilotv.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(
        streamId: Int,
        type: ContentType,
        name: String,
        posterUrl: String?,
        categoryName: String?
    ): Boolean {
        val isFav = repository.isFavorite(streamId, type)
        if (isFav) {
            repository.removeFavorite(streamId, type)
        } else {
            repository.addFavorite(Favorite(streamId, type, name, posterUrl, categoryName))
        }
        return !isFav
    }
}
