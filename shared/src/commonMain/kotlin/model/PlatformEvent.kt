package model

data class PlatformEvent(
    val message: String = "",
    val error: Exception? = null,
    val timeout: Boolean = false
)