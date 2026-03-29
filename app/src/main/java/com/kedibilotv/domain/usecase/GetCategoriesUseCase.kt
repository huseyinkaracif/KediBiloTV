package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.Category
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.repository.ContentRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType): Result<List<Category>> =
        repository.getCategories(type)
}
