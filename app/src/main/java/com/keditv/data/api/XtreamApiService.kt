package com.keditv.data.api

import com.keditv.data.api.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamApiService @Inject constructor(
    private val client: HttpClient
) {
    private var baseUrl: String = ""
    private var username: String = ""
    private var password: String = ""

    fun configure(serverUrl: String, user: String, pass: String) {
        var url = serverUrl.trim().trimEnd('/')
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "http://$url"
        }
        baseUrl = url
        username = user.trim()
        password = pass.trim()
        android.util.Log.d("KediTV", "configure → baseUrl=$baseUrl  user=$username")
    }

    private fun apiUrl(action: String? = null): String {
        val encodedUser = URLEncoder.encode(username, "UTF-8")
        val encodedPass = URLEncoder.encode(password, "UTF-8")
        // Eğer baseUrl zaten bir .php endpoint'i içeriyorsa (get.php gibi) direkt kullan,
        // aksi halde standart /player_api.php ekle
        val endpoint = if (baseUrl.contains(".php", ignoreCase = true)) baseUrl
                       else "$baseUrl/player_api.php"
        val base = "$endpoint?username=$encodedUser&password=$encodedPass"
        return if (action != null) "$base&action=$action" else base
    }

    var onUnauthorized: (() -> Unit)? = null

    suspend fun authenticateExactUrl(rawUrl: String) {
        android.util.Log.d("KediTV", "authenticateExactUrl → GET $rawUrl")
        val response = client.get(rawUrl)
        val status = response.status.value
        android.util.Log.d("KediTV", "authenticateExactUrl ← HTTP $status")
        if (status == 511 || status == 407) {
            throw Exception("Ağ erişimi engellendi (HTTP $status). VPN veya ağ ayarlarını kontrol et.")
        }
        if (status == 401 || status == 403) {
            throw Exception("Kullanıcı adı veya şifre hatalı (HTTP $status).")
        }
        if (status >= 400) {
            throw Exception("Sunucu hatası: HTTP $status")
        }
    }

    suspend fun authenticate(): AuthResponse {
        val url = apiUrl()
        android.util.Log.d("KediTV", "authenticate → GET $url")
        val response = client.get(url)
        val status = response.status.value
        val contentType = response.headers["Content-Type"] ?: ""
        android.util.Log.d("KediTV", "authenticate ← HTTP $status  Content-Type: $contentType")
        if (status == 511 || status == 407) {
            throw Exception("Ağ erişimi engellendi (HTTP $status). VPN veya ağ ayarlarını kontrol et.")
        }
        if (status == 401 || status == 403) {
            throw Exception("Kullanıcı adı veya şifre hatalı (HTTP $status).")
        }
        if (status >= 400) {
            throw Exception("Sunucu hatası: HTTP $status")
        }
        if (!contentType.contains("application/json", ignoreCase = true)) {
            throw Exception("Sunucu JSON döndürmedi ($contentType). URL'i kontrol et.")
        }
        return response.body()
    }

    suspend fun <T> safeGet(url: String, parse: suspend () -> T): T {
        val response = client.get(url)
        if (response.status.value == 403 || response.status.value == 401) {
            onUnauthorized?.invoke()
            throw Exception("Oturum suresi doldu. Lutfen tekrar giris yapin.")
        }
        return parse()
    }

    suspend fun getLiveCategories(): List<CategoryDto> =
        client.get(apiUrl("get_live_categories")).body()

    suspend fun getLiveStreams(): List<LiveStreamDto> =
        client.get(apiUrl("get_live_streams")).body()

    suspend fun getVodCategories(): List<CategoryDto> =
        client.get(apiUrl("get_vod_categories")).body()

    suspend fun getVodStreams(): List<VodStreamDto> =
        client.get(apiUrl("get_vod_streams")).body()

    suspend fun getSeriesCategories(): List<CategoryDto> =
        client.get(apiUrl("get_series_categories")).body()

    suspend fun getSeries(): List<SeriesDto> =
        client.get(apiUrl("get_series")).body()

    suspend fun getSeriesInfo(seriesId: Int): SeriesInfoDto =
        client.get(apiUrl("get_series_info") + "&series_id=$seriesId").body()

    fun buildStreamUrl(type: String, streamId: Int): String =
        "$baseUrl/$type/$username/$password/$streamId.ts"

    fun buildVodUrl(streamId: Int, ext: String = "mp4"): String =
        "$baseUrl/movie/$username/$password/$streamId.$ext"

    fun buildSeriesUrl(episodeId: Int, ext: String = "mp4"): String =
        "$baseUrl/series/$username/$password/$episodeId.$ext"
}
