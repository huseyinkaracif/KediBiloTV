package com.kedibilotv.data.repository

import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.data.api.model.*
import com.kedibilotv.data.db.dao.ServerConfigDao
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
