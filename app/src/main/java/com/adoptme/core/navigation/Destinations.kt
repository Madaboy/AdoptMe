package com.adoptme.core.navigation

/**
 * Rutas de navegación fuertemente tipadas.
 * La barra inferior tiene 4 destinos; "Adoptar" es la acción ovalada central.
 */
sealed class Screen(val route: String, val label: String) {
    object Adopt : Screen("adopt", "Adoptar")
    object Reports : Screen("reports", "Reportes")
    object Publish : Screen("publish", "Publicar")
    object Interests : Screen("interests", "Intereses")
    object MyPet : Screen("myPet", "Mi Mascota")
    object More : Screen("more", "Más")
}

val bottomNavItems = listOf(
    Screen.Publish,
    Screen.Reports,
    Screen.Interests,
    Screen.MyPet
)
