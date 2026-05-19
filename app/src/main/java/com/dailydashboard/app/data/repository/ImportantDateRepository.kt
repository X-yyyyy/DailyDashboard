package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.model.ImportantDate
import com.dailydashboard.app.data.remote.firebase.FirebaseFirestoreClient
import com.dailydashboard.app.data.remote.firebase.FirestoreDoc
import com.dailydashboard.app.data.remote.firebase.toData

class ImportantDateRepository(
    firestoreClient: FirebaseFirestoreClient,
) : BaseFirestoreRepository<ImportantDate>(firestoreClient, "importantDates") {
    override fun mapDocument(doc: FirestoreDoc): ImportantDate {
        return doc.toData<ImportantDate>(json).copy(id = doc.id)
    }
}
