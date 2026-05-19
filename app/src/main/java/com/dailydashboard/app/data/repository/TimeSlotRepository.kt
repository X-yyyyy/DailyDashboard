package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.model.TimeSlot
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.FirestoreDoc
import com.dailydashboard.app.data.remote.firebase.toData

class TimeSlotRepository(
    firestoreClient: FirebaseFirestoreClient,
) : BaseFirestoreRepository<TimeSlot>(firestoreClient, "timeSlots") {
    override fun mapDocument(doc: FirestoreDoc): TimeSlot {
        return doc.toData<TimeSlot>(json).copy(id = doc.id)
    }
}
