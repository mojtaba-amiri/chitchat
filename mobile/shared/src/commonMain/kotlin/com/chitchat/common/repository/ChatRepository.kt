package com.chitchat.common.repository

import com.chitchat.common.ANSWER_ENDPOINT
import com.chitchat.common.BASE_URL
import com.chitchat.common.REGISTER_ENDPOINT
import com.chitchat.common.SUMMARIZE_ENDPOINT
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.model.toShortLocalTime
import com.chitchat.common.ui.screens.conversation.GptAnswer
import com.chitchat.common.ui.screens.conversation.RegisterDto
import com.chitchat.common.ui.screens.conversation.RegisterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.client.utils.EmptyContent.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class AnswerRequestDto(
    val text: String
)
class ChatRepository {
    private var token : String? = null
    val httpClient: HttpClient = HttpClient(CIO) {
        this.install(ContentNegotiation) {
            json()
        }
    }

    fun setAuthToken(token: String) {
        this.token = token
    }

    suspend fun registerUser(userId: String): HttpResponse {

        return httpClient.post("$BASE_URL$REGISTER_ENDPOINT") {
                contentType(ContentType.Application.Json)
                setBody(RegisterDto(userId))
            }
    }

    suspend fun summarize(): HttpResponse {
         return httpClient.get("${BASE_URL}$SUMMARIZE_ENDPOINT") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun answer(messages: List<ChatMessage>): HttpResponse {
        return httpClient.post("${BASE_URL}$ANSWER_ENDPOINT") {
            contentType(ContentType.Application.Json)
            setBody(AnswerRequestDto(messages.map { it.message }.joinToString { "\n" }))
            headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}