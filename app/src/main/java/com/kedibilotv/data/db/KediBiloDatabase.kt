package com.kedibilotv.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kedibilotv.data.db.dao.FavoriteDao
import com.kedibilotv.data.db.dao.ServerConfigDao
import com.kedibilotv.data.db.dao.WatchHistoryDao
import com.kedibilotv.data.db.entity.FavoriteEntity
import com.kedibilotv.data.db.entity.ServerConfigEntity
import com.kedibilotv.data.db.entity.WatchHistoryEntity

@Database(
    entities = [ServerConfigEntity::class, FavoriteEntity::class, WatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KediBiloDatabase : RoomDatabase() {
    abstract fun serverConfigDao(): ServerConfigDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
}
