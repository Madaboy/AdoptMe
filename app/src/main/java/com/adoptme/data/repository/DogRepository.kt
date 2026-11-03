package com.adoptme.data.repository

import com.adoptme.data.api.HuachitosApiService
import com.adoptme.data.api.HuachitosClient
import com.adoptme.data.datasource.LocalDataSource
import com.adoptme.data.model.AnimalSource
import com.adoptme.data.model.Dog
import com.adoptme.data.model.Report
import com.adoptme.data.model.toDog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * INTERFAZ DE REPOSITORIO DE ANIMALES Y REPORTES
 * Oculta si los datos vienen de la API remota o del almacenamiento local.
 */
interface DogRepository {
    val interestedDogs: StateFlow<List<Dog>>
    val myPets: StateFlow<List<Dog>>
    val reports: StateFlow<List<Report>>

    fun getMyPets(): List<Dog>
    fun addDog(dog: Dog)
    fun getNextDogId(): Int

    fun getReports(): List<Report>
    fun addReport(report: Report)
    fun getNextReportId(): Int

    fun addInterestedDog(dog: Dog)
    fun removeInterestedDog(dog: Dog)
    fun getInterestedDogs(): List<Dog>

    suspend fun fetchDogsFromApi(): Result<List<Dog>>

    /**
     * Devuelve la portada y, para fichas de Huachitos, las fotos adicionales
     * que la página pública del animal tenga disponibles.
     */
    suspend fun fetchGallery(dog: Dog): List<String>
}

class DogRepositoryImpl(
    private val localDataSource: LocalDataSource = LocalDataSource(),
    private val api: HuachitosApiService = HuachitosClient.service
) : DogRepository {

    private val httpClient = OkHttpClient()
    private val galleryCache = mutableMapOf<String, List<String>>()

    override val interestedDogs: StateFlow<List<Dog>> = localDataSource.interestedDogsFlow
    override val myPets: StateFlow<List<Dog>> = localDataSource.myPetsFlow
    override val reports: StateFlow<List<Report>> = localDataSource.reportsFlow

    override fun getMyPets(): List<Dog> = localDataSource.getMyPets()

    override fun addDog(dog: Dog) = localDataSource.addDog(dog.copy(isMine = true))

    override fun getNextDogId(): Int = localDataSource.getNextDogId()

    override fun getReports(): List<Report> = localDataSource.getReports()

    override fun addReport(report: Report) = localDataSource.addReport(report)

    override fun getNextReportId(): Int = localDataSource.getNextReportId()

    override fun addInterestedDog(dog: Dog) = localDataSource.addInterestedDog(dog)

    override fun removeInterestedDog(dog: Dog) = localDataSource.removeInterestedDog(dog)

    override fun getInterestedDogs(): List<Dog> = localDataSource.getInterestedDogs()

    override suspend fun fetchDogsFromApi(): Result<List<Dog>> = runCatching {
        val firstPage = api.getAnimales(page = 1)
        val lastPage = firstPage.meta?.lastPage?.coerceAtLeast(1) ?: 1

        val pages = buildList {
            add(firstPage)
            for (page in 2..lastPage) {
                add(api.getAnimales(page = page))
            }
        }

        pages
            .flatMap { it.data }
            .filter { it.estado.isNullOrBlank() || it.estado.equals("adopcion", ignoreCase = true) }
            .map { it.toDog() }
            .distinctBy { "${it.source}-${it.externalId ?: it.id}" }
    }

    override suspend fun fetchGallery(dog: Dog): List<String> {
        val cover = listOfNotNull(dog.imageUrl)
        if (dog.source != AnimalSource.HUACHITOS || dog.sourceUrl.isNullOrBlank()) {
            return cover
        }

        galleryCache[dog.sourceUrl]?.let { return it }

        val gallery = withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(dog.sourceUrl)
                    .header("User-Agent", "AdoptMe/0.1")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@runCatching cover
                    val html = response.body?.string().orEmpty()
                    GalleryParser.parse(html, dog.sourceUrl, cover)
                }
            }.getOrDefault(cover)
        }

        galleryCache[dog.sourceUrl] = gallery
        return gallery
    }
}
