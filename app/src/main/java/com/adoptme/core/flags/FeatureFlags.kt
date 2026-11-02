package com.adoptme.core.flags

/**
 * Enum representativo de las funcionalidades de la Expansión 4.6.
 * Todas las características están deshabilitadas por defecto hasta pasar la etapa de QA correspondiente.
 */
enum class Feature {
    CHAT,
    COMMUNITY,
    STORE,
    DONATIONS,
    GAMIFICATION,
    MULTI_COUNTRY
}

object FeatureFlags {
    private val flags: MutableMap<Feature, Boolean> = mutableMapOf(
        Feature.CHAT to false,
        Feature.COMMUNITY to false,
        Feature.STORE to false,
        Feature.DONATIONS to false,
        Feature.GAMIFICATION to false,
        Feature.MULTI_COUNTRY to false
    )

    fun isEnabled(feature: Feature): Boolean = flags[feature] ?: false

    fun setEnabled(feature: Feature, enabled: Boolean) {
        flags[feature] = enabled
    }

    fun getAllFlags(): Map<Feature, Boolean> = flags.toMap()
}
