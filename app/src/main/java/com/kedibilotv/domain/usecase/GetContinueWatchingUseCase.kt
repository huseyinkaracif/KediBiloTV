package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.WatchHistory
import com.kedibilotv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContinueWatchingUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    operator fun invoke(): Flow<List<WatchHistory>> = repository.getContinueWatching()
}
