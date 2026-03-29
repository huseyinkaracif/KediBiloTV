package com.kedibilotv.data.repository

import com.kedibilotv.data.api.XtreamApiService
import com.kedibilotv.data.api.model.CategoryDto
import com.kedibilotv.domain.model.ContentType
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
