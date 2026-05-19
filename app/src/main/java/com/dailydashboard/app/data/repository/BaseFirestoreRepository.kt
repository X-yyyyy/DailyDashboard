package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.FirestoreDoc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

abstract class BaseFirestoreRepository<T>(
    protected val firestoreClient: FirebaseFirestoreClient,
    protected val collectionName: String,
) {
    protected val json = Json { ignoreUnknownKeys = true }

    protected val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> get() = _items.asStateFlow()

    protected abstract fun mapDocument(doc: FirestoreDoc): T

    suspend fun refresh(userId: String, idToken: String) {
        try {
            val docs = firestoreClient.listDocuments(userId, collectionName, idToken)
            _items.value = docs.map { mapDocument(it) }
        } catch (_: Exception) {
            // Network error — keep existing data
        }
    }
}
