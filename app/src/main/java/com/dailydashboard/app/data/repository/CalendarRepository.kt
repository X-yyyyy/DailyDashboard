package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.model.CalendarEvent
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.FirestoreDoc
import com.dailydashboard.app.data.remote.firebase.toData

class CalendarRepository(
    firestoreClient: FirebaseFirestoreClient,
) : BaseFirestoreRepository<CalendarEvent>(firestoreClient, "calendar") {
    override fun mapDocument(doc: FirestoreDoc): CalendarEvent {
        return doc.toData<CalendarEvent>(json).copy(id = doc.id)
    }
}
