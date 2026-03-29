package com.kedibilotv.data.api

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
        val service = XtreamApiService(createMockClient("{}"))
        service.configure("http://example.com:8080", "user", "pass")

        assertEquals("http://example.com:8080/live/user/pass/123.ts", service.buildStreamUrl("live", 123))
        assertEquals("http://example.com:8080/movie/user/pass/456.mp4", service.buildVodUrl(456))
    }
}
