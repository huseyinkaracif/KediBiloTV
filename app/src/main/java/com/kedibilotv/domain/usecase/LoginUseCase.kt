package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.ServerConfig
import com.kedibilotv.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String
    ): Result<ServerConfig> = repository.login(serverUrl, username, password)
}
