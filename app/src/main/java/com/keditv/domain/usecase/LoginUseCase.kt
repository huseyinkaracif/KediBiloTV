package com.keditv.domain.usecase

import com.keditv.domain.model.ServerConfig
import com.keditv.domain.repository.AuthRepository
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
