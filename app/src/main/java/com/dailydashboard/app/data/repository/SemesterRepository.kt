package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.model.Semester
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.toData
import com.dailydashboard.app.data.remote.firebase.toFirestoreFields
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SemesterRepository(
    private val firestoreClient: FirebaseFirestoreClient,
    private val dataStoreManager: DataStoreManager,
) {
    private val _semester = MutableStateFlow(Semester())
    val semester: Flow<Semester> get() = _semester

    suspend fun refresh(userId: String, idToken: String) {
        val doc = firestoreClient.getDocument(userId, "semester", "current", idToken)
        if (doc != null) {
            _semester.value = doc.toData<Semester>().copy(id = "current")
        }
    }

    suspend fun save(userId: String, idToken: String, semester: Semester) {
        val fields = semester.toFirestoreFields()
        firestoreClient.updateDocument(userId, "semester", "current", fields, idToken)
        _semester.value = semester
    }
}
