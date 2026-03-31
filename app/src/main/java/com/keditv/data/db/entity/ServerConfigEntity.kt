package com.keditv.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_config")
data class ServerConfigEntity(
    @PrimaryKey val id: Int = 1,
    val serverUrl: String,
    val username: String,
    val password: String,
    val expDate: Long? = null,
    val lastLogin: Long = System.currentTimeMillis()
)
