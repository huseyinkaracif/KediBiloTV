package com.keditv.domain.usecase

import com.keditv.domain.model.Category
import com.keditv.domain.model.ContentType
import com.keditv.domain.repository.ContentRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType): Result<List<Category>> =
        repository.getCategories(type)
}
