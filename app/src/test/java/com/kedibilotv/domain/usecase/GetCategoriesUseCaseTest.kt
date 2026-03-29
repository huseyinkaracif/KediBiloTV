package com.kedibilotv.domain.usecase

import com.kedibilotv.domain.model.Category
import com.kedibilotv.domain.model.ContentType
import com.kedibilotv.domain.repository.ContentRepository
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
