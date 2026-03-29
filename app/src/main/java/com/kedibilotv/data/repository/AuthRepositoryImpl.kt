package com.kedibilotv.data.repository

import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.data.db.dao.ServerConfigDao
import com.kedibilotv.data.db.entity.ServerConfigEntity
import com.kedibilotv.domain.model.ServerConfig
import com.kedibilotv.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: XtreamApiService,
    private val dao: ServerConfigDao
) : AuthRepository {

    override suspend fun login(serverUrl: String, username: String, password: String): Result<ServerConfig> {
        return try {
            api.configure(serverUrl, username, password)
            val response = api.authenticate()

            if (response.userInfo.auth != 1) {
                return Result.failure(Exception("Hesap aktif degil"))
            }

            val expDate = response.userInfo.expDate?.toLongOrNull()
            if (expDate != null && expDate < System.currentTimeMillis() / 1000) {
                return Result.failure(Exception("Hesabinizin suresi dolmus"))
            }

            val config = ServerConfig(serverUrl, username, password, expDate)
            dao.saveConfig(ServerConfigEntity(
                serverUrl = serverUrl,
                username = username,
                password = password,
                expDate = expDate
            ))

            Result.success(config)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(Exception("Sunucuya baglanamadi: ${e.message}"))
        }
    }

    override suspend fun getSavedConfig(): ServerConfig? {
        return dao.getConfig()?.let {
            api.configure(it.serverUrl, it.username, it.password)
            ServerConfig(it.serverUrl, it.username, it.password, it.expDate)
        }
    }

    override suspend fun logout() {
        dao.clearConfig()
    }
}
