package model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModuleOf


@Serializable
data class VoskResult(
    val partial: String? = "null",
    val text: String? = null

) {
    companion object {
        fun fromJson(json: String) = Json.decodeFromString(VoskResult.serializer(), json)
    }
}
