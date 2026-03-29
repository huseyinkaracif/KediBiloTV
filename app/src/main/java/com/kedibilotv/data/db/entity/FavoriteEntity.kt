package com.kedibilotv.data.db.entity

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["streamId", "type"])
data class FavoriteEntity(
    val streamId: Int,
    val type: String,
    val name: String,
    val posterUrl: String?,
    val categoryName: String?,
    val addedAt: Long = System.currentTimeMillis()
)
