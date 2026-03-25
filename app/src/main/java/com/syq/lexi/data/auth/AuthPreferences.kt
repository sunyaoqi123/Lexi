package com.syq.lexi.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthPreferences(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USERNAME_KEY = stringPreferencesKey("username")
    }

    val token: Flow<String?> = context.authDataStore.data.map { it[TOKEN_KEY] }
    val username: Flow<String?> = context.authDataStore.data.map { it[USERNAME_KEY] }

    suspend fun saveAuth(token: String, username: String) {
        context.authDataStore.edit {
            it[TOKEN_KEY] = token
            it[USERNAME_KEY] = username
        }
    }

    suspend fun clearAuth() {
        context.authDataStore.edit { it.clear() }
    }

    fun bearerToken(token: String) = "Bearer $token"
}
