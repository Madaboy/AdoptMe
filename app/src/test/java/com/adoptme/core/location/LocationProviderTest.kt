package com.adoptme.core.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationProviderTest {

    @Test
    fun samePoint_hasZeroDistance() {
        val distance = LocationProvider.distanceKm(
            fromLatitude = -33.4489,
            fromLongitude = -70.6693,
            toLatitude = -33.4489,
            toLongitude = -70.6693
        )

        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun oneDegreeOfLatitude_isAbout111Km() {
        val distance = LocationProvider.distanceKm(
            fromLatitude = 0.0,
            fromLongitude = 0.0,
            toLatitude = 1.0,
            toLongitude = 0.0
        )

        assertTrue(distance in 111.0..111.3)
    }
}
