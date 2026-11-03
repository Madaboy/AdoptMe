package com.adoptme.data.repository

import org.jsoup.Jsoup

/**
 * Extrae las fotos de la página pública de Huachitos.
 * La API entrega solo una portada; el resto vive en el HTML del animal.
 */
internal object GalleryParser {

    fun parse(html: String, sourceUrl: String, cover: List<String>): List<String> {
        if (html.isBlank()) return cover

        val document = Jsoup.parse(html, sourceUrl)
        val pageImages = document.select("img").mapNotNull { image ->
            val raw = image.attr("src").ifBlank { image.attr("data-src") }
            if (!raw.contains("/storage/animal-images/")) return@mapNotNull null
            when {
                raw.startsWith("http") -> raw
                raw.startsWith("/") -> "https://huachitos.cl$raw"
                else -> "https://huachitos.cl/$raw"
            }
        }.distinct()

        return (cover + pageImages).distinct().ifEmpty { cover }
    }
}
