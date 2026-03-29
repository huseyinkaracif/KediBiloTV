package com.kedibilotv.domain.repository

import com.kedibilotv.domain.model.ServerConfig

interface AuthRepository {
    suspend fun login(serverUrl: String, username: String, password: String): Result<ServerConfig>
    suspend fun getSavedConfig(): ServerConfig?
    suspend fun logout()
}
