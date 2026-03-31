package com.keditv.di

import android.content.Context
import androidx.room.Room
import com.keditv.data.api.XtreamApiService
import com.keditv.data.db.KediDatabase
import com.keditv.data.db.dao.FavoriteDao
import com.keditv.data.db.dao.ServerConfigDao
import com.keditv.data.db.dao.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        engine {
            config {
                connectTimeout(10_000, TimeUnit.MILLISECONDS)
                readTimeout(15_000, TimeUnit.MILLISECONDS)
            }
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KediDatabase =
        Room.databaseBuilder(context, KediDatabase::class.java, "keditv.db").build()

    @Provides fun provideServerConfigDao(db: KediDatabase): ServerConfigDao = db.serverConfigDao()
    @Provides fun provideFavoriteDao(db: KediDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideWatchHistoryDao(db: KediDatabase): WatchHistoryDao = db.watchHistoryDao()
}
