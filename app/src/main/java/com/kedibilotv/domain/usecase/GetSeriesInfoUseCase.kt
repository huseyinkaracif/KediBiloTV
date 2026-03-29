package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.SeriesInfo
import com.kedibilotv.domain.repository.ContentRepository
import javax.inject.Inject

class GetSeriesInfoUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(seriesId: Int): Result<SeriesInfo> =
        repository.getSeriesInfo(seriesId)
}
