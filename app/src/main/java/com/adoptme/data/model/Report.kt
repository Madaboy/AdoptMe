package com.adoptme.data.model

/**
 * Modelo de datos para un reporte de animal en peligro.
 * TODO: Cuando se conecte la API, esto se enviará via POST y se listará via GET.
 */
data class Report(
    val id: Int,
    val type: ReportType,
    val location: String,
    val description: String,
    val timeAgo: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = 0L,
    /**
     * Ruta local del archivo comprimido. En el backend debe guardarse solo la
     * clave/URL de object storage, nunca el binario ni base64 en la base de datos.
     */
    val imagePath: String? = null
)

enum class ReportType(val label: String, val emoji: String) {
    DANGER("Emergencia", "🚨"),
    LOST("Animal perdido", "🐕"),
    STREET("Situación de calle", "⚠️"),
    AT_RISK("En riesgo", "🔶")
}
