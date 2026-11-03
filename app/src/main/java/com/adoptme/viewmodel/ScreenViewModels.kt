package com.adoptme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adoptme.core.di.AppContainer
import com.adoptme.data.model.AnimalSpecies
import com.adoptme.data.model.AnimalSource
import com.adoptme.data.model.Dog
import com.adoptme.data.model.Report
import com.adoptme.data.model.ReportType
import com.adoptme.data.repository.DogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DogRepository = AppContainer.repository
) : ViewModel() {

    private val _dogs = MutableStateFlow<List<Dog>>(emptyList())
    val dogs: StateFlow<List<Dog>> = _dogs.asStateFlow()

    val interestedDogs: StateFlow<List<Dog>> = repository.interestedDogs

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadDogs()
    }

    fun loadDogs() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.fetchDogsFromApi()
                .onSuccess { animals ->
                    _dogs.value = animals
                    _currentIndex.value = 0
                }
                .onFailure {
                    _errorMessage.value =
                        "No pudimos cargar las mascotas desde Huachitos.cl. Revisa tu conexión e inténtalo de nuevo."
                }
            _isLoading.value = false
        }
    }

    fun onLike() {
        val current = getCurrentDog() ?: return
        repository.addInterestedDog(current)
        _toastMessage.value = "¡Interés registrado para ${current.name}! 🐾"
        nextDog()
    }

    fun onPass() {
        nextDog()
    }

    fun onFavorite() {
        val current = getCurrentDog() ?: return
        saveInterest(current)
    }

    fun saveInterest(dog: Dog) {
        repository.addInterestedDog(dog)
        _toastMessage.value = "💛 ${dog.name} guardado en intereses"
    }

    fun removeInterest(dog: Dog) {
        repository.removeInterestedDog(dog)
        _toastMessage.value = "${dog.name} ya no está en tus intereses"
    }

    private fun nextDog() {
        val next = _currentIndex.value + 1
        _currentIndex.value = next
        if (next >= _dogs.value.size) {
            _toastMessage.value = "No hay más mascotas por ahora"
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun getCurrentDog(): Dog? = _dogs.value.getOrNull(_currentIndex.value)

    fun isFinished(): Boolean = _currentIndex.value >= _dogs.value.size

    fun remainingCount(): Int = maxOf(0, _dogs.value.size - _currentIndex.value)
}

class PublishViewModel(
    private val repository: DogRepository = AppContainer.repository
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun publishDog(
        name: String,
        species: AnimalSpecies,
        age: String,
        breed: String,
        location: String,
        description: String,
        vaccinated: Boolean,
        neutered: Boolean,
        size: String,
        energyLevel: String,
        goodWithKids: Boolean,
        goodWithDogs: Boolean,
        goodWithCats: Boolean,
        houseTrained: Boolean,
        specialNeeds: String?,
        imageUrl: String? = null
    ): Boolean {
        if (name.isBlank() || location.isBlank()) {
            _toastMessage.value = "Completa el nombre y la comuna para publicar"
            return false
        }

        val newDog = Dog(
            id = repository.getNextDogId(),
            name = name.trim(),
            species = species,
            source = AnimalSource.LOCAL,
            age = age.trim().takeIf { it.isNotBlank() },
            breed = breed.trim().takeIf { it.isNotBlank() },
            location = location.trim(),
            distanceKm = null,
            vaccinated = vaccinated,
            neutered = neutered,
            size = size,
            energyLevel = energyLevel,
            goodWithKids = goodWithKids,
            goodWithDogs = goodWithDogs,
            goodWithCats = goodWithCats,
            houseTrained = houseTrained,
            specialNeeds = specialNeeds?.trim()?.takeIf { it.isNotBlank() },
            description = description.trim(),
            emoji = species.emoji,
            imageUrl = imageUrl,
            isMine = true
        )

        repository.addDog(newDog)
        _toastMessage.value = "🐾 ¡${newDog.name} ya está publicado para adopción!"
        return true
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

class MyAdoptionsViewModel(
    private val repository: DogRepository = AppContainer.repository
) : ViewModel() {

    val interestedDogs: StateFlow<List<Dog>> = repository.interestedDogs

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun removeInterest(dog: Dog) {
        repository.removeInterestedDog(dog)
        _toastMessage.value = "${dog.name} ya no está en tus intereses"
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

class ReportsViewModel(
    private val repository: DogRepository = AppContainer.repository
) : ViewModel() {

    val reports: StateFlow<List<Report>> = repository.reports

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun addReport(
        type: ReportType,
        location: String,
        description: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val newReport = Report(
            id = repository.getNextReportId(),
            type = type,
            location = location,
            description = description,
            timeAgo = "Ahora",
            latitude = latitude,
            longitude = longitude,
            createdAt = System.currentTimeMillis()
        )
        repository.addReport(newReport)
        _toastMessage.value = if (latitude != null && longitude != null) {
            "🚨 Reporte guardado y ubicado en el mapa."
        } else {
            "🚨 Reporte guardado sin ubicación en el mapa."
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

class MyPetViewModel(
    repository: DogRepository = AppContainer.repository
) : ViewModel() {

    val myPets: StateFlow<List<Dog>> = repository.myPets
}
