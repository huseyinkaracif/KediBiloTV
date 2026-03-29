package com.kedibilotv.data.api

import com.kedibilotv.data.api.model.*
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

    suspend fun authenticate(): AuthResponse =
        client.get(apiUrl()).body()

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

    fun buildVodUrl(streamId: Int): String =
        "$baseUrl/movie/$username/$password/$streamId.mp4"

    fun buildSeriesUrl(episodeId: Int): String =
        "$baseUrl/series/$username/$password/$episodeId.mp4"
}
