package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.ServerConfig
import com.kedibilotv.domain.repository.AuthRepository
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
