package com.keditv.domain.model

data class ServerConfig(
    val serverUrl: String,
    val username: String,
    val password: String,
    val expDate: Long? = null
)
