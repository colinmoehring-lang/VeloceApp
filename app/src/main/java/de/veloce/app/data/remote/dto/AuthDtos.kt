package de.veloce.app.data.remote.dto
 
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SignUpRequestDto(
    @SerialName("email")
    val email: String,
    val password: String,
)
 
@Serializable
data class LogInRequestDto(
    @SerialName("email")
    val email: String,
    val password: String,
)

@Serializable
data class VerifyEmailRequestDto(
    @SerialName("email")
    val email: String,
    val code: String,
)
 
@Serializable
data class GenericMessageResponseDto(
    val success: Boolean,
    val message: String = "",
)

@Serializable
data class AuthResponseDto(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("tokenType")
    val tokenType: String = "Bearer",
    @SerialName("expiresIn")
    val expiresIn: Int,
    @SerialName("userId")
    val userId: String,
    @SerialName("email")
    val email: String,
)
