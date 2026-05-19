package com.dailydashboard.app.data.remote.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import com.dailydashboard.app.util.await
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    @SerialName("returnSecureToken") val returnSecureToken: Boolean,
)

@Serializable
data class SignInResponse(
    val idToken: String = "",
    val email: String = "",
    val refreshToken: String = "",
    val expiresIn: String = "",
    val localId: String = "",
)

@Serializable
data class ErrorResponse(val error: ErrorDetail)

@Serializable
data class ErrorDetail(val code: Int, val message: String)

class FirebaseAuthClient(private val okHttpClient: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    suspend fun signInWithPassword(email: String, password: String): Result<SignInResponse> {
        val url = "${FirebaseConfig.authBaseUrl}/accounts:signInWithPassword?key=${FirebaseConfig.apiKey}"
        val body = SignInRequest(email, password, returnSecureToken = true)
        val requestBody = json.encodeToString(SignInRequest.serializer(), body)

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody(mediaType))
            .build()

        return try {
            val response = okHttpClient.newCall(request).await()
            val responseBody = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success(json.decodeFromString(SignInResponse.serializer(), responseBody))
            } else {
                val error = json.decodeFromString(ErrorResponse.serializer(), responseBody)
                Result.failure(Exception(error.error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
