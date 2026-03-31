# KediTV Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android IPTV app (TV + mobile) that connects to Xtream Codes API and plays live TV, movies, and series with a cat-themed Netflix-like UI.

**Architecture:** Single module, Clean Architecture via packages. Compose TV for TV UI, Compose Material for mobile. Shared ViewModels and data layer. Media3/ExoPlayer for playback. Room for local persistence (favorites, watch history, server config). Ktor for networking.

**Tech Stack:** Kotlin, minSdk 24, targetSdk 34, Jetpack Compose, Compose TV, Compose Navigation, Media3, Room, Ktor Client, Hilt, Coil, Coroutines + Flow

**Spec:** `docs/superpowers/specs/2026-03-29-KediTV-design.md`

---

## File Structure

```
app/
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/KediTV/
│   │   ├── KediApp.kt                          # Hilt Application class
│   │   ├── MainActivity.kt                          # Single Activity, NavHost
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   ├── XtreamApiService.kt              # Ktor-based API calls
│   │   │   │   └── model/                           # API response DTOs
│   │   │   │       ├── AuthResponse.kt
│   │   │   │       ├── CategoryDto.kt
│   │   │   │       ├── LiveStreamDto.kt
│   │   │   │       ├── VodStreamDto.kt
│   │   │   │       ├── SeriesDto.kt
│   │   │   │       └── SeriesInfoDto.kt
│   │   │   ├── db/
│   │   │   │   ├── KediDatabase.kt              # Room database
│   │   │   │   ├── entity/
│   │   │   │   │   ├── ServerConfigEntity.kt
│   │   │   │   │   ├── FavoriteEntity.kt
│   │   │   │   │   └── WatchHistoryEntity.kt
│   │   │   │   └── dao/
│   │   │   │       ├── ServerConfigDao.kt
│   │   │   │       ├── FavoriteDao.kt
│   │   │   │       └── WatchHistoryDao.kt
│   │   │   └── repository/
│   │   │       ├── AuthRepositoryImpl.kt
│   │   │       ├── ContentRepositoryImpl.kt
│   │   │       ├── FavoriteRepositoryImpl.kt
│   │   │       └── WatchHistoryRepositoryImpl.kt
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── ServerConfig.kt
│   │   │   │   ├── Category.kt
│   │   │   │   ├── ContentItem.kt                   # Unified: live/vod/series
│   │   │   │   ├── ContentType.kt                   # Enum: LIVE, VOD, SERIES
│   │   │   │   ├── SeriesInfo.kt
│   │   │   │   ├── Season.kt
│   │   │   │   ├── Episode.kt
│   │   │   │   ├── Favorite.kt
│   │   │   │   └── WatchHistory.kt
│   │   │   ├── repository/
│   │   │   │   ├── AuthRepository.kt
│   │   │   │   ├── ContentRepository.kt
│   │   │   │   ├── FavoriteRepository.kt
│   │   │   │   └── WatchHistoryRepository.kt
│   │   │   └── usecase/
│   │   │       ├── LoginUseCase.kt
│   │   │       ├── GetCategoriesUseCase.kt
│   │   │       ├── GetContentListUseCase.kt
│   │   │       ├── GetSeriesInfoUseCase.kt
│   │   │       ├── ToggleFavoriteUseCase.kt
│   │   │       ├── GetContinueWatchingUseCase.kt
│   │   │       └── SaveWatchProgressUseCase.kt
│   │   ├── ui/
│   │   │   ├── navigation/
│   │   │   │   ├── NavRoutes.kt                     # Route constants
│   │   │   │   └── KediNavHost.kt               # NavHost setup
│   │   │   ├── common/
│   │   │   │   ├── ContentCard.kt                   # Poster card (shared)
│   │   │   │   ├── EmptyState.kt                    # Cat emoji empty states
│   │   │   │   ├── ErrorState.kt                    # Error + retry
│   │   │   │   ├── LoadingIndicator.kt              # Cat paw loading
│   │   │   │   └── PlatformUtils.kt                 # isTV() helper
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Type.kt
│   │   │   │   └── Theme.kt                         # KediTheme
│   │   │   ├── login/
│   │   │   │   ├── LoginScreen.kt                   # Platform-branching composable
│   │   │   │   └── LoginViewModel.kt
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt                    # Platform-branching
│   │   │   │   ├── HomeMobileContent.kt
│   │   │   │   ├── HomeTvContent.kt
│   │   │   │   └── HomeViewModel.kt
│   │   │   ├── category/
│   │   │   │   ├── CategoryScreen.kt
│   │   │   │   └── CategoryViewModel.kt
│   │   │   ├── content/
│   │   │   │   ├── ContentListScreen.kt
│   │   │   │   └── ContentListViewModel.kt
│   │   │   ├── detail/
│   │   │   │   ├── DetailScreen.kt
│   │   │   │   └── DetailViewModel.kt
│   │   │   ├── player/
│   │   │   │   ├── PlayerScreen.kt
│   │   │   │   └── PlayerViewModel.kt
│   │   │   └── settings/
│   │   │       ├── SettingsScreen.kt
│   │   │       └── SettingsViewModel.kt
│   │   ├── player/
│   │   │   └── KediPlayer.kt                    # Media3 wrapper
│   │   ├── di/
│   │   │   ├── AppModule.kt                         # Ktor, Room providers
│   │   │   └── RepositoryModule.kt                  # Repository bindings
│   │   └── util/
│   │       └── Extensions.kt                        # Common extensions
│   └── res/
│       ├── values/strings.xml                       # Turkish strings
│       ├── values/colors.xml
│       ├── font/nunito_bold.ttf
│       ├── drawable/ic_launcher_foreground.xml       # Cat + TV vector
│       └── drawable/ic_cat_placeholder.xml           # Poster placeholder
├── src/test/java/com/KediTV/                    # Unit tests
│   ├── data/api/XtreamApiServiceTest.kt
│   ├── data/repository/AuthRepositoryImplTest.kt
│   ├── data/repository/ContentRepositoryImplTest.kt
│   ├── domain/usecase/LoginUseCaseTest.kt
│   └── domain/usecase/GetCategoriesUseCaseTest.kt
└── src/androidTest/java/com/KediTV/             # Instrumented tests
    └── data/db/DaoTest.kt
```

---

## Task 1: Project Scaffolding & Gradle Setup

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `app/build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/KediTV/KediApp.kt`
- Create: `app/src/main/java/com/KediTV/MainActivity.kt`
- Create: `app/proguard-rules.pro`

- [ ] **Step 1: Create root `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "KediTV"
include(":app")
```

- [ ] **Step 2: Create root `build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

- [ ] **Step 3: Create `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.KediTV"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.KediTV"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Compose TV
    implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
    implementation("androidx.tv:tv-material:1.0.0-alpha10")

    // Media3 / ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Ktor
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Leanback (TV launcher intent)
    implementation("androidx.leanback:leanback:1.0.0")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.7")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

- [ ] **Step 5: Create `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <uses-feature
        android:name="android.software.leanback"
        android:required="false" />

    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:name=".KediApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.KediTV">

        <!-- Mobile launcher -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize"
            android:theme="@style/Theme.KediTV">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <!-- TV launcher -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 6: Create `KediApp.kt`**

```kotlin
package com.KediTV

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KediApp : Application()
```

- [ ] **Step 7: Create stub `MainActivity.kt`**

```kotlin
package com.KediTV

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // NavHost will be added in Task 6
        }
    }
}
```

- [ ] **Step 8: Create `proguard-rules.pro`**

```proguard
# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.KediTV.**$$serializer { *; }
-keepclassmembers class com.KediTV.** { *** Companion; }
-keepclasseswithmembers class com.KediTV.** { kotlinx.serialization.KSerializer serializer(...); }
```

- [ ] **Step 9: Create Gradle wrapper and verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (or download + build if first run)

- [ ] **Step 10: Commit**

```bash
git init
git add .
git commit -m "feat: project scaffolding with all dependencies"
```

---

## Task 2: Domain Models & Repository Interfaces

**Files:**
- Create: `app/src/main/java/com/KediTV/domain/model/ContentType.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/ServerConfig.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/Category.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/ContentItem.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/SeriesInfo.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/Season.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/Episode.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/Favorite.kt`
- Create: `app/src/main/java/com/KediTV/domain/model/WatchHistory.kt`
- Create: `app/src/main/java/com/KediTV/domain/repository/AuthRepository.kt`
- Create: `app/src/main/java/com/KediTV/domain/repository/ContentRepository.kt`
- Create: `app/src/main/java/com/KediTV/domain/repository/FavoriteRepository.kt`
- Create: `app/src/main/java/com/KediTV/domain/repository/WatchHistoryRepository.kt`

- [ ] **Step 1: Create `ContentType.kt`**

```kotlin
package com.KediTV.domain.model

enum class ContentType(val apiPath: String) {
    LIVE("live"),
    VOD("movie"),
    SERIES("series")
}
```

- [ ] **Step 2: Create `ServerConfig.kt`**

```kotlin
package com.KediTV.domain.model

data class ServerConfig(
    val serverUrl: String,
    val username: String,
    val password: String,
    val expDate: Long? = null
)
```

- [ ] **Step 3: Create `Category.kt`**

```kotlin
package com.KediTV.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: ContentType
)
```

- [ ] **Step 4: Create `ContentItem.kt`**

```kotlin
package com.KediTV.domain.model

data class ContentItem(
    val streamId: Int,
    val name: String,
    val type: ContentType,
    val categoryId: String,
    val posterUrl: String?,
    val rating: String?,
    val streamUrl: String? = null
)
```

- [ ] **Step 5: Create series models — `SeriesInfo.kt`, `Season.kt`, `Episode.kt`**

```kotlin
// SeriesInfo.kt
package com.KediTV.domain.model

