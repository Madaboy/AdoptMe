package com.adoptme.core.common

import org.jsoup.Jsoup

/**
 * Limpia el HTML que entrega Huachitos.cl (<p>, <br>, entidades) y devuelve
 * texto plano con saltos de línea legibles. Nunca se muestra HTML crudo.
 */
object HtmlText {

    fun clean(html: String?): String {
        if (html.isNullOrBlank()) return ""

        val doc = Jsoup.parse(html)
        doc.select("br").append("\\n")
        doc.select("p").prepend("\\n\\n")

        return doc.text()
            .replace("\\n", "\n")
            .replace(Regex("[ \\t\\u00A0]+"), " ")
            .replace(Regex(" ?\\n ?"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
