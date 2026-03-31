package com.keditv.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.keditv.data.db.entity.ServerConfigEntity

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_config WHERE id = 1")
    suspend fun getConfig(): ServerConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ServerConfigEntity)

    @Query("DELETE FROM server_config")
    suspend fun clearConfig()
}
