package com.veloce.app.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientProvider {
    var baseUrl: String = "http://localhost:5000" // or http://10.0.2.2:5000 for Android
    var authToken: String? = null

    val client: HttpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                url(baseUrl)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                authToken?.let { token ->
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }

    fun buildUrl(path: String): String {
        val cleanPath = path.removePrefix("/")
        return "$baseUrl/api/integration/v1.0/$cleanPath"
    }
}
