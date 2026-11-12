package de.veloce.app.data.remote

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

class ApiException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)

internal fun HttpException.toApiException(): ApiException {
    val raw = response()?.errorBody()?.string().orEmpty()
    return ApiException(code(), raw.apiMessage().ifBlank { message() })
}

private fun String.apiMessage(): String {
    if (isBlank()) return ""
    val element = try {
        Json.parseToJsonElement(this)
    } catch (_: SerializationException) {
        return trim().trim('"')
    }
    return when (element) {
        is JsonPrimitive -> element.content
        is JsonObject -> element["errors"]?.firstErrorMessage()
            ?.takeIf { it.isNotBlank() }
            ?: sequenceOf("detail", "message", "title")
                .mapNotNull { (element[it] as? JsonPrimitive)?.content }
                .firstOrNull { it.isNotBlank() }
                .orEmpty()
        else -> ""
    }
}

private fun kotlinx.serialization.json.JsonElement.firstErrorMessage(): String? = when (this) {
    is JsonPrimitive -> content.takeIf { isString && it.isNotBlank() }
    is JsonArray -> firstNotNullOfOrNull { it.firstErrorMessage() }
    is JsonObject -> values.firstNotNullOfOrNull { it.firstErrorMessage() }
}
