package com.adoptme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adoptme.R
import com.adoptme.core.navigation.Screen
import com.adoptme.core.session.UserSession
import com.adoptme.viewmodel.AccountSessionState
import com.adoptme.viewmodel.AccountViewModel

/**
 * Pestaña Cuenta.
 *
 * Si no hay sesión muestra el registro/login local. Si hay sesión muestra el
 * perfil con estadísticas reales y accesos a las secciones de AdoptMe.
 */
@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onNavigate: (Screen) -> Unit,
    onPublish: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val myPets by viewModel.myPets.collectAsState()
    val interests by viewModel.interestedDogs.collectAsState()
    val reports by viewModel.reports.collectAsState()

    when (val state = sessionState) {
        AccountSessionState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        AccountSessionState.LoggedOut -> {
            AuthScreen(
                viewModel = viewModel,
                onShowToast = onShowToast
            )
        }

        is AccountSessionState.LoggedIn -> {
            AccountProfileScreen(
                session = state.session,
                myPetsCount = myPets.size,
                interestsCount = interests.size,
                reportsCount = reports.size,
                onNavigate = onNavigate,
                onPublish = onPublish,
                onShowToast = onShowToast,
                onLogout = viewModel::logout
            )
        }
    }
}

@Composable
private fun AccountProfileScreen(
    session: UserSession,
    myPetsCount: Int,
    interestsCount: Int,
    reportsCount: Int,
    onNavigate: (Screen) -> Unit,
    onPublish: () -> Unit,
    onShowToast: (String) -> Unit,
    onLogout: () -> Unit
) {
    val displayName = session.userName?.takeIf { it.isNotBlank() } ?: "Adoptante"
    val username = session.userEmail
        ?.substringBefore("@")
        ?.takeIf { it.isNotBlank() }
        ?.let { "@$it" }
        ?: "Cuenta local"
    val initials = remember(displayName) { initialsOf(displayName) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            ProfileHeader(
                initials = initials,
                onNotifications = {
                    onShowToast("Las notificaciones estarán disponibles con el backend.")
                }
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_paw),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Adoptante",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        item {
            AccountStatsCard(
                myPetsCount = myPetsCount,
                interestsCount = interestsCount,
                reportsCount = reportsCount
            )
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            AccountHighlightCard(onPublish = onPublish)
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            AccountMenuCard(
                myPetsCount = myPetsCount,
                interestsCount = interestsCount,
                reportsCount = reportsCount,
                onNavigate = onNavigate
            )
        }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            TextButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            ) {
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = {
                Text(
                    "Tu cuenta y tus publicaciones seguirán guardadas en este dispositivo. " +
                        "Puedes volver a entrar cuando quieras."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    initials: String,
    onNotifications: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 18.dp, top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cuenta",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Surface(
                onClick = onNotifications,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = "Notificaciones",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp)
                .size(104.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(4.dp, MaterialTheme.colorScheme.background)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun AccountStatsCard(
    myPetsCount: Int,
    interestsCount: Int,
    reportsCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Publicaciones",
                value = myPetsCount,
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatItem(
                label = "Intereses",
                value = interestsCount,
                modifier = Modifier.weight(1f)
            )
            StatDivider()
            StatItem(
                label = "Reportes",
                value = reportsCount,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun AccountHighlightCard(onPublish: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.inverseSurface
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tu próxima adopción empieza aquí",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Publica una mascota y ayuda a encontrar su hogar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.72f)
                )
            }
            Button(
                onClick = onPublish,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Publicar",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AccountMenuCard(
    myPetsCount: Int,
    interestsCount: Int,
    reportsCount: Int,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            AccountMenuRow(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_publish_adoption),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = "Mis publicaciones",
                subtitle = if (myPetsCount == 0) {
                    "Aún no publicas una mascota"
                } else {
                    "$myPetsCount ${if (myPetsCount == 1) "ficha activa" else "fichas activas"}"
                },
                onClick = { onNavigate(Screen.MyPet) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            AccountMenuRow(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = "Mis intereses",
                subtitle = if (interestsCount == 0) {
                    "Sin mascotas guardadas"
                } else {
                    "$interestsCount ${if (interestsCount == 1) "mascota guardada" else "mascotas guardadas"}"
                },
                onClick = { onNavigate(Screen.Interests) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            AccountMenuRow(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = "Mis reportes",
                subtitle = if (reportsCount == 0) {
                    "Sin reportes registrados"
                } else {
                    "$reportsCount ${if (reportsCount == 1) "reporte registrado" else "reportes registrados"}"
                },
                onClick = { onNavigate(Screen.Reports) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            AccountMenuRow(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = "Ajustes y privacidad",
                subtitle = "Cuenta, datos y preferencias",
                onClick = { onNavigate(Screen.More) }
            )
        }
    }
}

@Composable
private fun AccountMenuRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "›",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun initialsOf(name: String): String {
    val initials = name
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
    return initials.ifBlank { "🐾" }
}
