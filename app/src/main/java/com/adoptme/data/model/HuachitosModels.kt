package com.adoptme.data.model

import com.adoptme.core.common.HtmlText
import com.google.gson.annotations.SerializedName

/**
 * Contrato REAL de https://huachitos.cl/api/animales
 * Respuesta: { "data": [...], "links": {...}, "meta": {...} }
 */
data class HuachitosResponse(
    val data: List<HuachitosAnimal> = emptyList(),
    val links: HuachitosLinks? = null,
    val meta: HuachitosMeta? = null
)

data class HuachitosLinks(
    val first: String? = null,
    val last: String? = null,
    val prev: String? = null,
    val next: String? = null
)

data class HuachitosMeta(
    @SerializedName("current_page") val currentPage: Int = 1,
    @SerializedName("last_page") val lastPage: Int = 1,
    @SerializedName("per_page") val perPage: Int = 0,
    val total: Int = 0
)

data class HuachitosAnimal(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val color: String? = null,
    val edad: String? = null,
    val estado: String? = null,
    val genero: String? = null,
    @SerializedName("desc_fisica") val descFisica: String? = null,
    @SerializedName("desc_personalidad") val descPersonalidad: String? = null,
    @SerializedName("desc_adicional") val descAdicional: String? = null,
    val esterilizado: Int? = null,
    val vacunas: Int? = null,
    val imagen: String? = null,
    val equipo: String? = null,
    val region: String? = null,
    val comuna: String? = null,
    val url: String? = null
)

/**
 * Mapeo explícito API → modelo interno.
 * No se rellenan campos que la API no entrega (tamaño, energía, compatibilidad, distancia).
 */
fun HuachitosAnimal.toDog(): Dog {
    val physical = HtmlText.clean(descFisica)
    val personality = HtmlText.clean(descPersonalidad)
    val extra = HtmlText.clean(descAdicional)
    val species = tipo.toSpecies()

    return Dog(
        id = id,
        name = nombre,
        species = species,
        source = AnimalSource.HUACHITOS,
        externalId = id.toString(),
        age = edad?.trim()?.takeIf { it.isNotBlank() },
        gender = genero?.trim()?.takeIf { it.isNotBlank() },
        location = listOfNotNull(
            comuna?.trim()?.takeIf { it.isNotBlank() },
            region?.trim()?.takeIf { it.isNotBlank() }
        ).joinToString(", "),
        distanceKm = null,
        vaccinated = vacunas?.let { it == 1 },
        neutered = esterilizado?.let { it == 1 },
        size = null,
        energyLevel = null,
        goodWithKids = null,
        goodWithDogs = null,
        goodWithCats = null,
        houseTrained = null,
        specialNeeds = null,
        description = listOf(physical, personality, extra)
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n\n"),
        descriptionPersonality = personality.takeIf { it.isNotBlank() },
        descriptionExtra = extra.takeIf { it.isNotBlank() },
        emoji = species.emoji,
        imageUrl = imagen?.takeIf { it.isNotBlank() },
        sourceUrl = url?.takeIf { it.isNotBlank() }
    )
}

private fun String?.toSpecies(): AnimalSpecies {
    val value = this?.lowercase().orEmpty()
    return when {
        "perro" in value -> AnimalSpecies.DOG
        "gato" in value -> AnimalSpecies.CAT
        "conejo" in value -> AnimalSpecies.RABBIT
        else -> AnimalSpecies.OTHER
    }
}
