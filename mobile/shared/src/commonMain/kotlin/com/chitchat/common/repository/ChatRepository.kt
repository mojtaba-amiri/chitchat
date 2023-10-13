package com.chitchat.common.repository

import com.chitchat.common.ANSWER_ENDPOINT
import com.chitchat.common.BASE_URL
import com.chitchat.common.REGISTER_ENDPOINT
import com.chitchat.common.SUMMARIZE_ENDPOINT
import com.chitchat.common.getPlatformName
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.settings
import com.chitchat.common.ui.screens.conversation.RegisterDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
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
        this.install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 60000
            requestTimeoutMillis = 60000
            timeou
        }
    }

    fun setAuthToken(token: String) {
        this.token = token
        settings.putString("Token", token)
    }

    suspend fun refreshToken(userId: String): HttpResponse {
        return httpClient.post("$BASE_URL$REGISTER_ENDPOINT") {
                contentType(ContentType.Application.Json)
                setBody(RegisterDto(userId, "Android"))
            }
    }

    suspend fun summarize(): HttpResponse {
         return httpClient.get("${BASE_URL}$SUMMARIZE_ENDPOINT") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun answer(messages: List<ChatMessage>): HttpResponse? {
        if (token.isEmpty()) return null
        token.let {
            return httpClient.post("${BASE_URL}$ANSWER_ENDPOINT") {
                headers.append(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(AnswerRequestDto(messages.map { it.message }.joinToString ( "\n" )))
            }
        }
    }

    fun hasToken() = token.isNotEmpty()
}