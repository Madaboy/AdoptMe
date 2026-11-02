package com.adoptme.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.adoptme.data.model.Dog
import com.adoptme.data.model.Report
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Estado local que se conserva entre reinicios de la app.
 * La versión permite descartar datos de un formato anterior sin romper la app.
 */
data class PersistedState(
    val version: Int = CURRENT_VERSION,
    val interests: List<Dog> = emptyList(),
    val myPets: List<Dog> = emptyList(),
    val reports: List<Report> = emptyList()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Contrato mínimo de almacenamiento. Permite usar DataStore en producción
 * y un almacén en memoria determinista en los tests.
 */
interface LocalStore {
    suspend fun readState(): String?
    suspend fun writeState(json: String)
}

class InMemoryLocalStore(initialJson: String? = null) : LocalStore {

    var storedJson: String? = initialJson
        private set

    override suspend fun readState(): String? = storedJson

    override suspend fun writeState(json: String) {
        storedJson = json
    }
}

/**
 * Persistencia en disco con Preferences DataStore.
 * Solo guarda datos propios del usuario: intereses, publicaciones y reportes.
 */
class DataStoreLocalStore(context: Context) : LocalStore {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
    )

    override suspend fun readState(): String? = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .first()[STATE_KEY]

    override suspend fun writeState(json: String) {
        dataStore.edit { preferences ->
            preferences[STATE_KEY] = json
        }
    }

    private companion object {
        const val DATASTORE_NAME = "adoptme_local"
        val STATE_KEY = stringPreferencesKey("state_v1")
    }
}
