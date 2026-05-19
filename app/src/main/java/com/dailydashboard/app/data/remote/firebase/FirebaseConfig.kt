package com.dailydashboard.app.data.remote.firebase

import com.dailydashboard.app.BuildConfig

object FirebaseConfig {
    val apiKey: String get() = BuildConfig.FIREBASE_API_KEY
    val projectId: String get() = BuildConfig.FIREBASE_PROJECT_ID

    val authBaseUrl: String get() = "https://identitytoolkit.googleapis.com/v1"
    val firestoreBaseUrl: String get() =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"
}
