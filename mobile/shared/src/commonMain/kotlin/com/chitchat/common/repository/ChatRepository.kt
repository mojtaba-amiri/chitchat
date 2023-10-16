package com.chitchat.common.repository

import com.chitchat.common.ANSWER_ENDPOINT
import com.chitchat.common.BASE_URL
import com.chitchat.common.REGISTER_ENDPOINT
import com.chitchat.common.SUMMARIZE_ENDPOINT
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.settings
import com.chitchat.common.ui.screens.conversation.RegisterDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AnswerRequestDto(
    val text: String
)
class ChatRepository {
    private var token : String = settings.getString("Token", "")
    val httpClient: HttpClient = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.e(tag = "HTTP Client", message = message)
                }
            }
        }
        this.install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 60000
            requestTimeoutMillis = 60000
        }
    }

    fun setAuthToken(token: String) {
        this.token = token
        settings.putString("Token", token)
    }

    suspend fun refreshToken(userId: String): HttpResponse {
        try {
            val request = RegisterDto(userId,  reqPlatform = "Android")
            return httpClient.post("$BASE_URL$REGISTER_ENDPOINT") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun summarize(): HttpResponse {
        try {
         return httpClient.get("${BASE_URL}$SUMMARIZE_ENDPOINT") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun answer(messages: List<ChatMessage>): HttpResponse? {
        if (token.isEmpty()) return null
        token.let {
            try {
                return httpClient.post("${BASE_URL}$ANSWER_ENDPOINT") {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(AnswerRequestDto(messages.map { it.message }.joinToString("\n")))
                }
            } catch (e: Exception) {
                Napier.e(message = e.message.toString())
                throw e
            }
        }
    }

    fun hasToken() = token.isNotEmpty()
}