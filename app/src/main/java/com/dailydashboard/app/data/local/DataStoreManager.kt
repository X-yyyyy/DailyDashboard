package com.dailydashboard.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("daily_dashboard")

class DataStoreManager(private val context: Context) {

    private object Keys {
        val ID_TOKEN = stringPreferencesKey("id_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val LOCAL_ID = stringPreferencesKey("local_id")
    }

    suspend fun saveAuthToken(idToken: String, refreshToken: String, localId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ID_TOKEN] = idToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.LOCAL_ID] = localId
        }
    }

    suspend fun getTokens(): Triple<String?, String?, String?> {
        val prefs = context.dataStore.data
        var idToken: String? = null
        var refreshToken: String? = null
        var localId: String? = null
        prefs.collect { p ->
            idToken = p[Keys.ID_TOKEN]
            refreshToken = p[Keys.REFRESH_TOKEN]
            localId = p[Keys.LOCAL_ID]
        }
        return Triple(idToken, refreshToken, localId)
    }

    fun observeIdToken(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.ID_TOKEN]
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ID_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.LOCAL_ID)
        }
    }

    suspend fun cacheCollection(collection: String, jsonString: String) {
        val key = stringPreferencesKey("cache_$collection")
        context.dataStore.edit { prefs ->
            prefs[key] = jsonString
        }
    }

    suspend fun getCachedCollection(collection: String): String? {
        val key = stringPreferencesKey("cache_$collection")
        val prefs = context.dataStore.data
        var result: String? = null
        prefs.collect { p ->
            result = p[key]
        }
        return result
    }
}
