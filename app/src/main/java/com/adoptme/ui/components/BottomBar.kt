package com.adoptme.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adoptme.R
import com.adoptme.core.navigation.Screen
import com.adoptme.ui.theme.CoralAccent

/**
 * Barra inferior flotante de 5 posiciones.
 *
 * Publicar · Reportes · [Adoptar] · Intereses · Mi Mascota
 *
 * "Adoptar" es el botón circular coral con la huella y la acción principal.
 * "Publicar" ocupa la primera posición y abre el formulario a pantalla completa.
 */
@Composable
fun AdoptMeBottomBar(
    currentRoute: String?,
    interestCount: Int,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 18.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(
                    screen = Screen.Publish,
                    currentRoute = currentRoute,
                    icon = painterResource(R.drawable.ic_publish_adoption),
                    onClick = { onNavigate(Screen.Publish) }
                )
                DockItem(
                    screen = Screen.Reports,
                    currentRoute = currentRoute,
                    imageVector = Icons.Filled.Place,
                    onClick = { onNavigate(Screen.Reports) }
                )

                CenterAdoptItem(onClick = { onNavigate(Screen.Adopt) })

                DockItem(
                    screen = Screen.Interests,
                    currentRoute = currentRoute,
                    imageVector = Icons.Filled.Favorite,
                    badge = interestCount,
                    onClick = { onNavigate(Screen.Interests) }
                )
                DockItem(
                    screen = Screen.MyPet,
                    currentRoute = currentRoute,
                    imageVector = Icons.Filled.Person,
                    onClick = { onNavigate(Screen.MyPet) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.DockItem(
    screen: Screen,
    currentRoute: String?,
    onClick: () -> Unit,
    imageVector: ImageVector? = null,
    icon: Painter? = null,
    badge: Int = 0
) {
    val selected = currentRoute == screen.route
    val showLabel = LocalDensity.current.fontScale <= 1.15f
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.72f)
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageVector != null -> Icon(
                        imageVector = imageVector,
                        contentDescription = screen.label,
                        tint = if (selected) Color.White else contentColor,
                        modifier = Modifier.size(21.dp)
                    )

                    icon != null -> Icon(
                        painter = icon,
                        contentDescription = screen.label,
                        tint = if (selected) Color.White else contentColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            if (badge > 0) {
                Surface(
                    shape = CircleShape,
                    color = CoralAccent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(17.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (badge > 9) "9+" else badge.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (showLabel) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = screen.label,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
private fun RowScope.CenterAdoptItem(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 7.dp)
            .size(56.dp),
        shape = CircleShape,
        color = CoralAccent,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_paw),
                contentDescription = "Adoptar",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
