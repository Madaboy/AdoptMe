@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.adoptme.R
import com.adoptme.data.model.AnimalSource
import com.adoptme.data.model.Dog
import com.adoptme.ui.components.InfoChip
import com.adoptme.ui.components.SourceAttribution

/**
 * Ficha completa de una mascota.
 *
 * Se muestra como hoja inferior para no añadir una ruta de navegación nueva.
 * Todos los campos que la fuente no entrega se muestran como "No informado":
 * nunca se rellenan con datos inventados.
 */
@Composable
fun DogDetailSheet(
    dog: Dog,
    isInterested: Boolean,
    onDismiss: () -> Unit,
    onToggleInterest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            item {
                DetailPhotoHeader(dog = dog, onClose = onDismiss)
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DetailTitle(dog)

                    if (dog.description.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Sobre ${dog.name}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dog.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = if (dog.source == AnimalSource.HUACHITOS) {
                                "La fuente no entrega una descripción de ${dog.name}."
                            } else {
                                "Quien publica no agregó una descripción de ${dog.name}."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Ficha",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        DetailRow("Especie", dog.species.label)
                        DetailRow("Edad", dog.age ?: "No informado")
                        DetailRow("Sexo", dog.gender?.replaceFirstChar { it.uppercase() } ?: "No informado")
                        DetailRow("Raza", dog.breed ?: "No informado")
                        DetailRow("Tamaño", dog.size ?: "No informado")
                        DetailRow("Energía", dog.energyLevel ?: "No informado")
                        DetailRow("Ubicación", dog.location.ifBlank { "No informado" })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Salud y convivencia",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dog.vaccinated?.let { vaccinated ->
                                InfoChip(if (vaccinated) "✓ Vacunado" else "Vacunas pendientes")
                            }
                            dog.neutered?.let { neutered ->
                                InfoChip(if (neutered) "✓ Esterilizado" else "Sin esterilizar")
                            }
                            dog.houseTrained?.let { trained ->
                                InfoChip(if (trained) "🏠 Educado" else "En entrenamiento")
                            }
                            dog.goodWithKids?.let { value ->
                                InfoChip(if (value) "👶 Bueno con niños" else "Mejor sin niños")
                            }
                            dog.goodWithDogs?.let { value ->
                                InfoChip(if (value) "🐕 Se lleva con perros" else "Mejor sin perros")
                            }
                            dog.goodWithCats?.let { value ->
                                InfoChip(if (value) "🐈 Se lleva con gatos" else "Mejor sin gatos")
                            }
                        }

                        val noHealthData = dog.vaccinated == null &&
                            dog.neutered == null &&
                            dog.houseTrained == null &&
                            dog.goodWithKids == null &&
                            dog.goodWithDogs == null &&
                            dog.goodWithCats == null
                        if (noHealthData) {
                            Text(
                                text = if (dog.source == AnimalSource.HUACHITOS) {
                                    "La fuente no informa antecedentes de salud ni convivencia."
                                } else {
                                    "No se informaron antecedentes de salud ni convivencia."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        dog.specialNeeds?.let { needs ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "⚠️ Necesidades especiales: $needs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    if (dog.source == AnimalSource.HUACHITOS) {
                        SourceAttribution(
                            sourceName = "Huachitos.cl",
                            sourceUrl = dog.sourceUrl
                        )
                    }

                    DetailAction(
                        dog = dog,
                        isInterested = isInterested,
                        onToggleInterest = onToggleInterest
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailPhotoHeader(dog: Dog, onClose: () -> Unit) {
    val fallbackColor = remember(dog.backgroundColorHex) {
        runCatching {
            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(dog.backgroundColorHex))
        }.getOrDefault(androidx.compose.ui.graphics.Color(0xFFEFEEE4))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(264.dp)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        if (dog.imageUrl != null) {
            SubcomposeAsyncImage(
                model = dog.imageUrl,
                contentDescription = "Foto de ${dog.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(fallbackColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(dog.emoji, fontSize = 76.sp)
                    }
                },
                success = { SubcomposeAsyncImageContent() }
            )
        } else {
            Text(dog.emoji, fontSize = 84.sp)
        }

        Surface(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(38.dp),
            shape = CircleShape,
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar ficha",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (dog.source == AnimalSource.HUACHITOS) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f)
            ) {
                Text(
                    text = "Huachitos.cl",
                    style = MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailTitle(dog: Dog) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = dog.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = buildString {
                    append(dog.species.label)
                    dog.breed?.let { append(" · $it") }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (dog.isMine) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Tu publicación",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun DetailAction(
    dog: Dog,
    isInterested: Boolean,
    onToggleInterest: () -> Unit
) {
    if (dog.isMine) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "🐾 Esta ficha es tu publicación",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(14.dp)
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isInterested) {
            OutlinedButton(
                onClick = onToggleInterest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Quitar de intereses",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onToggleInterest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_paw),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "Me interesa adoptar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "Los intereses se guardan en este dispositivo. La adopción se coordina con la fuente o con quien publica.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(13.dp)
            )
        }
    }
}
