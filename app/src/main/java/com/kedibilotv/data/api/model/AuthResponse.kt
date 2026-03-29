package com.kedibilotv.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("user_info") val userInfo: UserInfo,
    @SerialName("server_info") val serverInfo: ServerInfo
)

@Serializable
data class UserInfo(
    val username: String,
    val password: String,
    val status: String,
    @SerialName("exp_date") val expDate: String? = null,
    @SerialName("auth") val auth: Int
)

@Serializable
data class ServerInfo(
    val url: String,
    val port: String,
    @SerialName("https_port") val httpsPort: String? = null,
    @SerialName("server_protocol") val serverProtocol: String? = null
)
