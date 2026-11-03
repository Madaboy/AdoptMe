package com.adoptme.core.session

import android.content.Context
import android.util.Base64
import android.util.Patterns
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.dataStore by preferencesDataStore(name = "user_session")

data class UserSession(
    val userId: String?,
    val userName: String?,
    val userEmail: String?,
    val countryCode: String = "CL",
    val onboardingCompleted: Boolean = false
)

data class LocalAccount(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long
)

data class AuthResult(
    val success: Boolean,
    val message: String? = null
)

/**
 * Cuenta local del prototipo. No hay backend todavía; la contraseña se guarda
 * como hash PBKDF2 con sal, nunca en texto plano.
 */
class SessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_COUNTRY_CODE = stringPreferencesKey("country_code")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_ACCOUNTS = stringPreferencesKey("accounts_v1")
        private const val PASSWORD_ITERATIONS = 120_000
    }

    private val gson = Gson()
    private val appContext = context.applicationContext

    val userSessionFlow: Flow<UserSession> = appContext.dataStore.data.map { prefs ->
        UserSession(
            userId = prefs[KEY_USER_ID],
            userName = prefs[KEY_USER_NAME],
            userEmail = prefs[KEY_USER_EMAIL],
            countryCode = prefs[KEY_COUNTRY_CODE] ?: "CL",
            onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): AuthResult {
        val cleanFirstName = firstName.trim()
        val cleanLastName = lastName.trim()
        val cleanEmail = email.trim().lowercase()

        if (cleanFirstName.isBlank() || cleanLastName.isBlank()) {
            return AuthResult(false, "Completa tu nombre y apellido.")
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return AuthResult(false, "Ingresa un correo electrónico válido.")
        }
        if (password.length < 6) {
            return AuthResult(false, "La contraseña debe tener al menos 6 caracteres.")
        }

        val accounts = loadAccounts()
        if (accounts.any { it.email == cleanEmail }) {
            return AuthResult(false, "Ya existe una cuenta con ese correo.")
        }

        return withContext(Dispatchers.Default) {
            val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val account = LocalAccount(
                id = UUID.randomUUID().toString(),
                firstName = cleanFirstName,
                lastName = cleanLastName,
                email = cleanEmail,
                passwordHash = hashPassword(password, saltBytes),
                salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP),
                createdAt = System.currentTimeMillis()
            )

            saveAccounts(accounts + account)
            saveSession(
                userId = account.id,
                name = "$cleanFirstName $cleanLastName",
                email = account.email,
                onboardingCompleted = false
            )
            AuthResult(true)
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        val account = loadAccounts().firstOrNull { it.email == cleanEmail }
            ?: return AuthResult(false, "No encontramos una cuenta con ese correo.")

        val saltBytes = runCatching {
            Base64.decode(account.salt, Base64.NO_WRAP)
        }.getOrNull() ?: return AuthResult(false, "No pudimos validar la cuenta.")

        val matches = withContext(Dispatchers.Default) {
            hashPassword(password, saltBytes) == account.passwordHash
        }
        if (!matches) {
            return AuthResult(false, "La contraseña no es correcta.")
        }

        saveSession(
            userId = account.id,
            name = "${account.firstName} ${account.lastName}",
            email = account.email,
            onboardingCompleted = true
        )
        return AuthResult(true)
    }

    suspend fun saveSession(
        userId: String,
        name: String,
        email: String,
        countryCode: String = "CL",
        onboardingCompleted: Boolean = false
    ) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_COUNTRY_CODE] = countryCode
            prefs[KEY_ONBOARDING_COMPLETED] = onboardingCompleted
        }
    }

    suspend fun markOnboardingCompleted() {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun currentUserId(): String? = userSessionFlow.first().userId

    suspend fun clearSession() {
        appContext.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_COUNTRY_CODE)
            prefs.remove(KEY_ONBOARDING_COMPLETED)
        }
    }

    private suspend fun loadAccounts(): List<LocalAccount> {
        val json = appContext.dataStore.data.first()[KEY_ACCOUNTS] ?: return emptyList()
        val type = object : TypeToken<List<LocalAccount>>() {}.type
        return runCatching { gson.fromJson<List<LocalAccount>>(json, type) }
            .getOrDefault(emptyList())
    }

    private suspend fun saveAccounts(accounts: List<LocalAccount>) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_ACCOUNTS] = gson.toJson(accounts)
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val factory = runCatching {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        }.getOrElse {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PASSWORD_ITERATIONS,
            256
        )
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
}
