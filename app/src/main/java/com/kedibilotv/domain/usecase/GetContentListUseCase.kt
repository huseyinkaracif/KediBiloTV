package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.ContentItem
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.repository.ContentRepository
import javax.inject.Inject

class GetContentListUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType, categoryId: String): Result<List<ContentItem>> =
        repository.getContentList(type, categoryId)
}
