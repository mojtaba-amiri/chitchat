package com.chitchat.common.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

@Serializable
data class ChatMessage(
    val id: UUID = UUID.generateUUID(),
    val timeStamp: Long = Clock.System.now().epochSeconds,
    var message: String,
    val user: String = "",
    val time: String = Clock.System.now().toShortLocalTime(),
    val endTime: String
)

@Serializable
data class Conversation(
    val id: UUID = UUID.generateUUID(),
    val messages: List<ChatMessage> = listOf(),
    val createTime: Long = Clock.System.now().epochSeconds
)

fun LocalTime.shortTime(): String = this.toString().split(".")[0]

fun Instant.toShortLocalTime() = this
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .time.shortTime()



fun Instant.toShortLocalDateTime(removeQuotes: Boolean = false) = this
    .toLocalDateTime(TimeZone.currentSystemDefault()).toString().apply {
        if (removeQuotes) this.replace(":", "")
    }

fun Instant.asFileName(): String {
    val raw = this.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
    return raw.replace("-","_")
        .replace(":", "")
        .split(".")[0]
}