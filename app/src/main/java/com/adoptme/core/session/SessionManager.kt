package com.adoptme.core.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

data class UserSession(
    val userId: String?,
    val userName: String?,
    val userEmail: String?,
    val countryCode: String = "CL"
)

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_COUNTRY_CODE = stringPreferencesKey("country_code")
    }

    val userSessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
        UserSession(
            userId = prefs[KEY_USER_ID],
            userName = prefs[KEY_USER_NAME],
            userEmail = prefs[KEY_USER_EMAIL],
            countryCode = prefs[KEY_COUNTRY_CODE] ?: "CL"
        )
    }

    suspend fun saveSession(userId: String, name: String, email: String, countryCode: String = "CL") {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_COUNTRY_CODE] = countryCode
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
