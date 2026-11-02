package com.adoptme.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adoptme.R
import com.adoptme.core.navigation.Screen

/**
 * Barra inferior de 5 posiciones.
 *
 * Publicar · Reportes · [Adoptar] · Intereses · Mi Mascota
 *
 * "Adoptar" es la acción central ovalada que abre el catálogo.
 * "Publicar" ocupa la primera posición y abre el formulario a pantalla completa.
 */
@Composable
fun AdoptMeBottomBar(
    currentRoute: String?,
    interestCount: Int,
    onNavigate: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
        ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    screen = Screen.Publish,
                    currentRoute = currentRoute,
                    icon = painterResource(R.drawable.ic_publish_adoption),
                    onClick = { onNavigate(Screen.Publish) }
                )
                BottomBarItem(
                    screen = Screen.Reports,
                    currentRoute = currentRoute,
                    imageVector = Icons.Filled.Place,
                    onClick = { onNavigate(Screen.Reports) }
                )

                // Espacio reservado para la cápsula central
                Spacer(Modifier.width(104.dp))

                BottomBarItem(
                    screen = Screen.Interests,
                    currentRoute = currentRoute,
                    imageVector = Icons.Filled.Favorite,
                    badge = interestCount,
                    onClick = { onNavigate(Screen.Interests) }
                )
                BottomBarItem(
                    screen = Screen.MyPet,
                    currentRoute = currentRoute,
                    imageVector = Icons.Filled.Person,
                    onClick = { onNavigate(Screen.MyPet) }
                )
            }
        }

        AdoptCapsule(
            modifier = Modifier.align(Alignment.TopCenter),
            onClick = { onNavigate(Screen.Adopt) }
        )
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    screen: Screen,
    currentRoute: String?,
    onClick: () -> Unit,
    imageVector: ImageVector? = null,
    icon: Painter? = null,
    badge: Int = 0
) {
    val selected = currentRoute == screen.route
    val tint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            when {
                imageVector != null -> Icon(
                    imageVector = imageVector,
                    contentDescription = screen.label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )

                icon != null -> Icon(
                    painter = icon,
                    contentDescription = screen.label,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (badge > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-6).dp)
                        .size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (badge > 9) "9+" else badge.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

        Text(
            text = screen.label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = tint,
            maxLines = 1
        )
    }
}

/**
 * Cápsula ovalada "Adoptar" con la huella.
 * Es la acción principal de la barra: descubrir mascotas que buscan hogar.
 */
@Composable
private fun AdoptCapsule(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(width = 104.dp, height = 44.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.background),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_paw),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "Adoptar",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
