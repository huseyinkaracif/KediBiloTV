package com.kedibilotv.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kedibilotv.data.db.dao.FavoriteDao
import com.kedibilotv.data.db.dao.WatchHistoryDao
import com.kedibilotv.data.db.entity.FavoriteEntity
import com.kedibilotv.data.db.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {
    private lateinit var db: KediBiloDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var watchHistoryDao: WatchHistoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, KediBiloDatabase::class.java).build()
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
