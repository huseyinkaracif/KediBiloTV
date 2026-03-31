package com.keditv.domain.usecase

import com.keditv.domain.model.SeriesInfo
import com.keditv.domain.repository.ContentRepository
import javax.inject.Inject

class GetSeriesInfoUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(seriesId: Int): Result<SeriesInfo> =
        repository.getSeriesInfo(seriesId)
}
