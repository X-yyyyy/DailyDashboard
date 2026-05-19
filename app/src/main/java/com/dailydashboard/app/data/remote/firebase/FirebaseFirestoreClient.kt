package com.dailydashboard.app.data.remote.firebase

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import com.dailydashboard.app.util.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FirebaseFirestoreClient(
    private val okHttpClient: OkHttpClient,
    private val tokenProvider: suspend () -> String?,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    private suspend fun authHeader(): String {
        val token = tokenProvider()
        return "Bearer $token"
    }

    suspend fun getDocument(path: String): Result<JsonElement> {
        val url = "${FirebaseConfig.firestoreBaseUrl}/$path"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", authHeader())
            .get()
            .build()

        return execute(request)
    }

    suspend fun listCollection(collectionPath: String): Result<JsonElement> {
        val url = "${FirebaseConfig.firestoreBaseUrl}/$collectionPath"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", authHeader())
            .get()
            .build()

        return execute(request)
    }

    suspend fun createDocument(
        collectionPath: String,
        data: JsonElement,
    ): Result<JsonElement> {
        val url = "${FirebaseConfig.firestoreBaseUrl}/$collectionPath"
        val body = mapOf("fields" to data)
        val requestBody = json.encodeToString(JsonElement.serializer(), data)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", authHeader())
            .post(requestBody.toRequestBody(mediaType))
            .build()

        return execute(request)
    }

    suspend fun updateDocument(
        path: String,
        data: JsonElement,
    ): Result<JsonElement> {
        val url = "${FirebaseConfig.firestoreBaseUrl}/$path"
        val requestBody = json.encodeToString(JsonElement.serializer(), data)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", authHeader())
            .patch(requestBody.toRequestBody(mediaType))
            .build()

        return execute(request)
    }

    suspend fun deleteDocument(path: String): Result<Unit> {
        val url = "${FirebaseConfig.firestoreBaseUrl}/$path"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", authHeader())
            .delete()
            .build()

        return try {
            val response = okHttpClient.newCall(request).await()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun execute(request: Request): Result<JsonElement> {
        return try {
            val response = okHttpClient.newCall(request).await()
            val responseBody = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success(json.parseToJsonElement(responseBody))
            } else {
                Result.failure(Exception("Request failed: ${response.code} - $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
