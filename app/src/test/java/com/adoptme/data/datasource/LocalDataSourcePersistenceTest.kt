package com.adoptme.data.datasource

import com.adoptme.data.model.Dog
import com.adoptme.data.model.Report
import com.adoptme.data.model.ReportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalDataSourcePersistenceTest {

    @Test
    fun interestsPetsAndReports_surviveANewInstance() = runTest {
        val store = InMemoryLocalStore()

        val firstScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val first = LocalDataSource(store, firstScope)
        first.awaitReady()

        val interest = Dog(
            id = 491,
            name = "Balto",
            location = "San Bernardo, RM",
            description = "Blanco mestizo"
        )
        val myPet = Dog(
            id = 1,
            name = "Milo",
            location = "Ñuñoa, RM",
            description = "Cachorro juguetón",
            imageUrl = "file:///data/user/0/com.adoptme/files/pet_images/pet_milo.jpg",
            isMine = true
        )

        first.addInterestedDog(interest)
        first.addDog(myPet)
        first.addReport(
            Report(
                id = 1,
                type = ReportType.DANGER,
                location = "Av. Grecia 890",
                description = "Perro herido",
                timeAgo = "Ahora"
            )
        )
        advanceUntilIdle()

        firstScope.cancel()

        val secondScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val second = LocalDataSource(store, secondScope)
        second.awaitReady()
        advanceUntilIdle()

        assertEquals(listOf("Balto"), second.getInterestedDogs().map { it.name })
        assertEquals(listOf("Milo"), second.getMyPets().map { it.name })
        assertTrue(second.getMyPets().first().isMine)
        assertEquals(
            "file:///data/user/0/com.adoptme/files/pet_images/pet_milo.jpg",
            second.getMyPets().first().imageUrl
        )
        assertEquals(1, second.getReports().size)
        assertEquals("Av. Grecia 890", second.getReports().first().location)

        secondScope.cancel()
    }

    @Test
    fun removingAnInterest_isPersisted() = runTest {
        val store = InMemoryLocalStore()
        val dog = Dog(id = 7, name = "Luna", location = "Santiago", description = "")

        val firstScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val first = LocalDataSource(store, firstScope)
        first.awaitReady()
        first.addInterestedDog(dog)
        first.removeInterestedDog(dog)
        advanceUntilIdle()
        firstScope.cancel()

        val secondScope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val second = LocalDataSource(store, secondScope)
        second.awaitReady()
        advanceUntilIdle()

        assertTrue(second.getInterestedDogs().isEmpty())
        secondScope.cancel()
    }

    @Test
    fun corruptStoredState_startsEmptyInsteadOfCrashing() = runTest {
        val store = InMemoryLocalStore(initialJson = "{ esto no es json }")
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))

        val dataSource = LocalDataSource(store, scope)
        dataSource.awaitReady()
        advanceUntilIdle()

        assertTrue(dataSource.getInterestedDogs().isEmpty())
        assertTrue(dataSource.getMyPets().isEmpty())
        assertTrue(dataSource.getReports().isEmpty())
        scope.cancel()
    }
}
