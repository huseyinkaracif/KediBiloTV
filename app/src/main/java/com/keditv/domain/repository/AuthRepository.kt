package com.keditv.domain.repository

import com.keditv.domain.model.ServerConfig

interface AuthRepository {
    suspend fun login(serverUrl: String, username: String, password: String): Result<ServerConfig>
    suspend fun loginM3u(rawUrl: String): Result<ServerConfig>
    suspend fun getSavedConfig(): ServerConfig?
    suspend fun logout()
}
