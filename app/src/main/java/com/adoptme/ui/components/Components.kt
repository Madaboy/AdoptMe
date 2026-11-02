package com.adoptme.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// -------------------------------------------------------------------------
// HEADER de pantalla
// -------------------------------------------------------------------------
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing?.invoke()
        }
    }
}

// -------------------------------------------------------------------------
// TEXT FIELD estilizado
// -------------------------------------------------------------------------
@Composable
fun AdoptMeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    height: Int? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (height != null) Modifier.height(height.dp) else Modifier),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = singleLine,
            shape = MaterialTheme.shapes.small,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// -------------------------------------------------------------------------
// SWITCH ROW estilizado
// -------------------------------------------------------------------------
@Composable
fun AdoptMeSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// -------------------------------------------------------------------------
// CARD SECTION para formularios
// -------------------------------------------------------------------------
@Composable
fun AdoptMeCardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

// -------------------------------------------------------------------------
// CHIP de información (edad, vacunas, etc.)
// -------------------------------------------------------------------------
@Composable
fun InfoChip(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun StatusChip(text: String, containerColor: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(50), color = containerColor) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// -------------------------------------------------------------------------
// BADGE de distancia
// -------------------------------------------------------------------------
@Composable
fun DistanceBadge(distanceKm: Double) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("📍", fontSize = 10.sp)
            Text(
                text = "${"%.1f".format(distanceKm)} km",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// -------------------------------------------------------------------------
// TEXTO EXPANDIBLE (descripciones largas de Huachitos)
// -------------------------------------------------------------------------
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 3,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var expanded by remember(text) { mutableStateOf(false) }
    var hasOverflow by remember(text) { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout ->
                if (!expanded) hasOverflow = layout.hasVisualOverflow
            }
        )
        if (hasOverflow || expanded) {
            Text(
                text = if (expanded) "Ver menos" else "Leer más",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

// -------------------------------------------------------------------------
// ATRIBUCIÓN de la fuente externa
// -------------------------------------------------------------------------
@Composable
fun SourceAttribution(sourceName: String, sourceUrl: String?) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Fuente: $sourceName",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!sourceUrl.isNullOrBlank()) {
            Text(
                text = "Ver original ↗",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    runCatching { uriHandler.openUri(sourceUrl) }
                }
            )
        }
    }
}

// -------------------------------------------------------------------------
// TOAST — Mensaje temporal en pantalla
// -------------------------------------------------------------------------
@Composable
fun AdoptMeToast(
    message: String?,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(2400)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.inverseSurface)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )
        }
    }
}

// -------------------------------------------------------------------------
// BOTÓN CIRCULAR de acción
// -------------------------------------------------------------------------
@Composable
fun CircleActionButton(
    emoji: String,
    size: Int = 52,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        modifier = Modifier.size(size.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = (size / 2.5).sp)
        }
    }
}

// -------------------------------------------------------------------------
// BOTONES DE ACCIÓN (Home)
// -------------------------------------------------------------------------
@Composable
fun ActionButtons(onPass: () -> Unit, onLike: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleActionButton(
            emoji = "✕",
            size = 58,
            onClick = onPass
        )

        Spacer(Modifier.width(36.dp))

        Surface(
            onClick = onLike,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp,
            modifier = Modifier.size(70.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.adoptme.R.drawable.ic_paw),
                    contentDescription = "Me interesa adoptar",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// ESTADO VACÍO
// -------------------------------------------------------------------------
@Composable
fun EmptyState(
    message: String = "No hay más mascotas cercanas",
    subMessage: String = "Prueba con otro filtro",
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🐾", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) {
            Spacer(Modifier.height(20.dp))
            action()
        }
    }
}

// -------------------------------------------------------------------------
// SECCIÓN TÍTULO
// -------------------------------------------------------------------------
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
