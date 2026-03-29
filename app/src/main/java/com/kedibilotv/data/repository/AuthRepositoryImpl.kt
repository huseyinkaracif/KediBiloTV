package com.kedibilotv.data.repository

import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.data.db.dao.ServerConfigDao
import com.kedibilotv.data.db.entity.ServerConfigEntity
import com.kedibilotv.domain.model.ServerConfig
import com.kedibilotv.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: XtreamApiService,
    private val dao: ServerConfigDao
) : AuthRepository {

    override suspend fun login(serverUrl: String, username: String, password: String): Result<ServerConfig> {
        return tryLogin(serverUrl, username, password)
    }

    private suspend fun tryLogin(
        serverUrl: String,
        username: String,
        password: String,
        originalUrl: String = serverUrl
    ): Result<ServerConfig> {
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

            val config = ServerConfig(originalUrl, username, password, expDate)
            dao.saveConfig(ServerConfigEntity(
                serverUrl = originalUrl,
                username = username,
                password = password,
                expDate = expDate
            ))

            Result.success(config)
        } catch (e: SSLException) {
            // Sunucu https:// yerine http:// konuşuyor, otomatik düşür
            // Ama kaydederken orijinal serverUrl'i sakla ki kullanıcıya http:// göstermeyelim
            if (serverUrl.startsWith("https://", ignoreCase = true)) {
                val httpUrl = "http://" + serverUrl.removePrefix("https://")
                tryLogin(httpUrl, username, password, originalUrl = serverUrl)
            } else {
                Result.failure(Exception("Sunucuya baglanamadi: ${e.message}"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("AuthRepositoryImpl", "Hata oluştu:", e)
            Result.failure(Exception("Sunucuya baglanamadi: ${e.message}"))
        }
    }

    override suspend fun loginM3u(rawUrl: String): Result<ServerConfig> {
        return loginM3uInternal(rawUrl.trim())
    }

    private suspend fun loginM3uInternal(rawUrl: String): Result<ServerConfig> {
        return try {
            api.authenticateExactUrl(rawUrl)

            val uri = android.net.Uri.parse(rawUrl)
            val username = uri.getQueryParameter("username")?.trim()
                ?: return Result.failure(Exception("URL'de username bulunamadı"))
            val password = uri.getQueryParameter("password")?.trim()
                ?: return Result.failure(Exception("URL'de password bulunamadı"))
            val serverBase = "${uri.scheme}://${uri.authority}"

            api.configure(serverBase, username, password)
            val config = ServerConfig(serverBase, username, password, null)
            dao.saveConfig(ServerConfigEntity(
                serverUrl = serverBase,
                username = username,
                password = password,
                expDate = null
            ))
            Result.success(config)
        } catch (e: SSLException) {
            if (rawUrl.startsWith("https://", ignoreCase = true)) {
                loginM3uInternal("http://" + rawUrl.removePrefix("https://"))
            } else {
                Result.failure(Exception("Sunucuya bağlanamadı: ${e.message}"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("AuthRepositoryImpl", "M3U login hatası:", e)
            Result.failure(Exception("Bağlanamadı: ${e.message}"))
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
