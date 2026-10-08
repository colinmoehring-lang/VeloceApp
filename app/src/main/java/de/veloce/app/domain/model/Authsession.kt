package de.veloce.app.domain.model
 
import java.util.UUID
 
data class AuthSession(
    val accessToken: String,
    val expiresInSeconds: Int,
    val userId: UUID,
    val userName: String,
)