data class SeriesInfo(
    val seriesId: Int,
    val name: String,
    val posterUrl: String?,
    val plot: String?,
    val cast: String?,
    val rating: String?,
    val seasons: List<Season>
)

// Season.kt
package com.KediTV.domain.model

data class Season(
    val seasonNumber: Int,
    val name: String,
    val episodes: List<Episode>
)

// Episode.kt
package com.KediTV.domain.model

data class Episode(
    val id: Int,
    val episodeNumber: Int,
    val title: String,
    val posterUrl: String?,
    val plot: String?,
    val duration: String?,
    val streamUrl: String? = null
)
```

- [ ] **Step 6: Create `Favorite.kt` and `WatchHistory.kt`**

```kotlin
// Favorite.kt
package com.KediTV.domain.model

data class Favorite(
    val streamId: Int,
    val type: ContentType,
    val name: String,
    val posterUrl: String?,
    val categoryName: String?,
    val addedAt: Long = System.currentTimeMillis()
)

// WatchHistory.kt
package com.KediTV.domain.model

data class WatchHistory(
    val streamId: Int,
    val type: ContentType,
    val name: String,
    val posterUrl: String?,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatched: Long,
    val episodeId: Int? = null,
    val episodeTitle: String? = null
)
```

- [ ] **Step 7: Create repository interfaces**

```kotlin
// AuthRepository.kt
package com.KediTV.domain.repository

import com.KediTV.domain.model.ServerConfig

interface AuthRepository {
    suspend fun login(serverUrl: String, username: String, password: String): Result<ServerConfig>
    suspend fun getSavedConfig(): ServerConfig?
    suspend fun logout()
}

// ContentRepository.kt
package com.KediTV.domain.repository

import com.KediTV.domain.model.Category
import com.KediTV.domain.model.ContentItem
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.model.SeriesInfo

interface ContentRepository {
    suspend fun getCategories(type: ContentType): Result<List<Category>>
    suspend fun getContentList(type: ContentType, categoryId: String): Result<List<ContentItem>>
    /** Returns full VOD list without category filter — used for home screen featured banner. */
    suspend fun getAllVod(): Result<List<ContentItem>>
    suspend fun getSeriesInfo(seriesId: Int): Result<SeriesInfo>
    fun buildStreamUrl(type: ContentType, streamId: Int): String
    fun buildEpisodeUrl(episodeId: Int): String
}

// FavoriteRepository.kt
package com.KediTV.domain.repository

import com.KediTV.domain.model.ContentType
import com.KediTV.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<Favorite>>
    suspend fun isFavorite(streamId: Int, type: ContentType): Boolean
    suspend fun addFavorite(favorite: Favorite)
    suspend fun removeFavorite(streamId: Int, type: ContentType)
}

// WatchHistoryRepository.kt
package com.KediTV.domain.repository

import com.KediTV.domain.model.WatchHistory
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun getContinueWatching(): Flow<List<WatchHistory>>
    suspend fun saveProgress(history: WatchHistory)
    suspend fun getProgress(streamId: Int, episodeId: Int? = null): WatchHistory?
}
```

- [ ] **Step 8: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/KediTV/domain/
git commit -m "feat: domain models and repository interfaces"
```

---

## Task 3: Room Database & DAOs

**Files:**
- Create: `app/src/main/java/com/KediTV/data/db/entity/ServerConfigEntity.kt`
- Create: `app/src/main/java/com/KediTV/data/db/entity/FavoriteEntity.kt`
- Create: `app/src/main/java/com/KediTV/data/db/entity/WatchHistoryEntity.kt`
- Create: `app/src/main/java/com/KediTV/data/db/dao/ServerConfigDao.kt`
- Create: `app/src/main/java/com/KediTV/data/db/dao/FavoriteDao.kt`
- Create: `app/src/main/java/com/KediTV/data/db/dao/WatchHistoryDao.kt`
- Create: `app/src/main/java/com/KediTV/data/db/KediDatabase.kt`
- Test: `app/src/androidTest/java/com/KediTV/data/db/DaoTest.kt`

- [ ] **Step 1: Create Room entities**

```kotlin
// ServerConfigEntity.kt
package com.KediTV.data.db.entity

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

// FavoriteEntity.kt
package com.KediTV.data.db.entity

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

// WatchHistoryEntity.kt
package com.KediTV.data.db.entity

import androidx.room.Entity

// episodeId = -1 means "no episode" (VOD/LIVE). Never use 0 as sentinel
// since real episode IDs can be 0 per Xtream API.
@Entity(tableName = "watch_history", primaryKeys = ["streamId", "type", "episodeId"])
data class WatchHistoryEntity(
    val streamId: Int,
    val type: String,
    val name: String,
    val posterUrl: String?,
    val positionMs: Long,
    val durationMs: Long,
    val lastWatched: Long,
    val episodeId: Int = -1,
    val episodeTitle: String? = null
)
```

- [ ] **Step 2: Create DAOs**

```kotlin
// ServerConfigDao.kt
package com.KediTV.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.KediTV.data.db.entity.ServerConfigEntity

@Dao
interface ServerConfigDao {
    @Query("SELECT * FROM server_config WHERE id = 1")
    suspend fun getConfig(): ServerConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ServerConfigEntity)

    @Query("DELETE FROM server_config")
    suspend fun clearConfig()
}

// FavoriteDao.kt
package com.KediTV.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.KediTV.data.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE streamId = :streamId AND type = :type")
    suspend fun isFavorite(streamId: Int, type: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE streamId = :streamId AND type = :type")
    suspend fun delete(streamId: Int, type: String)
}

// WatchHistoryDao.kt
package com.KediTV.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.KediTV.data.db.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history WHERE positionMs > 0 ORDER BY lastWatched DESC LIMIT 20")
    fun getContinueWatching(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId AND episodeId = :episodeId")
    suspend fun getProgress(streamId: Int, episodeId: Int = -1): WatchHistoryEntity?
}
```

- [ ] **Step 3: Create `KediDatabase.kt`**

```kotlin
package com.KediTV.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.KediTV.data.db.dao.FavoriteDao
import com.KediTV.data.db.dao.ServerConfigDao
import com.KediTV.data.db.dao.WatchHistoryDao
import com.KediTV.data.db.entity.FavoriteEntity
import com.KediTV.data.db.entity.ServerConfigEntity
import com.KediTV.data.db.entity.WatchHistoryEntity

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
```

- [ ] **Step 4: Write DAO instrumented test**

```kotlin
// DaoTest.kt
package com.KediTV.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.KediTV.data.db.dao.FavoriteDao
import com.KediTV.data.db.dao.WatchHistoryDao
import com.KediTV.data.db.entity.FavoriteEntity
import com.KediTV.data.db.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {
    private lateinit var db: KediDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var watchHistoryDao: WatchHistoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KediDatabase::class.java).build()
        favoriteDao = db.favoriteDao()
        watchHistoryDao = db.watchHistoryDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndQueryFavorite() = runTest {
        val fav = FavoriteEntity(1, "LIVE", "TRT 1", null, "Ulusal")
        favoriteDao.insert(fav)
        val all = favoriteDao.getAll().first()
        assertEquals(1, all.size)
        assertTrue(favoriteDao.isFavorite(1, "LIVE"))
    }

    @Test
    fun deleteFavorite() = runTest {
        favoriteDao.insert(FavoriteEntity(1, "LIVE", "TRT 1", null, null))
        favoriteDao.delete(1, "LIVE")
        assertFalse(favoriteDao.isFavorite(1, "LIVE"))
    }

    @Test
    fun continueWatchingOnlyReturnsNonZeroPosition() = runTest {
        watchHistoryDao.upsert(WatchHistoryEntity(1, "VOD", "Film 1", null, 5000, 90000, System.currentTimeMillis()))
        watchHistoryDao.upsert(WatchHistoryEntity(2, "VOD", "Film 2", null, 0, 90000, System.currentTimeMillis()))
        val list = watchHistoryDao.getContinueWatching().first()
        assertEquals(1, list.size)
        assertEquals(1, list[0].streamId)
    }
}
```

- [ ] **Step 5: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/KediTV/data/db/ app/src/androidTest/
git commit -m "feat: Room database, entities, DAOs with tests"
```

---

## Task 4: Xtream Codes API Service

**Files:**
- Create: `app/src/main/java/com/KediTV/data/api/model/AuthResponse.kt`
- Create: `app/src/main/java/com/KediTV/data/api/model/CategoryDto.kt`
- Create: `app/src/main/java/com/KediTV/data/api/model/LiveStreamDto.kt`
- Create: `app/src/main/java/com/KediTV/data/api/model/VodStreamDto.kt`
- Create: `app/src/main/java/com/KediTV/data/api/model/SeriesDto.kt`
- Create: `app/src/main/java/com/KediTV/data/api/model/SeriesInfoDto.kt`
- Create: `app/src/main/java/com/KediTV/data/api/XtreamApiService.kt`
- Test: `app/src/test/java/com/KediTV/data/api/XtreamApiServiceTest.kt`

- [ ] **Step 1: Create API DTOs**

```kotlin
// AuthResponse.kt
package com.KediTV.data.api.model

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

// CategoryDto.kt
package com.KediTV.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("parent_id") val parentId: Int = 0
)

// LiveStreamDto.kt
package com.KediTV.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveStreamDto(
    @SerialName("stream_id") val streamId: Int,
    @SerialName("name") val name: String,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("rating") val rating: String? = null
)

// VodStreamDto.kt
package com.KediTV.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VodStreamDto(
    @SerialName("stream_id") val streamId: Int,
    @SerialName("name") val name: String,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("rating") val rating: String? = null,
    @SerialName("plot") val plot: String? = null
)

// SeriesDto.kt
package com.KediTV.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesDto(
    @SerialName("series_id") val seriesId: Int,
    @SerialName("name") val name: String,
    @SerialName("cover") val cover: String? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("rating") val rating: String? = null,
    @SerialName("plot") val plot: String? = null
)

