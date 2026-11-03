package com.adoptme.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GalleryParserTest {

    private val cover = listOf("https://huachitos.cl/storage/animal-images/cover.jpeg")

    @Test
    fun extractsOnlyAnimalImages_andDeduplicates() {
        val html = """
            <html><body>
              <img src="/storage/animal-images/balto-1.jpeg">
              <img src="https://huachitos.cl/storage/animal-images/balto-2.jpeg">
              <img data-src="/storage/animal-images/balto-3.jpeg">
              <img src="/assets/logo.png">
              <img src="/storage/animal-images/balto-1.jpeg">
            </body></html>
        """.trimIndent()

        val result = GalleryParser.parse(
            html = html,
            sourceUrl = "https://huachitos.cl/animal/491-balto",
            cover = cover
        )

        assertEquals(4, result.size)
        assertEquals(cover.first(), result.first())
        assertTrue(result.contains("https://huachitos.cl/storage/animal-images/balto-1.jpeg"))
        assertTrue(result.contains("https://huachitos.cl/storage/animal-images/balto-2.jpeg"))
        assertTrue(result.contains("https://huachitos.cl/storage/animal-images/balto-3.jpeg"))
    }

    @Test
    fun emptyHtml_returnsCover() {
        val result = GalleryParser.parse(
            html = "",
            sourceUrl = "https://huachitos.cl/animal/491-balto",
            cover = cover
        )

        assertEquals(cover, result)
    }
}
