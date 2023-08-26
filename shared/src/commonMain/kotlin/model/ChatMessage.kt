package model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

@Serializable
data class ChatMessage(
    val id: UUID = UUID.generateUUID(),
    val timeStamp: Long = Clock.System.now().epochSeconds,
    val message: String,
    val user: String
)