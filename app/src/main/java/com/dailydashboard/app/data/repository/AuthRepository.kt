package com.dailydashboard.app.data.repository

import com.dailydashboard.app.data.local.DataStoreManager
import com.dailydashboard.app.data.remote.firebase.FirebaseAuthClient
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val authClient: FirebaseAuthClient,
    private val dataStoreManager: DataStoreManager,
) {
    data class AuthState(
        val isLoggedIn: Boolean = false,
        val idToken: String? = null,
        val localId: String? = null,
        val email: String? = null,
    )

    suspend fun login(email: String, password: String): Result<AuthState> {
        val result = authClient.signInWithPassword(email, password)
        return result.map { response ->
            dataStoreManager.saveAuthToken(response.idToken, response.refreshToken, response.localId)
            AuthState(
                isLoggedIn = true,
                idToken = response.idToken,
                localId = response.localId,
                email = response.email,
            )
        }
    }

    suspend fun restoreSession(): AuthState? {
        val (idToken, _, localId) = dataStoreManager.getTokens()
        if (idToken != null && localId != null) {
            return AuthState(isLoggedIn = true, idToken = idToken, localId = localId)
        }
        return null
    }

    suspend fun logout() {
        dataStoreManager.clearAuth()
    }

    fun observeIdToken(): Flow<String?> = dataStoreManager.observeIdToken()
}
