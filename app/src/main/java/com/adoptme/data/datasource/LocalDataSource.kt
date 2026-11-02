package com.adoptme.data.datasource

import com.adoptme.data.model.Dog
import com.adoptme.data.model.Report
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Almacén local de publicaciones propias, intereses y reportes.
 *
 * - Mantiene el estado en memoria para que la UI sea inmediata.
 * - Restaura desde disco al iniciar y persiste cada cambio.
 * - No contiene datos de ejemplo: la app no muestra mascotas ni reportes simulados.
 */
class LocalDataSource(
    private val store: LocalStore = InMemoryLocalStore(),
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val gson = Gson()

    // CONFLATED: si hay varios cambios seguidos, solo importa el último estado.
    private val persistChannel = Channel<PersistedState>(Channel.CONFLATED)

    private val restoreJob = scope.launch { restore() }

    init {
        scope.launch {
            for (state in persistChannel) {
                // Si falla el disco, la app sigue funcionando en memoria.
                runCatching { store.writeState(gson.toJson(state)) }
            }
        }
    }

    @Volatile
    private var loaded = false

    private val _interestedDogs = MutableStateFlow<List<Dog>>(emptyList())
    val interestedDogsFlow: StateFlow<List<Dog>> = _interestedDogs.asStateFlow()

    private val _myPets = MutableStateFlow<List<Dog>>(emptyList())
    val myPetsFlow: StateFlow<List<Dog>> = _myPets.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reportsFlow: StateFlow<List<Report>> = _reports.asStateFlow()

    /**
     * Espera a que termine la restauración inicial.
     * Se usa en tests; la UI observa los StateFlow y se actualiza sola.
     */
    suspend fun awaitReady() {
        restoreJob.join()
    }

    fun getMyPets(): List<Dog> = _myPets.value

    fun addDog(dog: Dog) {
        _myPets.value = listOf(dog) + _myPets.value
        persist()
    }

    fun getNextDogId(): Int = (_myPets.value.maxOfOrNull { it.id } ?: 0) + 1

    fun getReports(): List<Report> = _reports.value

    fun addReport(report: Report) {
        _reports.value = listOf(report) + _reports.value
        persist()
    }

    fun getNextReportId(): Int = (_reports.value.maxOfOrNull { it.id } ?: 0) + 1

    fun addInterestedDog(dog: Dog) {
        if (_interestedDogs.value.none { it.sameIdentityAs(dog) }) {
            _interestedDogs.value = listOf(dog) + _interestedDogs.value
            persist()
        }
    }

    fun removeInterestedDog(dog: Dog) {
        _interestedDogs.value = _interestedDogs.value.filterNot { it.sameIdentityAs(dog) }
        persist()
    }

    fun getInterestedDogs(): List<Dog> = _interestedDogs.value

    private fun persist() {
        if (!loaded) return
        persistChannel.trySend(snapshot())
    }

    private fun snapshot() = PersistedState(
        interests = _interestedDogs.value,
        myPets = _myPets.value,
        reports = _reports.value
    )

    private suspend fun restore() {
        val raw = runCatching { store.readState() }.getOrNull()
        val state = raw?.let { json ->
            runCatching { gson.fromJson(json, PersistedState::class.java) }.getOrNull()
        }

        if (state != null && state.version == PersistedState.CURRENT_VERSION) {
            // Lo que el usuario ya hizo en esta sesión tiene prioridad sobre el disco.
            _interestedDogs.value = mergeDogs(_interestedDogs.value, state.interests)
            _myPets.value = mergeDogs(_myPets.value, state.myPets)
            _reports.value = mergeReports(_reports.value, state.reports)
        }

        loaded = true
        persistChannel.trySend(snapshot())
    }

    private fun mergeDogs(current: List<Dog>, persisted: List<Dog>): List<Dog> =
        (current + persisted).distinctBy { "${it.source}-${it.id}" }

    private fun mergeReports(current: List<Report>, persisted: List<Report>): List<Report> =
        (current + persisted).distinctBy { it.id }

    private fun Dog.sameIdentityAs(other: Dog): Boolean =
        id == other.id && source == other.source
}