// SeriesInfoDto.kt
package com.KediTV.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SeriesInfoDto(
    @SerialName("info") val info: SeriesDetailDto? = null,
    @SerialName("episodes") val episodes: Map<String, List<EpisodeDto>> = emptyMap()
)

@Serializable
data class SeriesDetailDto(
    @SerialName("name") val name: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("cast") val cast: String? = null,
    @SerialName("rating") val rating: String? = null
)

@Serializable
data class EpisodeDto(
    @SerialName("id") val id: String,
    @SerialName("episode_num") val episodeNum: Int,
    @SerialName("title") val title: String,
    @SerialName("info") val info: EpisodeInfoDto? = null
)

@Serializable
data class EpisodeInfoDto(
    @SerialName("movie_image") val movieImage: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("duration") val duration: String? = null
)
```

- [ ] **Step 2: Create `XtreamApiService.kt`**

```kotlin
package com.KediTV.data.api

import com.KediTV.data.api.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
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
        baseUrl = serverUrl.trimEnd('/')
        username = user
        password = pass
    }

    private fun apiUrl(action: String? = null): String {
        val base = "$baseUrl/player_api.php?username=$username&password=$password"
        return if (action != null) "$base&action=$action" else base
    }

    // Callback invoked on 403 — MainActivity/NavHost should navigate to Login
    var onUnauthorized: (() -> Unit)? = null

    suspend fun authenticate(): AuthResponse =
        client.get(apiUrl()).body()

    suspend fun <T> safeGet(url: String, parse: suspend () -> T): T {
        val response = client.get(url)
        if (response.status.value == 403 || response.status.value == 401) {
            onUnauthorized?.invoke()
            throw Exception("Oturum suresi doldu. Lutfen tekrar giris yapin.")
        }
        return response.body()
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
```

- [ ] **Step 3: Write API service test with Ktor mock**

```kotlin
package com.KediTV.data.api

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class XtreamApiServiceTest {

    private fun createMockClient(responseBody: String): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = responseBody,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `authenticate parses auth response`() = runTest {
        val json = """{"user_info":{"username":"test","password":"test","status":"Active","exp_date":"1735689600","auth":1},"server_info":{"url":"http://example.com","port":"8080"}}"""
        val service = XtreamApiService(createMockClient(json))
        service.configure("http://example.com:8080", "test", "test")

        val response = service.authenticate()
        assertEquals(1, response.userInfo.auth)
        assertEquals("Active", response.userInfo.status)
    }

    @Test
    fun `getLiveCategories parses categories`() = runTest {
        val json = """[{"category_id":"1","category_name":"Ulusal","parent_id":0}]"""
        val service = XtreamApiService(createMockClient(json))
        service.configure("http://example.com:8080", "test", "test")

        val categories = service.getLiveCategories()
        assertEquals(1, categories.size)
        assertEquals("Ulusal", categories[0].categoryName)
    }

    @Test
    fun `buildStreamUrl formats correctly`() {
        // Use MockEngine to avoid needing a real engine on JVM
        val service = XtreamApiService(createMockClient("{}"))
        service.configure("http://example.com:8080", "user", "pass")

        assertEquals("http://example.com:8080/live/user/pass/123.ts", service.buildStreamUrl("live", 123))
        assertEquals("http://example.com:8080/movie/user/pass/456.mp4", service.buildVodUrl(456))
    }
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew test --tests "com.KediTV.data.api.XtreamApiServiceTest"`
Expected: 3 tests PASSED

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/KediTV/data/api/ app/src/test/
git commit -m "feat: Xtream Codes API service with DTOs and tests"
```

---

## Task 5: Repository Implementations & DI

**Files:**
- Create: `app/src/main/java/com/KediTV/data/repository/AuthRepositoryImpl.kt`
- Create: `app/src/main/java/com/KediTV/data/repository/ContentRepositoryImpl.kt`
- Create: `app/src/main/java/com/KediTV/data/repository/FavoriteRepositoryImpl.kt`
- Create: `app/src/main/java/com/KediTV/data/repository/WatchHistoryRepositoryImpl.kt`
- Create: `app/src/main/java/com/KediTV/di/AppModule.kt`
- Create: `app/src/main/java/com/KediTV/di/RepositoryModule.kt`
- Test: `app/src/test/java/com/KediTV/data/repository/AuthRepositoryImplTest.kt`
- Test: `app/src/test/java/com/KediTV/data/repository/ContentRepositoryImplTest.kt`

- [ ] **Step 1: Create `AuthRepositoryImpl.kt`**

```kotlin
package com.KediTV.data.repository

import com.KediTV.data.api.XtreamApiService
import com.KediTV.data.db.dao.ServerConfigDao
import com.KediTV.data.db.entity.ServerConfigEntity
import com.KediTV.domain.model.ServerConfig
import com.KediTV.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: XtreamApiService,
    private val dao: ServerConfigDao
) : AuthRepository {

    override suspend fun login(serverUrl: String, username: String, password: String): Result<ServerConfig> {
        return try {
            api.configure(serverUrl, username, password)
            val response = api.authenticate()

            if (response.userInfo.auth != 1) {
                return Result.failure(Exception("Hesap aktif degil"))
            }

            val expDate = response.userInfo.expDate?.toLongOrNull()
            if (expDate != null && expDate < System.currentTimeMillis() / 1000) {
                return Result.failure(Exception("Hesabinizin suresi dolmus"))
            }

            val config = ServerConfig(serverUrl, username, password, expDate)
            dao.saveConfig(ServerConfigEntity(
                serverUrl = serverUrl,
                username = username,
                password = password,
                expDate = expDate
            ))

            Result.success(config)
        } catch (e: Exception) {
            Result.failure(Exception("Sunucuya baglanamadi: ${e.message}"))
        }
    }

    override suspend fun getSavedConfig(): ServerConfig? {
        return dao.getConfig()?.let {
            api.configure(it.serverUrl, it.username, it.password)
            ServerConfig(it.serverUrl, it.username, it.password, it.expDate)
        }
    }

    override suspend fun logout() {
        dao.clearConfig()
    }
}
```

- [ ] **Step 2: Create `ContentRepositoryImpl.kt`**

```kotlin
package com.KediTV.data.repository

import com.KediTV.data.api.XtreamApiService
import com.KediTV.domain.model.*
import com.KediTV.domain.repository.ContentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XtreamApiService
) : ContentRepository {

    private val categoryCache = mutableMapOf<ContentType, List<Category>>()
    private val contentCache = mutableMapOf<String, List<ContentItem>>()

    override suspend fun getCategories(type: ContentType): Result<List<Category>> {
        categoryCache[type]?.let { return Result.success(it) }

        return try {
            val dtos = when (type) {
                ContentType.LIVE -> api.getLiveCategories()
                ContentType.VOD -> api.getVodCategories()
                ContentType.SERIES -> api.getSeriesCategories()
            }
            val categories = dtos.map { Category(it.categoryId, it.categoryName, type) }
            categoryCache[type] = categories
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContentList(type: ContentType, categoryId: String): Result<List<ContentItem>> {
        val key = "${type.name}_$categoryId"
        contentCache[key]?.let { return Result.success(it) }

        return try {
            val items = when (type) {
                ContentType.LIVE -> api.getLiveStreams()
                    .filter { it.categoryId == categoryId }
                    .map { ContentItem(it.streamId, it.name, type, it.categoryId, it.streamIcon, it.rating) }
                ContentType.VOD -> api.getVodStreams()
                    .filter { it.categoryId == categoryId }
                    .map { ContentItem(it.streamId, it.name, type, it.categoryId, it.streamIcon, it.rating) }
                ContentType.SERIES -> api.getSeries()
                    .filter { it.categoryId == categoryId }
                    .map { ContentItem(it.seriesId, it.name, type, it.categoryId, it.cover, it.rating) }
            }
            contentCache[key] = items
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllVod(): Result<List<ContentItem>> {
        contentCache["ALL_VOD"]?.let { return Result.success(it) }
        return try {
            val items = api.getVodStreams()
                .map { ContentItem(it.streamId, it.name, ContentType.VOD, it.categoryId, it.streamIcon, it.rating) }
            contentCache["ALL_VOD"] = items
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSeriesInfo(seriesId: Int): Result<SeriesInfo> {
        return try {
            val dto = api.getSeriesInfo(seriesId)
            val seasons = dto.episodes.map { (seasonNum, episodes) ->
                Season(
                    seasonNumber = seasonNum.toIntOrNull() ?: 0,
                    name = "Sezon $seasonNum",
                    episodes = episodes.map { ep ->
                        Episode(
                            id = ep.id.toIntOrNull() ?: 0,
                            episodeNumber = ep.episodeNum,
                            title = ep.title,
                            posterUrl = ep.info?.movieImage,
                            plot = ep.info?.plot,
                            duration = ep.info?.duration
                        )
                    }
                )
            }.sortedBy { it.seasonNumber }

            Result.success(SeriesInfo(
                seriesId = seriesId,
                name = dto.info?.name ?: "",
                posterUrl = dto.info?.cover,
                plot = dto.info?.plot,
                cast = dto.info?.cast,
                rating = dto.info?.rating,
                seasons = seasons
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun buildStreamUrl(type: ContentType, streamId: Int): String {
        return when (type) {
            ContentType.LIVE -> api.buildStreamUrl("live", streamId)
            ContentType.VOD -> api.buildVodUrl(streamId)
            ContentType.SERIES -> api.buildSeriesUrl(streamId)
        }
    }

    override fun buildEpisodeUrl(episodeId: Int): String = api.buildSeriesUrl(episodeId)

    fun clearCache() {
        categoryCache.clear()
        contentCache.clear()
    }
}
```

- [ ] **Step 3: Create `FavoriteRepositoryImpl.kt` and `WatchHistoryRepositoryImpl.kt`**

```kotlin
// FavoriteRepositoryImpl.kt
package com.KediTV.data.repository

import com.KediTV.data.db.dao.FavoriteDao
import com.KediTV.data.db.entity.FavoriteEntity
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.model.Favorite
import com.KediTV.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<Favorite>> =
        dao.getAll().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun isFavorite(streamId: Int, type: ContentType): Boolean =
        dao.isFavorite(streamId, type.name)

    override suspend fun addFavorite(favorite: Favorite) {
        dao.insert(FavoriteEntity(
            streamId = favorite.streamId,
            type = favorite.type.name,
            name = favorite.name,
            posterUrl = favorite.posterUrl,
            categoryName = favorite.categoryName,
            addedAt = favorite.addedAt
        ))
    }

    override suspend fun removeFavorite(streamId: Int, type: ContentType) {
        dao.delete(streamId, type.name)
    }

    private fun FavoriteEntity.toDomain() = Favorite(
        streamId = streamId,
        type = ContentType.valueOf(type),
        name = name,
        posterUrl = posterUrl,
        categoryName = categoryName,
        addedAt = addedAt
    )
}

// WatchHistoryRepositoryImpl.kt
package com.KediTV.data.repository

import com.KediTV.data.db.dao.WatchHistoryDao
import com.KediTV.data.db.entity.WatchHistoryEntity
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.model.WatchHistory
import com.KediTV.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val dao: WatchHistoryDao
) : WatchHistoryRepository {

    override fun getContinueWatching(): Flow<List<WatchHistory>> =
        dao.getContinueWatching().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun saveProgress(history: WatchHistory) {
        dao.upsert(WatchHistoryEntity(
            streamId = history.streamId,
            type = history.type.name,
            name = history.name,
            posterUrl = history.posterUrl,
            positionMs = history.positionMs,
            durationMs = history.durationMs,
            lastWatched = history.lastWatched,
            episodeId = history.episodeId ?: -1,
            episodeTitle = history.episodeTitle
        ))
    }

    override suspend fun getProgress(streamId: Int, episodeId: Int?): WatchHistory? {
        return dao.getProgress(streamId, episodeId ?: 0)?.toDomain()
    }

    private fun WatchHistoryEntity.toDomain() = WatchHistory(
        streamId = streamId,
        type = ContentType.valueOf(type),
        name = name,
        posterUrl = posterUrl,
        positionMs = positionMs,
        durationMs = durationMs,
        lastWatched = lastWatched,
        episodeId = if (episodeId == -1) null else episodeId,
        episodeTitle = episodeTitle
    )
}
```

- [ ] **Step 4: Create DI modules**

```kotlin
// AppModule.kt
package com.KediTV.di

import android.content.Context
import androidx.room.Room
import com.KediTV.data.api.XtreamApiService
import com.KediTV.data.db.KediDatabase
import com.KediTV.data.db.dao.FavoriteDao
import com.KediTV.data.db.dao.ServerConfigDao
import com.KediTV.data.db.dao.WatchHistoryDao
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
    fun provideDatabase(@ApplicationContext context: Context): KediDatabase =
        Room.databaseBuilder(context, KediDatabase::class.java, "KediTV.db").build()

    @Provides fun provideServerConfigDao(db: KediDatabase): ServerConfigDao = db.serverConfigDao()
    @Provides fun provideFavoriteDao(db: KediDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideWatchHistoryDao(db: KediDatabase): WatchHistoryDao = db.watchHistoryDao()
}

// RepositoryModule.kt
package com.KediTV.di

import com.KediTV.data.repository.*
import com.KediTV.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository

    @Binds @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds @Singleton
    abstract fun bindWatchHistoryRepository(impl: WatchHistoryRepositoryImpl): WatchHistoryRepository
}
```

- [ ] **Step 5: Write repository tests**

```kotlin
// AuthRepositoryImplTest.kt
package com.KediTV.data.repository

import com.KediTV.data.api.XtreamApiService
import com.KediTV.data.api.model.*
import com.KediTV.data.db.dao.ServerConfigDao
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryImplTest {
    private val api = mockk<XtreamApiService>(relaxed = true)
    private val dao = mockk<ServerConfigDao>(relaxed = true)
    private val repo = AuthRepositoryImpl(api, dao)

    @Test
    fun `login succeeds with valid auth`() = runTest {
        coEvery { api.authenticate() } returns AuthResponse(
            userInfo = UserInfo("test", "test", "Active", "9999999999", 1),
            serverInfo = ServerInfo("http://test.com", "8080")
        )

        val result = repo.login("http://test.com:8080", "test", "test")
        assertTrue(result.isSuccess)
        coVerify { dao.saveConfig(any()) }
    }

    @Test
    fun `login fails with inactive auth`() = runTest {
        coEvery { api.authenticate() } returns AuthResponse(
            userInfo = UserInfo("test", "test", "Disabled", null, 0),
            serverInfo = ServerInfo("http://test.com", "8080")
        )

        val result = repo.login("http://test.com:8080", "test", "test")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("aktif degil") == true)
    }
}

// ContentRepositoryImplTest.kt
package com.KediTV.data.repository

import com.KediTV.data.api.XtreamApiService
import com.KediTV.data.api.model.CategoryDto
import com.KediTV.domain.model.ContentType
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ContentRepositoryImplTest {
    private val api = mockk<XtreamApiService>(relaxed = true)
    private val repo = ContentRepositoryImpl(api)

    @Test
    fun `getCategories returns mapped categories`() = runTest {
        coEvery { api.getLiveCategories() } returns listOf(
            CategoryDto("1", "Ulusal"),
            CategoryDto("2", "Spor")
        )

        val result = repo.getCategories(ContentType.LIVE)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Ulusal", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `getCategories caches results`() = runTest {
        coEvery { api.getLiveCategories() } returns listOf(CategoryDto("1", "Ulusal"))

        repo.getCategories(ContentType.LIVE)
        repo.getCategories(ContentType.LIVE)

        coVerify(exactly = 1) { api.getLiveCategories() }
    }
}
```

- [ ] **Step 6: Run all tests**

Run: `./gradlew test`
Expected: All tests PASSED

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/KediTV/data/repository/ app/src/main/java/com/KediTV/di/ app/src/test/
git commit -m "feat: repository implementations and DI setup with tests"
```

---

## Task 6: Theme, Common UI & Navigation

**Files:**
- Create: `app/src/main/java/com/KediTV/ui/theme/Color.kt`
- Create: `app/src/main/java/com/KediTV/ui/theme/Type.kt`
- Create: `app/src/main/java/com/KediTV/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/KediTV/ui/common/PlatformUtils.kt`
- Create: `app/src/main/java/com/KediTV/ui/common/ContentCard.kt`
- Create: `app/src/main/java/com/KediTV/ui/common/EmptyState.kt`
- Create: `app/src/main/java/com/KediTV/ui/common/ErrorState.kt`
- Create: `app/src/main/java/com/KediTV/ui/common/LoadingIndicator.kt`
- Create: `app/src/main/java/com/KediTV/ui/navigation/NavRoutes.kt`
- Create: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/java/com/KediTV/MainActivity.kt`

- [ ] **Step 1: Create theme files**

```kotlin
// Color.kt
package com.KediTV.ui.theme

import androidx.compose.ui.graphics.Color

val KediOrange = Color(0xFFFF8C00)
val KediOrangeLight = Color(0xFFFFB347)
val KediBackground = Color(0xFF0D1117)
val KediSurface = Color(0xFF1A1A2E)
val KediSurfaceLight = Color(0xFF252540)
val KediTextPrimary = Color(0xFFFFFFFF)
val KediTextSecondary = Color(0xFFB0B0B0)
val KediError = Color(0xFFCF6679)

// Type.kt
package com.KediTV.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.KediTV.R

val NunitoFamily = FontFamily(
    Font(R.font.nunito_bold, FontWeight.Bold)
)

val KediTypography = Typography(
    headlineLarge = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
)

// Theme.kt
package com.KediTV.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KediColorScheme = darkColorScheme(
    primary = KediOrange,
    secondary = KediOrangeLight,
    background = KediBackground,
    surface = KediSurface,
    onPrimary = KediTextPrimary,
    onSecondary = KediBackground,
    onBackground = KediTextPrimary,
    onSurface = KediTextPrimary,
    error = KediError,
    onError = KediTextPrimary
)

@Composable
fun KediTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KediColorScheme,
        typography = KediTypography,
        content = content
    )
}
```

- [ ] **Step 2: Create `PlatformUtils.kt`**

```kotlin
package com.KediTV.ui.common

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun isTV(): Boolean {
    val context = LocalContext.current
    return remember {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
}
```

- [ ] **Step 3: Create common UI components**

```kotlin
// ContentCard.kt
package com.KediTV.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.KediTV.R

@Composable
fun ContentCard(
    name: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = posterUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_cat_placeholder),
                error = painterResource(R.drawable.ic_cat_placeholder)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// EmptyState.kt
package com.KediTV.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "\uD83D\uDE3F", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ErrorState.kt
package com.KediTV.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "\uD83D\uDE3F", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Tekrar Dene")
            }
        }
    }
}

// LoadingIndicator.kt
package com.KediTV.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}
```

- [ ] **Step 4: Create navigation**

```kotlin
// NavRoutes.kt
package com.KediTV.ui.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CATEGORY = "category/{type}"
    const val CONTENT = "content/{type}/{categoryId}"
    const val DETAIL = "detail/{type}/{streamId}?name={name}&posterUrl={posterUrl}"
    const val PLAYER = "player/{type}/{streamId}?episodeId={episodeId}"
    const val SETTINGS = "settings"

    fun category(type: String) = "category/$type"
    fun content(type: String, categoryId: String) = "content/$type/$categoryId"
    fun detail(type: String, streamId: Int, name: String = "", posterUrl: String = "") =
        "detail/$type/$streamId?name=${java.net.URLEncoder.encode(name, "UTF-8")}&posterUrl=${java.net.URLEncoder.encode(posterUrl, "UTF-8")}"
    fun player(type: String, streamId: Int, episodeId: Int? = null): String {
        val base = "player/$type/$streamId"
        return if (episodeId != null) "$base?episodeId=$episodeId" else base
    }
}

// KediNavHost.kt
package com.KediTV.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun KediNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.LOGIN) {
            // LoginScreen — will be added in Task 7
        }
        composable(NavRoutes.HOME) {
            // HomeScreen — will be added in Task 8
        }
        composable(
            NavRoutes.CATEGORY,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) {
            // CategoryScreen — will be added in Task 9
        }
        composable(
            NavRoutes.CONTENT,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType }
            )
        ) {
            // ContentListScreen — will be added in Task 9
        }
        composable(
            NavRoutes.DETAIL,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType }
            )
        ) {
            // DetailScreen — will be added in Task 10
        }
        composable(
            NavRoutes.PLAYER,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("streamId") { type = NavType.IntType },
                navArgument("episodeId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) {
            // PlayerScreen — will be added in Task 11
        }
        composable(NavRoutes.SETTINGS) {
            // SettingsScreen — will be added in Task 12
        }
    }
}
```

- [ ] **Step 5: Update `MainActivity.kt`**

**Important:** Keep `@AndroidEntryPoint` — required for Hilt injection.

```kotlin
package com.KediTV

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.KediTV.data.api.XtreamApiService
import com.KediTV.ui.navigation.KediNavHost
import com.KediTV.ui.navigation.NavRoutes
import com.KediTV.ui.theme.KediTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint // DO NOT REMOVE — required for Hilt
class MainActivity : ComponentActivity() {

    @Inject lateinit var apiService: XtreamApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KediTheme {
                val navController = rememberNavController()

                // Wire 403 callback — navigate to login on session expiry
                apiService.onUnauthorized = {
                    runOnUiThread {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                KediNavHost(
                    navController = navController,
                    startDestination = NavRoutes.LOGIN
                )
            }
        }
    }
}
```

- [ ] **Step 6: Create `strings.xml` and `colors.xml`**

```xml
<!-- strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">KediTV</string>
    <string name="login_title">KediTV\'ye Hos Geldin</string>
    <string name="server_url">Sunucu URL</string>
    <string name="username">Kullanici Adi</string>
    <string name="password">Sifre</string>
    <string name="connect">Baglan</string>
    <string name="connecting">Baglaniyor...</string>
    <string name="live_tv">Canli TV</string>
    <string name="movies">Filmler</string>
    <string name="series">Diziler</string>
    <string name="continue_watching">Devam Et</string>
    <string name="favorites">Favoriler</string>
    <string name="search_hint">Ara...</string>
    <string name="no_favorites">Henuz favori eklemedin</string>
    <string name="no_results">Sonuc bulunamadi</string>
    <string name="no_content">Icerik bulunamadi</string>
    <string name="error_connection">Sunucuya baglanamadi</string>
    <string name="error_auth">Gecersiz kullanici adi veya sifre</string>
    <string name="error_expired">Hesabinizin suresi dolmus</string>
    <string name="error_inactive">Hesap aktif degil</string>
    <string name="error_stream">Kanal calismiyor</string>
    <string name="error_no_internet">Baglanti yok</string>
    <string name="retry">Tekrar Dene</string>
    <string name="play">Oynat</string>
    <string name="continue_play">Devam Et</string>
    <string name="add_favorite">Favorilere Ekle</string>
    <string name="remove_favorite">Favorilerden Cikar</string>
    <string name="settings">Ayarlar</string>
    <string name="logout">Cikis Yap</string>
    <string name="buffer_low">Dusuk</string>
    <string name="buffer_medium">Orta</string>
    <string name="buffer_high">Yuksek</string>
    <string name="buffer_size">Buffer Boyutu</string>
    <string name="season_label">Sezon %d</string>
</resources>

<!-- colors.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="kedi_orange">#FFFF8C00</color>
    <color name="kedi_background">#FF0D1117</color>
    <color name="kedi_surface">#FF1A1A2E</color>
</resources>
```

- [ ] **Step 7a: Create `ic_launcher_foreground.xml` (required for build)**

Create `app/src/main/res/drawable/ic_launcher_foreground.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#FF0D1117" android:pathData="M0,0h108v108H0z"/>
    <path android:fillColor="#FFFF8C00"
        android:pathData="M54,28 C42,28 32,38 32,50 C32,62 42,72 54,72 C66,72 76,62 76,50 C76,38 66,28 54,28z M38,25 L30,12 L44,22z M70,25 L78,12 L64,22z"/>
</vector>
```

Also create `app/src/main/res/mipmap-hdpi/ic_launcher.xml` pointing to the foreground (adaptive icon stub):

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/kedi_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

- [ ] **Step 7b: Create `ic_cat_placeholder.xml` — a simple cat silhouette placeholder:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="140dp"
    android:height="200dp"
    android:viewportWidth="140"
    android:viewportHeight="200">
    <path
        android:fillColor="#FF1A1A2E"
        android:pathData="M0,0h140v200H0z" />
    <path
        android:fillColor="#FF3A3A5E"
        android:pathData="M70,60 C50,60 35,75 35,95 C35,115 50,130 70,130 C90,130 105,115 105,95 C105,75 90,60 70,60z M50,55 L40,35 L55,50z M90,55 L100,35 L85,50z" />
</vector>
```

- [ ] **Step 8: Download Nunito Bold font**

Download `nunito_bold.ttf` to `app/src/main/res/font/`. If not available, use system default (remove NunitoFamily references temporarily).

Run: `mkdir -p app/src/main/res/font`

- [ ] **Step 9: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/KediTV/ui/ app/src/main/res/ app/src/main/java/com/KediTV/MainActivity.kt
git commit -m "feat: theme, common UI components, and navigation setup"
```

---

## Task 7: Login Screen

**Files:**
- Create: `app/src/main/java/com/KediTV/domain/usecase/LoginUseCase.kt`
- Create: `app/src/main/java/com/KediTV/ui/login/LoginViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/login/LoginScreen.kt`
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`
- Test: `app/src/test/java/com/KediTV/domain/usecase/LoginUseCaseTest.kt`

- [ ] **Step 1: Write LoginUseCase test**

```kotlin
package com.KediTV.domain.usecase

import com.KediTV.domain.model.ServerConfig
import com.KediTV.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LoginUseCaseTest {
    private val repo = mockk<AuthRepository>()
    private val useCase = LoginUseCase(repo)

    @Test
    fun `returns success when login succeeds`() = runTest {
        coEvery { repo.login(any(), any(), any()) } returns
            Result.success(ServerConfig("http://test.com", "user", "pass"))

        val result = useCase("http://test.com", "user", "pass")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns failure when login fails`() = runTest {
        coEvery { repo.login(any(), any(), any()) } returns
            Result.failure(Exception("Hesap aktif degil"))

        val result = useCase("http://test.com", "user", "pass")
        assertTrue(result.isFailure)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.KediTV.domain.usecase.LoginUseCaseTest"`
Expected: FAIL — LoginUseCase not found

- [ ] **Step 3: Create `LoginUseCase.kt`**

```kotlin
package com.KediTV.domain.usecase

import com.KediTV.domain.model.ServerConfig
import com.KediTV.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        serverUrl: String,
        username: String,
        password: String
    ): Result<ServerConfig> = repository.login(serverUrl, username, password)
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.KediTV.domain.usecase.LoginUseCaseTest"`
Expected: PASSED

- [ ] **Step 5: Create `LoginViewModel.kt`**

```kotlin
package com.KediTV.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.repository.AuthRepository
import com.KediTV.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isCheckingSaved: Boolean = true
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    init {
        checkSavedLogin()
    }

    private fun checkSavedLogin() {
        viewModelScope.launch {
            val config = authRepository.getSavedConfig()
            if (config != null) {
                _state.value = _state.value.copy(
                    serverUrl = config.serverUrl,
                    username = config.username,
                    password = config.password,
                    isLoggedIn = true,
                    isCheckingSaved = false
                )
            } else {
                _state.value = _state.value.copy(isCheckingSaved = false)
            }
        }
    }

    fun updateServerUrl(url: String) { _state.value = _state.value.copy(serverUrl = url, error = null) }
    fun updateUsername(name: String) { _state.value = _state.value.copy(username = name, error = null) }
    fun updatePassword(pass: String) { _state.value = _state.value.copy(password = pass, error = null) }

    fun login() {
        val s = _state.value
        if (s.serverUrl.isBlank() || s.username.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "Tum alanlari doldur")
            return
        }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            val result = loginUseCase(s.serverUrl, s.username, s.password)
            result.fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, isLoggedIn = true) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }
}
```

- [ ] **Step 6: Create `LoginScreen.kt`**

```kotlin
package com.KediTV.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.KediTV.R
import com.KediTV.ui.common.LoadingIndicator

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    if (state.isCheckingSaved) {
        LoadingIndicator()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = viewModel::updateServerUrl,
                label = { Text(stringResource(R.string.server_url)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::updateUsername,
                label = { Text(stringResource(R.string.username)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                label = { Text(stringResource(R.string.password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.connect))
                }
            }
        }
    }
}
```

- [ ] **Step 7: Wire LoginScreen into NavHost**

Update the `composable(NavRoutes.LOGIN)` block in `KediNavHost.kt`:

```kotlin
composable(NavRoutes.LOGIN) {
    LoginScreen(onLoginSuccess = {
        navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
        }
    })
}
```

- [ ] **Step 8: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/KediTV/domain/usecase/LoginUseCase.kt app/src/main/java/com/KediTV/ui/login/ app/src/main/java/com/KediTV/ui/navigation/ app/src/test/
git commit -m "feat: login screen with auto-login and Xtream auth"
```

---

## Task 8: Home Screen

**Files:**
- Create: `app/src/main/java/com/KediTV/domain/usecase/GetContinueWatchingUseCase.kt`
- Create: `app/src/main/java/com/KediTV/ui/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/home/HomeScreen.kt`
- Create: `app/src/main/java/com/KediTV/ui/home/HomeMobileContent.kt`
- Create: `app/src/main/java/com/KediTV/ui/home/HomeTvContent.kt`
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`

- [ ] **Step 1: Create `GetContinueWatchingUseCase.kt`**

```kotlin
package com.KediTV.domain.usecase

import com.KediTV.domain.model.WatchHistory
import com.KediTV.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContinueWatchingUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    operator fun invoke(): Flow<List<WatchHistory>> = repository.getContinueWatching()
}
```

- [ ] **Step 2: Create `HomeViewModel.kt`**

```kotlin
package com.KediTV.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.model.*
import com.KediTV.domain.repository.ContentRepository
import com.KediTV.domain.repository.FavoriteRepository
import com.KediTV.domain.usecase.GetContinueWatchingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val featuredItems: List<ContentItem> = emptyList(),
    val currentFeaturedIndex: Int = 0,
    val continueWatching: List<WatchHistory> = emptyList(),
    val favorites: List<Favorite> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val getContinueWatching: GetContinueWatchingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        loadHome()
        observeContinueWatching()
        observeFavorites()
        rotateFeaturedBanner()
    }

    private fun loadHome() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // Use getAllVod() — not getContentList(VOD, "") which filters by categoryId
            contentRepository.getAllVod().fold(
                onSuccess = { items ->
                    val featured = items.shuffled().take(5)
                    _state.value = _state.value.copy(isLoading = false, featuredItems = featured, error = null)
                },
                onFailure = {
                    _state.value = _state.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    private fun rotateFeaturedBanner() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(5_000)
                val items = _state.value.featuredItems
                if (items.size > 1) {
                    val next = (_state.value.currentFeaturedIndex + 1) % items.size
                    _state.value = _state.value.copy(currentFeaturedIndex = next)
                }
            }
        }
    }

    private fun observeContinueWatching() {
        getContinueWatching().onEach { list ->
            _state.value = _state.value.copy(continueWatching = list)
        }.launchIn(viewModelScope)
    }

    private fun observeFavorites() {
        favoriteRepository.getAllFavorites().onEach { list ->
            _state.value = _state.value.copy(favorites = list)
        }.launchIn(viewModelScope)
    }

    fun retry() = loadHome()
}
```

- [ ] **Step 3: Create `HomeScreen.kt` with platform branching**

```kotlin
package com.KediTV.ui.home

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.KediTV.ui.common.ErrorState
import com.KediTV.ui.common.LoadingIndicator
import com.KediTV.ui.common.isTV

@Composable
fun HomeScreen(
    onNavigateToCategory: (String) -> Unit,
    onNavigateToDetail: (String, Int) -> Unit,
    onNavigateToPlayer: (String, Int, Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> LoadingIndicator()
        state.error != null -> ErrorState(message = state.error!!, onRetry = viewModel::retry)
        else -> {
            if (isTV()) {
                HomeTvContent(
                    state = state,
                    onCategoryClick = onNavigateToCategory,
                    onItemClick = onNavigateToDetail,
                    onContinueClick = onNavigateToPlayer,
                    onSettingsClick = onNavigateToSettings
                )
            } else {
                HomeMobileContent(
                    state = state,
                    onCategoryClick = onNavigateToCategory,
                    onItemClick = onNavigateToDetail,
                    onContinueClick = onNavigateToPlayer,
                    onSettingsClick = onNavigateToSettings
                )
            }
        }
    }
}
```

- [ ] **Step 4: Create `HomeMobileContent.kt`**

```kotlin
package com.KediTV.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.KediTV.R
import com.KediTV.domain.model.ContentType
import com.KediTV.ui.common.ContentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMobileContent(
    state: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String, Int) -> Unit,
    onContinueClick: (String, Int, Int?) -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KediTV", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Featured banner — rotates every 5s via HomeViewModel.rotateFeaturedBanner()
            if (state.featuredItems.isNotEmpty()) {
                item {
                    val featured = state.featuredItems[state.currentFeaturedIndex]
                    AsyncImage(
                        model = featured.posterUrl,
                        contentDescription = featured.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onItemClick(featured.type.name, featured.streamId) },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Continue watching
            if (state.continueWatching.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.continue_watching))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.continueWatching) { item ->
                            ContentCard(
                                name = item.episodeTitle ?: item.name,
                                posterUrl = item.posterUrl,
                                onClick = { onContinueClick(item.type.name, item.streamId, item.episodeId) }
                            )
                        }
                    }
                }
            }

            // Favorites
            if (state.favorites.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.favorites))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.favorites) { item ->
                            ContentCard(
                                name = item.name,
                                posterUrl = item.posterUrl,
                                onClick = { onItemClick(item.type.name, item.streamId) }
                            )
                        }
                    }
                }
            }

            // Category buttons
            item {
                SectionHeader("Kategoriler")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryButton(stringResource(R.string.live_tv), Modifier.weight(1f)) { onCategoryClick(ContentType.LIVE.name) }
                    CategoryButton(stringResource(R.string.movies), Modifier.weight(1f)) { onCategoryClick(ContentType.VOD.name) }
                    CategoryButton(stringResource(R.string.series), Modifier.weight(1f)) { onCategoryClick(ContentType.SERIES.name) }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun CategoryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
```

- [ ] **Step 5: Create `HomeTvContent.kt`**

```kotlin
package com.KediTV.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import com.KediTV.R
import com.KediTV.domain.model.ContentType
import com.KediTV.ui.common.ContentCard

// NOTE: Use androidx.compose.material3.Text throughout — NOT androidx.tv.material3.Text.
// The two have incompatible TextStyle types. Keep all Text/MaterialTheme imports from compose.material3.

@Composable
fun HomeTvContent(
    state: HomeUiState,
    onCategoryClick: (String) -> Unit,
    onItemClick: (String, Int) -> Unit,
    onContinueClick: (String, Int, Int?) -> Unit,
    onSettingsClick: () -> Unit
) {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize().padding(start = 48.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Continue watching
        if (state.continueWatching.isNotEmpty()) {
            item {
                Text(stringResource(R.string.continue_watching), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.continueWatching) { item ->
                        ContentCard(
                            name = item.episodeTitle ?: item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onContinueClick(item.type.name, item.streamId, item.episodeId) }
                        )
                    }
                }
            }
        }

        // Favorites
        if (state.favorites.isNotEmpty()) {
            item {
                Text(stringResource(R.string.favorites), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.favorites) { item ->
                        ContentCard(
                            name = item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onItemClick(item.type.name, item.streamId) }
                        )
                    }
                }
            }
        }

        // Categories row
        item {
            Text("Kategoriler", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            TvLazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    ContentCard(name = stringResource(R.string.live_tv), posterUrl = null, onClick = { onCategoryClick(ContentType.LIVE.name) })
                }
                item {
                    ContentCard(name = stringResource(R.string.movies), posterUrl = null, onClick = { onCategoryClick(ContentType.VOD.name) })
                }
                item {
                    ContentCard(name = stringResource(R.string.series), posterUrl = null, onClick = { onCategoryClick(ContentType.SERIES.name) })
                }
            }
        }
    }
}
```

- [ ] **Step 6: Wire HomeScreen into NavHost**

Update the `composable(NavRoutes.HOME)` block in `KediNavHost.kt`:

```kotlin
composable(NavRoutes.HOME) {
    HomeScreen(
        onNavigateToCategory = { type -> navController.navigate(NavRoutes.category(type)) },
        onNavigateToDetail = { type, id -> navController.navigate(NavRoutes.detail(type, id)) },
        onNavigateToPlayer = { type, id, epId -> navController.navigate(NavRoutes.player(type, id, epId)) },
        onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
    )
}
```

- [ ] **Step 7: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/KediTV/ui/home/ app/src/main/java/com/KediTV/domain/usecase/GetContinueWatchingUseCase.kt app/src/main/java/com/KediTV/ui/navigation/
git commit -m "feat: home screen with continue watching, favorites, and categories"
```

---

## Task 9: Category & Content List Screens

**Files:**
- Create: `app/src/main/java/com/KediTV/domain/usecase/GetCategoriesUseCase.kt`
- Create: `app/src/main/java/com/KediTV/domain/usecase/GetContentListUseCase.kt`
- Create: `app/src/main/java/com/KediTV/ui/category/CategoryViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/category/CategoryScreen.kt`
- Create: `app/src/main/java/com/KediTV/ui/content/ContentListViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/content/ContentListScreen.kt`
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`
- Test: `app/src/test/java/com/KediTV/domain/usecase/GetCategoriesUseCaseTest.kt`

- [ ] **Step 1: Write GetCategoriesUseCase test**

```kotlin
package com.KediTV.domain.usecase

import com.KediTV.domain.model.Category
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.repository.ContentRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class GetCategoriesUseCaseTest {
    private val repo = mockk<ContentRepository>()
    private val useCase = GetCategoriesUseCase(repo)

    @Test
    fun `returns categories for content type`() = runTest {
        coEvery { repo.getCategories(ContentType.LIVE) } returns Result.success(
            listOf(Category("1", "Ulusal", ContentType.LIVE))
        )

        val result = useCase(ContentType.LIVE)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }
}
```

- [ ] **Step 2: Create use cases**

```kotlin
// GetCategoriesUseCase.kt
package com.KediTV.domain.usecase

import com.KediTV.domain.model.Category
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.repository.ContentRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType): Result<List<Category>> =
        repository.getCategories(type)
}

// GetContentListUseCase.kt
package com.KediTV.domain.usecase

import com.KediTV.domain.model.ContentItem
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.repository.ContentRepository
import javax.inject.Inject

class GetContentListUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType, categoryId: String): Result<List<ContentItem>> =
        repository.getContentList(type, categoryId)
}
```

- [ ] **Step 3: Run test**

Run: `./gradlew test --tests "com.KediTV.domain.usecase.GetCategoriesUseCaseTest"`
Expected: PASSED

- [ ] **Step 4: Create `CategoryViewModel.kt` and `CategoryScreen.kt`**

```kotlin
// CategoryViewModel.kt
package com.KediTV.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.model.Category
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val contentType: ContentType = ContentType.LIVE
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val _state = MutableStateFlow(CategoryUiState(contentType = type))
    val state: StateFlow<CategoryUiState> = _state

    init { loadCategories() }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getCategoriesUseCase(type).fold(
                onSuccess = { _state.value = _state.value.copy(isLoading = false, categories = it, error = null) },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun retry() = loadCategories()
}

// CategoryScreen.kt
package com.KediTV.ui.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.KediTV.domain.model.ContentType
import com.KediTV.ui.common.EmptyState
import com.KediTV.ui.common.ErrorState
import com.KediTV.ui.common.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onNavigateToContent: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val title = when (state.contentType) {
        ContentType.LIVE -> "Canli TV"
        ContentType.VOD -> "Filmler"
        ContentType.SERIES -> "Diziler"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            state.categories.isEmpty() -> EmptyState("Kategori bulunamadi")
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.categories) { category ->
                        Card(
                            modifier = Modifier
                                .height(80.dp)
                                .clickable { onNavigateToContent(state.contentType.name, category.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 5: Create `ContentListViewModel.kt` and `ContentListScreen.kt`**

```kotlin
// ContentListViewModel.kt
package com.KediTV.ui.content

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.model.ContentItem
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.usecase.GetContentListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<ContentItem> = emptyList(),
    val filteredItems: List<ContentItem> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class ContentListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContentListUseCase: GetContentListUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val categoryId = savedStateHandle.get<String>("categoryId")!!

    private val _state = MutableStateFlow(ContentListUiState())
    val state: StateFlow<ContentListUiState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getContentListUseCase(type, categoryId).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false, items = it, filteredItems = it, error = null)
                },
                onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun search(query: String) {
        val filtered = if (query.isBlank()) _state.value.items
        else _state.value.items.filter { it.name.contains(query, ignoreCase = true) }
        _state.value = _state.value.copy(searchQuery = query, filteredItems = filtered)
    }

    fun retry() = load()
}

// ContentListScreen.kt
package com.KediTV.ui.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.KediTV.R
import com.KediTV.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentListScreen(
    onNavigateToDetail: (String, Int) -> Unit,
    onNavigateToPlayer: (String, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: ContentListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::search,
                        placeholder = { Text(stringResource(R.string.search_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            state.filteredItems.isEmpty() -> EmptyState(stringResource(R.string.no_content))
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredItems) { item ->
                        ContentCard(
                            name = item.name,
                            posterUrl = item.posterUrl,
                            onClick = { onNavigateToDetail(item.type.name, item.streamId) }
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 6: Wire screens into NavHost**

Update Category and Content routes in `KediNavHost.kt`:

```kotlin
composable(NavRoutes.CATEGORY, arguments = listOf(navArgument("type") { type = NavType.StringType })) {
    CategoryScreen(
        onNavigateToContent = { t, cId -> navController.navigate(NavRoutes.content(t, cId)) },
        onBack = { navController.popBackStack() }
    )
}
composable(
    NavRoutes.CONTENT,
    arguments = listOf(
        navArgument("type") { type = NavType.StringType },
        navArgument("categoryId") { type = NavType.StringType }
    )
) {
    val contentType = it.arguments?.getString("type") ?: ""
    ContentListScreen(
        onNavigateToDetail = { t, id -> navController.navigate(NavRoutes.detail(t, id)) },
        onNavigateToPlayer = { t, id -> navController.navigate(NavRoutes.player(t, id)) },
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 7: Verify build and run tests**

Run: `./gradlew test; ./gradlew compileDebugKotlin`
Expected: All PASSED, BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/KediTV/ui/category/ app/src/main/java/com/KediTV/ui/content/ app/src/main/java/com/KediTV/domain/usecase/ app/src/main/java/com/KediTV/ui/navigation/ app/src/test/
git commit -m "feat: category and content list screens with search filtering"
```

---

## Task 10: Detail Screen

**Files:**
- Create: `app/src/main/java/com/KediTV/domain/usecase/GetSeriesInfoUseCase.kt`
- Create: `app/src/main/java/com/KediTV/domain/usecase/ToggleFavoriteUseCase.kt`
- Create: `app/src/main/java/com/KediTV/ui/detail/DetailViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/detail/DetailScreen.kt`
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`

- [ ] **Step 1: Create use cases**

```kotlin
// GetSeriesInfoUseCase.kt
package com.KediTV.domain.usecase

import com.KediTV.domain.model.SeriesInfo
import com.KediTV.domain.repository.ContentRepository
import javax.inject.Inject

class GetSeriesInfoUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(seriesId: Int): Result<SeriesInfo> =
        repository.getSeriesInfo(seriesId)
}

// ToggleFavoriteUseCase.kt
package com.KediTV.domain.usecase

import com.KediTV.domain.model.ContentType
import com.KediTV.domain.model.Favorite
import com.KediTV.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    suspend operator fun invoke(
        streamId: Int,
        type: ContentType,
        name: String,
        posterUrl: String?,
        categoryName: String?
    ): Boolean {
        val isFav = repository.isFavorite(streamId, type)
        if (isFav) {
            repository.removeFavorite(streamId, type)
        } else {
            repository.addFavorite(Favorite(streamId, type, name, posterUrl, categoryName))
        }
        return !isFav
    }
}
```

- [ ] **Step 2: Create `DetailViewModel.kt`**

```kotlin
package com.KediTV.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.model.*
import com.KediTV.domain.repository.ContentRepository
import com.KediTV.domain.repository.FavoriteRepository
import com.KediTV.domain.repository.WatchHistoryRepository
import com.KediTV.domain.usecase.GetSeriesInfoUseCase
import com.KediTV.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val contentType: ContentType = ContentType.VOD,
    val streamId: Int = 0,
    val name: String = "",
    val posterUrl: String? = null,
    val plot: String? = null,
    val rating: String? = null,
    val isFavorite: Boolean = false,
    val seriesInfo: SeriesInfo? = null,
    val selectedSeason: Int = 1,
    val watchHistory: WatchHistory? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val getSeriesInfoUseCase: GetSeriesInfoUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val streamId = savedStateHandle.get<Int>("streamId")!!
    // name and posterUrl are passed via route args for VOD/LIVE (no extra API call needed)
    private val initialName = savedStateHandle.get<String>("name") ?: ""
    private val initialPosterUrl = savedStateHandle.get<String>("posterUrl")?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(
        DetailUiState(contentType = type, streamId = streamId, name = initialName, posterUrl = initialPosterUrl)
    )
    val state: StateFlow<DetailUiState> = _state

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val isFav = favoriteRepository.isFavorite(streamId, type)
            val history = watchHistoryRepository.getProgress(streamId)

            if (type == ContentType.SERIES) {
                getSeriesInfoUseCase(streamId).fold(
                    onSuccess = { info ->
                        _state.value = _state.value.copy(
                            isLoading = false, name = info.name, posterUrl = info.posterUrl,
                            plot = info.plot, rating = info.rating, seriesInfo = info,
                            isFavorite = isFav, watchHistory = history, error = null
                        )
                    },
                    onFailure = { _state.value = _state.value.copy(isLoading = false, error = it.message) }
                )
            } else {
                // VOD/LIVE: name & posterUrl already set from route args
                _state.value = _state.value.copy(
                    isLoading = false, isFavorite = isFav, watchHistory = history, error = null
                )
            }
        }
    }

    fun selectSeason(season: Int) {
        _state.value = _state.value.copy(selectedSeason = season)
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val newState = toggleFavoriteUseCase(
                streamId, type, _state.value.name, _state.value.posterUrl, null
            )
            _state.value = _state.value.copy(isFavorite = newState)
        }
    }

    fun getStreamUrl(): String = contentRepository.buildStreamUrl(type, streamId)
    fun getEpisodeUrl(episodeId: Int): String = contentRepository.buildEpisodeUrl(episodeId)

    fun retry() = load()
}
```

- [ ] **Step 3: Create `DetailScreen.kt`**

```kotlin
package com.KediTV.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.KediTV.R
import com.KediTV.domain.model.ContentType
import com.KediTV.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onPlay: (String, Int, Int?) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") } },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (state.isFavorite) stringResource(R.string.remove_favorite) else stringResource(R.string.add_favorite),
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingIndicator()
            state.error != null -> ErrorState(state.error!!, viewModel::retry)
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Poster
                    item {
                        AsyncImage(
                            model = state.posterUrl,
                            contentDescription = state.name,
                            modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Plot
                    state.plot?.let { plot ->
                        item {
                            Text(plot, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                        }
                    }

                    // Play button (VOD/LIVE)
                    if (state.contentType != ContentType.SERIES) {
                        item {
                            val hasProgress = state.watchHistory != null && state.watchHistory!!.positionMs > 0
                            Button(
                                onClick = { onPlay(state.contentType.name, state.streamId, null) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (hasProgress) stringResource(R.string.continue_play) else stringResource(R.string.play))
                            }
                        }
                    }

                    // Series: season selector + episodes
                    state.seriesInfo?.let { info ->
                        if (info.seasons.isNotEmpty()) {
                            item {
                                ScrollableTabRow(selectedTabIndex = state.selectedSeason - 1) {
                                    info.seasons.forEach { season ->
                                        Tab(
                                            selected = state.selectedSeason == season.seasonNumber,
                                            onClick = { viewModel.selectSeason(season.seasonNumber) },
                                            text = { Text("Sezon ${season.seasonNumber}") }
                                        )
                                    }
                                }
                            }

                            val currentSeason = info.seasons.find { it.seasonNumber == state.selectedSeason }
                            currentSeason?.episodes?.let { episodes ->
                                items(episodes) { episode ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onPlay(ContentType.SERIES.name, state.streamId, episode.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${episode.episodeNumber}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(40.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(episode.title, style = MaterialTheme.typography.titleMedium)
                                                episode.duration?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Wire DetailScreen into NavHost**

```kotlin
composable(
    NavRoutes.DETAIL,
    arguments = listOf(
        navArgument("type") { type = NavType.StringType },
        navArgument("streamId") { type = NavType.IntType }
    )
) {
    val contentType = it.arguments?.getString("type") ?: ""
    val streamId = it.arguments?.getInt("streamId") ?: 0
    DetailScreen(
        onPlay = { t, id, epId -> navController.navigate(NavRoutes.player(t, id, epId)) },
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 5: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/KediTV/ui/detail/ app/src/main/java/com/KediTV/domain/usecase/ app/src/main/java/com/KediTV/ui/navigation/
git commit -m "feat: detail screen with series episodes and favorites"
```

---

## Task 11: Player Screen

**Files:**
- Create: `app/src/main/java/com/KediTV/player/KediPlayer.kt`
- Create: `app/src/main/java/com/KediTV/domain/usecase/SaveWatchProgressUseCase.kt`
- Create: `app/src/main/java/com/KediTV/ui/player/PlayerViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/player/PlayerScreen.kt`
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`

- [ ] **Step 1: Create `KediPlayer.kt` (Media3 wrapper)**

```kotlin
package com.KediTV.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl

object KediPlayer {

    fun create(context: Context): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2_000,   // minBufferMs
                30_000,  // maxBufferMs
                1_500,   // bufferForPlaybackMs
                3_000    // bufferForPlaybackAfterRebufferMs
            )
            .build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
    }

    fun play(player: ExoPlayer, url: String, startPositionMs: Long = 0) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.play()
    }
}
```

- [ ] **Step 2: Create `SaveWatchProgressUseCase.kt`**

```kotlin
package com.KediTV.domain.usecase

import com.KediTV.domain.model.ContentType
import com.KediTV.domain.model.WatchHistory
import com.KediTV.domain.repository.WatchHistoryRepository
import javax.inject.Inject

class SaveWatchProgressUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(
        streamId: Int,
        type: ContentType,
        name: String,
        posterUrl: String?,
        positionMs: Long,
        durationMs: Long,
        episodeId: Int? = null,
        episodeTitle: String? = null
    ) {
        repository.saveProgress(
            WatchHistory(
                streamId = streamId,
                type = type,
                name = name,
                posterUrl = posterUrl,
                positionMs = positionMs,
                durationMs = durationMs,
                lastWatched = System.currentTimeMillis(),
                episodeId = episodeId,
                episodeTitle = episodeTitle
            )
        )
    }
}
```

- [ ] **Step 3: Create `PlayerViewModel.kt`**

```kotlin
package com.KediTV.ui.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.model.ContentType
import com.KediTV.domain.repository.ContentRepository
import com.KediTV.domain.repository.WatchHistoryRepository
import com.KediTV.domain.usecase.SaveWatchProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val streamUrl: String = "",
    val startPositionMs: Long = 0,
    val contentType: ContentType = ContentType.LIVE,
    val streamId: Int = 0,
    val episodeId: Int? = null,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val saveProgressUseCase: SaveWatchProgressUseCase
) : ViewModel() {

    private val type = ContentType.valueOf(savedStateHandle.get<String>("type")!!)
    private val streamId = savedStateHandle.get<Int>("streamId")!!
    private val episodeId = savedStateHandle.get<Int>("episodeId")?.takeIf { it != -1 }

    private val _state = MutableStateFlow(PlayerUiState(contentType = type, streamId = streamId, episodeId = episodeId))
    val state: StateFlow<PlayerUiState> = _state

    init { loadStreamUrl() }

    private fun loadStreamUrl() {
        viewModelScope.launch {
            val url = if (episodeId != null) {
                contentRepository.buildEpisodeUrl(episodeId)
            } else {
                contentRepository.buildStreamUrl(type, streamId)
            }

            val history = watchHistoryRepository.getProgress(streamId, episodeId)
            _state.value = _state.value.copy(
                streamUrl = url,
                startPositionMs = history?.positionMs ?: 0
            )
        }
    }

    fun saveProgress(positionMs: Long, durationMs: Long, name: String, posterUrl: String?) {
        viewModelScope.launch {
            saveProgressUseCase(
                streamId = streamId,
                type = type,
                name = name,
                posterUrl = posterUrl,
                positionMs = positionMs,
                durationMs = durationMs,
                episodeId = episodeId
            )
        }
    }
}
```

- [ ] **Step 4: Create `PlayerScreen.kt`**

```kotlin
package com.KediTV.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.KediTV.player.KediPlayer

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val player = remember {
        KediPlayer.create(context)
    }

    // Lock to landscape for mobile
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val pos = player.currentPosition
            val dur = player.duration
            if (dur > 0) {
                viewModel.saveProgress(pos, dur, "", null)
            }
            player.release()
        }
    }

    // Start playback when URL is ready
    LaunchedEffect(state.streamUrl) {
        if (state.streamUrl.isNotEmpty()) {
            KediPlayer.play(player, state.streamUrl, state.startPositionMs)
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                this.player = player
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = Modifier.fillMaxSize().background(Color.Black)
    )
}
```

- [ ] **Step 5: Wire PlayerScreen into NavHost**

```kotlin
composable(
    NavRoutes.PLAYER,
    arguments = listOf(
        navArgument("type") { type = NavType.StringType },
        navArgument("streamId") { type = NavType.IntType },
        navArgument("episodeId") { type = NavType.IntType; defaultValue = -1 }
    )
) {
    PlayerScreen(onBack = { navController.popBackStack() })
}
```

- [ ] **Step 6: Verify build**

Run: `./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/KediTV/player/ app/src/main/java/com/KediTV/ui/player/ app/src/main/java/com/KediTV/domain/usecase/SaveWatchProgressUseCase.kt app/src/main/java/com/KediTV/ui/navigation/
git commit -m "feat: player screen with Media3, resume playback, and progress saving"
```

---

## Task 12: Settings Screen

**Files:**
- Create: `app/src/main/java/com/KediTV/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/KediTV/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt`

- [ ] **Step 1: Create `SettingsViewModel.kt`**

```kotlin
package com.KediTV.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KediTV.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BufferSize(val label: String, val minBufferMs: Int, val maxBufferMs: Int) {
    LOW("Dusuk", 5_000, 15_000),
    MEDIUM("Orta", 10_000, 30_000),
    HIGH("Yuksek", 20_000, 60_000)
}

data class SettingsUiState(
    val isLoggedOut: Boolean = false,
    val bufferSize: BufferSize = BufferSize.MEDIUM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    fun setBufferSize(size: BufferSize) {
        _state.value = _state.value.copy(bufferSize = size)
        // KediPlayer reads this via PlayerViewModel before creating ExoPlayer
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}
```

- [ ] **Step 2: Create `SettingsScreen.kt`**

```kotlin
package com.KediTV.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.KediTV.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("KediTV v1.0.0", style = MaterialTheme.typography.titleLarge)

            HorizontalDivider()

            // Buffer size setting
            Text(stringResource(R.string.buffer_size), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BufferSize.entries.forEach { size ->
                    FilterChip(
                        selected = state.bufferSize == size,
                        onClick = { viewModel.setBufferSize(size) },
                        label = { Text(size.label) }
                    )
                }
            }

            HorizontalDivider()

            Button(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.logout))
            }
        }
    }
}
```

- [ ] **Step 3: Wire SettingsScreen into NavHost**

```kotlin
composable(NavRoutes.SETTINGS) {
    SettingsScreen(
        onLogout = {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        },
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 4: Verify full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/KediTV/ui/settings/ app/src/main/java/com/KediTV/ui/navigation/
git commit -m "feat: settings screen with logout"
```

---

## Task 13: Final Integration & Polish

**Files:**
- Modify: `app/src/main/java/com/KediTV/ui/navigation/KediNavHost.kt` (ensure all routes complete)
- Create: `app/src/main/java/com/KediTV/util/Extensions.kt`
- Modify: `app/src/main/res/values/strings.xml` (if needed)

- [ ] **Step 1: Create `Extensions.kt`**

```kotlin
package com.KediTV.util

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
```

- [ ] **Step 2: Verify all NavHost routes are wired**

Read `KediNavHost.kt` and confirm all composable routes call their screens.

- [ ] **Step 3: Run full test suite**

Run: `./gradlew test`
Expected: All tests PASSED

- [ ] **Step 4: Build debug APK**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL, APK at `app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: final integration and utilities"
```

---

## Summary

| Task | Description | Key Files |
|------|-------------|-----------|
| 1 | Project scaffolding | Gradle, Manifest, App, Activity |
| 2 | Domain models & interfaces | domain/model/*, domain/repository/* |
| 3 | Room DB & DAOs | data/db/* |
| 4 | Xtream API service | data/api/* |
| 5 | Repositories & DI | data/repository/*, di/* |
| 6 | Theme, common UI, navigation | ui/theme/*, ui/common/*, ui/navigation/* |
| 7 | Login screen | ui/login/* |
| 8 | Home screen | ui/home/* |
| 9 | Category & content list | ui/category/*, ui/content/* |
| 10 | Detail screen | ui/detail/* |
| 11 | Player screen | player/*, ui/player/* |
| 12 | Settings screen | ui/settings/* |
| 13 | Final integration | util/*, final wiring |
