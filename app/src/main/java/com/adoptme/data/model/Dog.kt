package com.adoptme.data.model

/**
 * Modelo único de animal para el catálogo y las publicaciones propias.
 *
 * Regla de honestidad de datos: todo campo que la fuente no entregue queda en `null`
 * y la UI lo trata como "No informado" (nunca se inventa un valor).
 */
data class Dog(
    val id: Int,
    val name: String,
    val species: AnimalSpecies = AnimalSpecies.DOG,
    val source: AnimalSource = AnimalSource.LOCAL,
    val externalId: String? = null,
    val age: String? = null,
    val breed: String? = null,
    val gender: String? = null,
    val location: String,
    val distanceKm: Double? = null,
    val vaccinated: Boolean? = null,
    val neutered: Boolean? = null,
    val size: String? = null,           // Pequeño, Mediano, Grande
    val energyLevel: String? = null,    // Baja, Media, Alta
    val goodWithKids: Boolean? = null,
    val goodWithDogs: Boolean? = null,
    val goodWithCats: Boolean? = null,
    val houseTrained: Boolean? = null,
    val specialNeeds: String? = null,
    val description: String,
    val descriptionPersonality: String? = null,
    val descriptionExtra: String? = null,
    val emoji: String = "🐾",
    val imageUrl: String? = null,
    val sourceUrl: String? = null,
    val backgroundColorHex: String = "#EFEEE4",
    val isMine: Boolean = false,
    val ownerId: String? = null
)

enum class AnimalSpecies(val label: String, val emoji: String) {
    DOG("Perro", "🐕"),
    CAT("Gato", "🐈"),
    RABBIT("Conejo", "🐇"),
    OTHER("Otro", "🐾")
}

enum class AnimalSource {
    HUACHITOS,
    LOCAL
}
