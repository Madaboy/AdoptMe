package com.adoptme.data.model

import com.adoptme.core.common.HtmlText
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class HuachitosMapperTest {

    private val gson = Gson()

    @Test
    fun parsesRealApiShape_andMapsOnlyReportedFields() {
        val json = """
            {
              "data": [
                {
                  "id": 491,
                  "nombre": "Balto",
                  "tipo": "Perro",
                  "color": "#F65B2A",
                  "edad": "5 Año",
                  "estado": "adopcion",
                  "genero": "macho",
                  "desc_fisica": "<p>Blanco mestizo, estatura media.</p>",
                  "desc_personalidad": "<p>Alegre y juguetón.&nbsp;Necesita espacio.</p>",
                  "desc_adicional": "<p>Vacunas al día.</p>",
                  "esterilizado": 1,
                  "vacunas": 1,
                  "imagen": "https://huachitos.cl/storage/balto.jpeg",
                  "equipo": "Privado",
                  "region": "RM",
                  "comuna": "San Bernardo",
                  "url": "https://huachitos.cl/animal/491-balto"
                }
              ],
              "links": {"next": "https://huachitos.cl/api/animales?page=2"},
              "meta": {"current_page": 1, "last_page": 2, "per_page": 100, "total": 114}
            }
        """.trimIndent()

        val response = gson.fromJson(json, HuachitosResponse::class.java)

        assertEquals(2, response.meta?.lastPage)
        assertEquals(114, response.meta?.total)
        assertEquals(1, response.data.size)

        val dog = response.data.first().toDog()

        assertEquals("Balto", dog.name)
        assertEquals(AnimalSpecies.DOG, dog.species)
        assertEquals(AnimalSource.HUACHITOS, dog.source)
        assertEquals("491", dog.externalId)
        assertEquals("5 Año", dog.age)
        assertEquals("macho", dog.gender)
        assertEquals("San Bernardo, RM", dog.location)
        assertEquals(true, dog.vaccinated)
        assertEquals(true, dog.neutered)
        assertEquals("https://huachitos.cl/storage/balto.jpeg", dog.imageUrl)
        assertEquals("https://huachitos.cl/animal/491-balto", dog.sourceUrl)

        // La API no entrega estos campos: deben quedar nulos, no inventarse.
        assertNull(dog.size)
        assertNull(dog.energyLevel)
        assertNull(dog.distanceKm)
        assertNull(dog.goodWithKids)
        assertNull(dog.breed)

        // Las descripciones llegan en HTML y se muestran como texto plano.
        assertFalse(dog.description.contains("<p>"))
        assertTrue(dog.description.contains("Blanco mestizo"))
        assertTrue(dog.descriptionPersonality!!.contains("Alegre y juguetón"))
    }

    @Test
    fun mapsCatAndRabbitSpecies() {
        val cat = HuachitosAnimal(id = 1, nombre = "Michi", tipo = "Gato")
        val rabbit = HuachitosAnimal(id = 2, nombre = "Copito", tipo = "Conejo")
        val unknown = HuachitosAnimal(id = 3, nombre = "Otro", tipo = "Hurón")

        assertEquals(AnimalSpecies.CAT, cat.toDog().species)
        assertEquals(AnimalSpecies.RABBIT, rabbit.toDog().species)
        assertEquals(AnimalSpecies.OTHER, unknown.toDog().species)
    }

    @Test
    fun emptyDescription_isEmptyAndNeverNull() {
        val animal = HuachitosAnimal(id = 4, nombre = "Sin datos", tipo = "Perro")
        val dog = animal.toDog()

        assertTrue(dog.description.isEmpty())
        assertNull(dog.descriptionPersonality)
        assertNull(dog.descriptionExtra)
    }

    @Test
    fun htmlText_removesTags_andPreservesParagraphBreaks() {
        val result = HtmlText.clean("<p>Primera línea</p><p>Segunda <strong>línea</strong></p><br>Tercera &amp; final")

        assertFalse(result.contains("<"))
        assertTrue(result.contains("Primera línea"))
        assertTrue(result.contains("Segunda línea"))
        assertTrue(result.contains("Tercera & final"))
        assertTrue(result.contains("\n"))
    }
}
