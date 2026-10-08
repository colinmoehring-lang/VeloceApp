package de.veloce.app.data.remote.dto
 
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequestDto(
    val userName: String,
    val password: String,
)
 
@Serializable
data class LogInRequestDto(
    val userName: String,
    val password: String,
)
 
@Serializable
data class GenericMessageResponseDto(
    val success: Boolean,
    val message: String = "",
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int,
    val userId: String,
    val userName: String,
)
