package com.adoptme.data.repository

import com.adoptme.data.api.HuachitosApiService
import com.adoptme.data.datasource.LocalDataSource
import com.adoptme.data.model.AnimalSpecies
import com.adoptme.data.model.Dog
import com.adoptme.data.model.HuachitosAnimal
import com.adoptme.data.model.HuachitosMeta
import com.adoptme.data.model.HuachitosResponse
import com.adoptme.data.model.Report
import com.adoptme.data.model.ReportType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DogRepositoryTest {

    private lateinit var repository: DogRepository

    @Before
    fun setUp() {
        repository = DogRepositoryImpl(LocalDataSource())
    }

    @Test
    fun myPets_startsEmpty_andIgnoresSeedCatalog() {
        assertTrue(repository.getMyPets().isEmpty())
    }

    @Test
    fun addDog_marksItAsMineAndAddsToMyPets() {
        val newDog = Dog(
            id = repository.getNextDogId(),
            name = "TestDog",
            species = AnimalSpecies.DOG,
            age = "1 año",
            breed = "Poodle",
            location = "Santiago",
            distanceKm = 2.0,
            vaccinated = true,
            neutered = true,
            size = "Pequeño",
            energyLevel = "Baja",
            goodWithKids = true,
            goodWithDogs = true,
            goodWithCats = true,
            houseTrained = true,
            description = "Dog de prueba",
            emoji = "🐩",
            backgroundColorHex = "#FFFFFF"
        )

        repository.addDog(newDog)

        val mine = repository.getMyPets()
        assertEquals(1, mine.size)
        assertEquals("TestDog", mine.first().name)
        assertTrue(mine.first().isMine)
    }

    @Test
    fun addInterestedDog_storesDogWithoutDuplicates() {
        val dog = Dog(
            id = 901,
            name = "Interés",
            location = "Ñuñoa",
            description = "Perro de prueba"
        )

        repository.addInterestedDog(dog)
        repository.addInterestedDog(dog)

        val interested = repository.getInterestedDogs()
        assertEquals(1, interested.size)
        assertEquals(dog.id, interested.first().id)
    }

    @Test
    fun removeInterestedDog_removesOnlyThatDog() {
        val dog = Dog(id = 902, name = "Quitar", location = "Maipú", description = "")
        repository.addInterestedDog(dog)
        repository.removeInterestedDog(dog)

        assertTrue(repository.getInterestedDogs().isEmpty())
    }

    @Test
    fun reports_addAndRetrieve() {
        val initialReports = repository.getReports().size
        val newReport = Report(
            id = repository.getNextReportId(),
            type = ReportType.DANGER,
            location = "Test Loc",
            description = "Test Desc",
            timeAgo = "Ahora"
        )

        repository.addReport(newReport)

        val reportsAfter = repository.getReports()
        assertEquals(initialReports + 1, reportsAfter.size)
        assertEquals("Test Loc", reportsAfter.first().location)
    }

    @Test
    fun fetchDogsFromApi_readsEveryPage_filtersNonAdoption_andDeduplicates() = runBlocking {
        val api = FakeHuachitosApi(
            pages = mapOf(
                1 to HuachitosResponse(
                    data = listOf(
                        animal(id = 1, name = "Balto", estado = "adopcion"),
                        animal(id = 2, name = "Perdido", estado = "perdido")
                    ),
                    meta = HuachitosMeta(currentPage = 1, lastPage = 2, perPage = 100, total = 3)
                ),
                2 to HuachitosResponse(
                    data = listOf(
                        animal(id = 1, name = "Balto", estado = "adopcion"),
                        animal(id = 3, name = "Michi", estado = "adopcion", tipo = "Gato")
                    ),
                    meta = HuachitosMeta(currentPage = 2, lastPage = 2, perPage = 100, total = 3)
                )
            )
        )

        val repo = DogRepositoryImpl(LocalDataSource(), api)
        val result = repo.fetchDogsFromApi()

        assertTrue(result.isSuccess)
        val dogs = result.getOrThrow()
        assertEquals(listOf("Balto", "Michi"), dogs.map { it.name })
        assertEquals(AnimalSpecies.CAT, dogs.last().species)
        assertEquals(listOf(1, 2), api.requestedPages)
    }

    private fun animal(
        id: Int,
        name: String,
        estado: String,
        tipo: String = "Perro"
    ) = HuachitosAnimal(
        id = id,
        nombre = name,
        tipo = tipo,
        estado = estado,
        descFisica = "<p>Descripción de $name</p>",
        esterilizado = 1,
        vacunas = 0,
        imagen = "https://huachitos.cl/img/$id.jpeg",
        region = "RM",
        comuna = "Santiago",
        url = "https://huachitos.cl/animal/$id"
    )

    private class FakeHuachitosApi(
        private val pages: Map<Int, HuachitosResponse>
    ) : HuachitosApiService {

        val requestedPages = mutableListOf<Int>()

        override suspend fun getAnimales(page: Int): HuachitosResponse {
            requestedPages += page
            return pages.getValue(page)
        }
    }
}
