package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adoptme.core.flags.Feature
import com.adoptme.core.flags.FeatureFlags
import com.adoptme.ui.components.SectionTitle

@Composable
fun MoreHubScreen(
    onFeatureClick: (Feature) -> Unit,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 18.dp, top = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = "Más",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Funcionalidades en preparación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Datos reales, decisiones reales",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "El catálogo se alimenta en vivo desde Huachitos.cl. No mostramos mascotas simuladas: si un dato no existe, aparece como \"No informado\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item {
                SectionTitle("Comunidad y servicios")
            }

            item {
                MoreHubItem(
                    title = "Chat interno",
                    description = "Mensajería con adoptantes y organizaciones",
                    emoji = "💬",
                    feature = Feature.CHAT,
                    onFeatureClick = onFeatureClick
                )
            }
            item {
                MoreHubItem(
                    title = "Comunidad",
                    description = "Red de rescatistas y adopciones responsables",
                    emoji = "👥",
                    feature = Feature.COMMUNITY,
                    onFeatureClick = onFeatureClick
                )
            }
            item {
                MoreHubItem(
                    title = "Tienda y donaciones",
                    description = "Aportes para fundaciones y refugios",
                    emoji = "🛍️",
                    feature = Feature.STORE,
                    onFeatureClick = onFeatureClick
                )
            }
            item {
                MoreHubItem(
                    title = "Logros y medallas",
                    description = "Reconocimiento a acciones responsables",
                    emoji = "🏆",
                    feature = Feature.GAMIFICATION,
                    onFeatureClick = onFeatureClick
                )
            }
            item {
                MoreHubItem(
                    title = "Multi-país",
                    description = "Localización y nuevas regiones",
                    emoji = "🌎",
                    feature = Feature.MULTI_COUNTRY,
                    onFeatureClick = onFeatureClick
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "AdoptMe · Catálogo por Huachitos.cl",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MoreHubItem(
    title: String,
    description: String,
    emoji: String,
    feature: Feature,
    onFeatureClick: (Feature) -> Unit
) {
    val enabled = FeatureFlags.isEnabled(feature)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFeatureClick(feature) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (enabled) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (!enabled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Próximamente",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
