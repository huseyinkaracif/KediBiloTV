package com.keditv.domain.usecase

import com.keditv.domain.model.ContentType
import com.keditv.domain.model.Favorite
import com.keditv.domain.repository.FavoriteRepository
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
