package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.model.Course
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.FirestoreDoc
import com.dailydashboard.app.data.remote.firebase.toData

class CourseRepository(
    firestoreClient: FirebaseFirestoreClient,
) : BaseFirestoreRepository<Course>(firestoreClient, "courses") {
    override fun mapDocument(doc: FirestoreDoc): Course {
        return doc.toData<Course>(json).copy(id = doc.id)
    }
}
