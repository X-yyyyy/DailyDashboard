package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.model.TodoItem
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.FirestoreDoc
import com.dailydashboard.app.data.remote.firebase.toData

class TodoRepository(
    firestoreClient: FirebaseFirestoreClient,
) : BaseFirestoreRepository<TodoItem>(firestoreClient, "todos") {
    override fun mapDocument(doc: FirestoreDoc): TodoItem {
        return doc.toData<TodoItem>(json).copy(id = doc.id)
    }
}
