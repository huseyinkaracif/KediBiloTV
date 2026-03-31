package com.keditv.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.keditv.data.db.dao.FavoriteDao
import com.keditv.data.db.dao.ServerConfigDao
import com.keditv.data.db.dao.WatchHistoryDao
import com.keditv.data.db.entity.FavoriteEntity
import com.keditv.data.db.entity.ServerConfigEntity
import com.keditv.data.db.entity.WatchHistoryEntity

@Database(
    entities = [ServerConfigEntity::class, FavoriteEntity::class, WatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KediDatabase : RoomDatabase() {
    abstract fun serverConfigDao(): ServerConfigDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
