package com.keditv.domain.usecase

import com.keditv.domain.model.WatchHistory
import com.keditv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContinueWatchingUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    operator fun invoke(): Flow<List<WatchHistory>> = repository.getContinueWatching()
}
