package com.kedibilotv.di

import android.content.Context
import androidx.room.Room
import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.data.db.KediBiloDatabase
import com.kedibilotv.data.db.dao.FavoriteDao
import com.kedibilotv.data.db.dao.ServerConfigDao
import com.kedibilotv.data.db.dao.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        engine {
            connectTimeout = 10_000
            socketTimeout = 15_000
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KediBiloDatabase =
        Room.databaseBuilder(context, KediBiloDatabase::class.java, "kedibilotv.db").build()

    @Provides fun provideServerConfigDao(db: KediBiloDatabase): ServerConfigDao = db.serverConfigDao()
    @Provides fun provideFavoriteDao(db: KediBiloDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideWatchHistoryDao(db: KediBiloDatabase): WatchHistoryDao = db.watchHistoryDao()
}
