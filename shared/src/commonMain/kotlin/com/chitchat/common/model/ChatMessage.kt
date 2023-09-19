package com.chitchat.common.model

import kotlinx.datetime.Clock
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
    val time: String = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time.shortTime()
)

@Serializable
data class Conversation(
    val id: UUID = UUID.generateUUID(),
    val messages: List<ChatMessage> = listOf(),
    val createTime: Long = Clock.System.now().epochSeconds
)

fun LocalTime.shortTime(): String = this.toString().split(".")[0]