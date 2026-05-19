package com.dailydashboard.app.data.remote.firebase

import com.dailydashboard.app.util.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FirebaseFirestoreClient(
    private val okHttpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json".toMediaType()

    private fun collectionPath(userId: String, collection: String) =
        "${FirebaseConfig.firestoreBaseUrl}/users/$userId/$collection"

    private fun documentPath(userId: String, collection: String, docId: String) =
        "${FirebaseConfig.firestoreBaseUrl}/users/$userId/$collection/$docId"

    suspend fun listDocuments(
        userId: String,
        collection: String,
        idToken: String,
    ): List<FirestoreDoc> {
        val url = "${collectionPath(userId, collection)}?pageSize=100"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        val body = response.body?.string() ?: return emptyList()

        if (!response.isSuccessful) throw Exception("Firestore list error: $body")

        val jsonObj = json.parseToJsonElement(body).jsonObject
        val documents = jsonObj["documents"]?.jsonArray ?: return emptyList()

        return documents.map { element ->
            val obj = element.jsonObject
            val name = obj["name"]?.jsonPrimitive?.content ?: ""
            val docId = name.split("/").last()
            val fields = obj["fields"]?.jsonObject ?: JsonObject(emptyMap())
            FirestoreDoc(id = docId, fields = fields, name = name)
        }
    }

    suspend fun getDocument(
        userId: String,
        collection: String,
        docId: String,
        idToken: String,
    ): FirestoreDoc? {
        val url = documentPath(userId, collection, docId)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .get()
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) return null
        val body = response.body?.string() ?: return null
        val obj = json.parseToJsonElement(body).jsonObject
        val name = obj["name"]?.jsonPrimitive?.content ?: ""
        val id = name.split("/").last()
        val fields = obj["fields"]?.jsonObject ?: JsonObject(emptyMap())
        return FirestoreDoc(id = id, fields = fields, name = name)
    }

    suspend fun createDocument(
        userId: String,
        collection: String,
        fields: Map<String, JsonElement>,
        idToken: String,
    ): String {
        val url = "${collectionPath(userId, collection)}?documentId="
        val body = JsonObject(mapOf("fields" to JsonObject(fields)))
        val requestBody = json.encodeToString(JsonObject.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        val responseBody = response.body?.string()
            ?: throw Exception("Empty create response")

        if (!response.isSuccessful) throw Exception("Firestore create error: $responseBody")

        val obj = json.parseToJsonElement(responseBody).jsonObject
        val name = obj["name"]?.jsonPrimitive?.content ?: ""
        return name.split("/").last()
    }

    suspend fun updateDocument(
        userId: String,
        collection: String,
        docId: String,
        fields: Map<String, JsonElement>,
        idToken: String,
    ) {
        val url = "${documentPath(userId, collection, docId)}?updateMask.fieldPaths=" +
                fields.keys.joinToString("&updateMask.fieldPaths=")
        val body = JsonObject(mapOf("fields" to JsonObject(fields)))
        val requestBody = json.encodeToString(JsonObject.serializer(), body)
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .patch(requestBody)
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) {
            throw Exception("Firestore update error: ${response.body?.string()}")
        }
    }

    suspend fun deleteDocument(
        userId: String,
        collection: String,
        docId: String,
        idToken: String,
    ) {
        val url = documentPath(userId, collection, docId)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $idToken")
            .delete()
            .build()

        val response = okHttpClient.newCall(request).await()
        if (!response.isSuccessful) {
            throw Exception("Firestore delete error: ${response.body?.string()}")
        }
    }
}

@Serializable
data class FirestoreDoc(
    val id: String,
    val fields: JsonObject,
    val name: String,
)

inline fun <reified T> FirestoreDoc.toData(json: Json = Json { ignoreUnknownKeys = true }): T {
    val flat = mutableMapOf<String, JsonElement>()
    fields.forEach { (key, value) ->
        val fieldObj = value.jsonObject
        when {
            fieldObj.containsKey("stringValue") -> {
                flat[key] = JsonPrimitive(fieldObj["stringValue"]!!.jsonPrimitive.content)
            }
            fieldObj.containsKey("integerValue") -> {
                flat[key] = JsonPrimitive(fieldObj["integerValue"]!!.jsonPrimitive.content)
            }
            fieldObj.containsKey("booleanValue") -> {
                flat[key] = JsonPrimitive(fieldObj["booleanValue"]!!.jsonPrimitive.content)
            }
            fieldObj.containsKey("arrayValue") -> {
                val arr = fieldObj["arrayValue"]!!.jsonObject["values"]?.jsonArray
                flat[key] = if (arr != null) JsonArray(arr) else JsonArray(emptyList())
            }
            fieldObj.containsKey("timestampValue") -> {
                flat[key] = JsonPrimitive(fieldObj["timestampValue"]!!.jsonPrimitive.content)
            }
        }
    }
    return json.decodeFromJsonElement(JsonObject(flat))
}

inline fun <reified T> T.toFirestoreFields(j: Json = Json { ignoreUnknownKeys = true }): Map<String, JsonElement> {
    val serializer = serializer<T>()
    val jsonElement = j.encodeToJsonElement(serializer, this)
    val obj = jsonElement.jsonObject
    val fields = mutableMapOf<String, JsonElement>()

    obj.forEach { (key, value) ->
        val fieldValue = when (value) {
            is JsonPrimitive -> {
                val prim = value
                when {
                    prim.isString -> JsonObject(mapOf("stringValue" to prim))
                    prim.content == "true" || prim.content == "false" ->
                        JsonObject(mapOf("booleanValue" to prim))
                    else -> JsonObject(mapOf("integerValue" to prim))
                }
            }
            is JsonArray -> {
                val items = value.map { elem ->
                    JsonObject(mapOf("stringValue" to JsonPrimitive(elem.jsonPrimitive.content)))
                }
                JsonObject(mapOf("arrayValue" to JsonObject(mapOf("values" to JsonArray(items)))))
            }
            else -> JsonObject(mapOf("stringValue" to JsonPrimitive(value.toString())))
        }
        fields[key] = fieldValue
    }
    return fields
}
