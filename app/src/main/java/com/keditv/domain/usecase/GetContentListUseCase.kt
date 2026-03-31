package com.keditv.domain.usecase

import com.keditv.domain.model.ContentItem
import com.keditv.domain.model.ContentType
import com.keditv.domain.repository.ContentRepository
import javax.inject.Inject

class GetContentListUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType, categoryId: String): Result<List<ContentItem>> =
        repository.getContentList(type, categoryId)
}
