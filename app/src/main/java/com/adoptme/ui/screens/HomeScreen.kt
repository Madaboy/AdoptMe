@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.adoptme.data.model.AnimalSource
import com.adoptme.data.model.AnimalSpecies
import com.adoptme.data.model.Dog
import com.adoptme.ui.components.*
import com.adoptme.ui.theme.OrangeAccent
import com.adoptme.viewmodel.HomeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onShowToast: (String) -> Unit = {}
) {
    val dogs by viewModel.dogs.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val speciesFilter by viewModel.speciesFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val interestedDogs by viewModel.interestedDogs.collectAsState()

    var detailDog by remember { mutableStateOf<Dog?>(null) }

    val currentDog = dogs.getOrNull(currentIndex)
    val remaining = (dogs.size - currentIndex).coerceAtLeast(0)

    LaunchedEffect(toastMessage) {
        val message = toastMessage
        if (message != null) {
            onShowToast(message)
            viewModel.clearToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTopBar(
            remaining = remaining,
            selectedSpecies = speciesFilter,
            onSpeciesSelected = viewModel::setSpeciesFilter,
            onRefresh = viewModel::loadDogs
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = "Buscando mascotas en Huachitos.cl…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            errorMessage != null -> {
                EmptyState(
                    message = "No pudimos cargar el catálogo",
                    subMessage = errorMessage ?: "Revisa tu conexión e inténtalo de nuevo.",
                    action = {
                        Button(
                            onClick = viewModel::loadDogs,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                )
            }

            currentDog == null -> {
                EmptyState(
                    message = "No hay más mascotas con este filtro",
                    subMessage = if (speciesFilter != null) {
                        "Prueba con otra especie o vuelve a \"Todos\"."
                    } else {
                        "Vuelve más tarde: Huachitos.cl publica nuevos casos cada semana."
                    },
                    action = {
                        if (speciesFilter != null) {
                            OutlinedButton(
                                onClick = { viewModel.setSpeciesFilter(null) },
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Ver todos")
                            }
                        } else {
                            OutlinedButton(
                                onClick = viewModel::loadDogs,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Actualizar catálogo")
                            }
                        }
                    }
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val nextDog = dogs.getOrNull(currentIndex + 1)
                    if (nextDog != null) {
                        DogCard(
                            dog = nextDog,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = 0.94f,
                                    scaleY = 0.94f,
                                    translationY = 20f,
                                    alpha = 0.62f
                                ),
                            onLike = {},
                            onPass = {},
                            onFavorite = {},
                            onOpenDetail = {},
                            isBackground = true
                        )
                    }
                    DogCard(
                        dog = currentDog,
                        modifier = Modifier.fillMaxSize(),
                        onLike = viewModel::onLike,
                        onPass = viewModel::onPass,
                        onFavorite = viewModel::onFavorite,
                        onOpenDetail = { detailDog = currentDog },
                        isBackground = false
                    )
                }

                ActionButtons(
                    onPass = viewModel::onPass,
                    onLike = viewModel::onLike
                )
            }
        }
    }

    val selectedDog = detailDog
    if (selectedDog != null) {
        val isInterested = interestedDogs.any {
            it.source == selectedDog.source && it.id == selectedDog.id
        }
        DogDetailSheet(
            dog = selectedDog,
            isInterested = isInterested,
            onDismiss = { detailDog = null },
            onToggleInterest = {
                if (isInterested) {
                    viewModel.removeInterest(selectedDog)
                } else {
                    viewModel.saveInterest(selectedDog)
                }
            }
        )
    }
}

@Composable
fun HomeTopBar(
    remaining: Int,
    selectedSpecies: AnimalSpecies?,
    onSpeciesSelected: (AnimalSpecies?) -> Unit,
    onRefresh: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🐾", fontSize = 22.sp)
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AdoptMe",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Mascotas reales",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (remaining > 0) "$remaining" else "0",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "por descubrir",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            SoftIconButton(
                icon = Icons.Filled.Refresh,
                contentDescription = "Actualizar catálogo",
                onClick = onRefresh
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeciesChip(
                label = "Todos",
                emoji = "🐾",
                selected = selectedSpecies == null,
                onClick = { onSpeciesSelected(null) }
            )
            AnimalSpecies.entries.forEach { species ->
                SpeciesChip(
                    label = species.label + "s",
                    emoji = species.emoji,
                    selected = selectedSpecies == species,
                    onClick = { onSpeciesSelected(species) }
                )
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun SpeciesChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        shadowElevation = if (selected) 0.dp else 1.dp
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}

@Composable
fun DogCard(
    dog: Dog,
    modifier: Modifier = Modifier,
    onLike: () -> Unit,
    onPass: () -> Unit,
    onFavorite: () -> Unit,
    onOpenDetail: () -> Unit = {},
    isBackground: Boolean
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val rotation = (dragOffset / 24f).coerceIn(-12f, 12f)
    val fallbackColor = remember(dog.backgroundColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(dog.backgroundColorHex)) }
            .getOrDefault(Color(0xFFE9EDF4))
    }

    Card(
        modifier = modifier
            .then(
                if (!isBackground) {
                    Modifier
                        .graphicsLayer(
                            translationX = dragOffset,
                            rotationZ = rotation
                        )
                        .pointerInput(dog.id) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragOffset > 100f) onLike()
                                    else if (dragOffset < -100f) onPass()
                                    dragOffset = 0f
                                },
                                onHorizontalDrag = { _, delta ->
                                    dragOffset += delta
                                }
                            )
                        }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isBackground) 0.dp else 5.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(
                        if (!isBackground) {
                            Modifier.clickable(onClick = onOpenDetail)
                        } else {
                            Modifier
                        }
                    )
                    .background(fallbackColor)
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
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp,
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(dog.emoji, fontSize = 86.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.72f)
                                ),
                                startY = 80f
                            )
                        )
                )

                if (dog.source == AnimalSource.HUACHITOS) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.42f)
                    ) {
                        Text(
                            text = "Huachitos.cl",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                        )
                    }
                }

                if (dog.distanceKm != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                    ) {
                        DistanceBadge(dog.distanceKm)
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = dog.species.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = dog.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dog.location.ifBlank { "Ubicación no informada" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dog.age?.let { InfoChip(it) }
                    dog.gender?.let { InfoChip(it.replaceFirstChar { c -> c.uppercase() }) }
                    dog.size?.let {
                        InfoChip(
                            text = it,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    dog.energyLevel?.let {
                        InfoChip(
                            text = "⚡ $it",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            textColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    dog.vaccinated?.let { vaccinated ->
                        InfoChip(if (vaccinated) "✓ Vacunado" else "Vacunas pendientes")
                    }
                    if (dog.neutered == true) InfoChip("Esterilizado")
                    if (dog.goodWithKids == true) InfoChip("👶 Niños")
                    if (dog.goodWithDogs == true) InfoChip("🐕 Perros")
                    if (dog.goodWithCats == true) InfoChip("🐈 Gatos")
                    if (dog.houseTrained == true) InfoChip("🏠 Educado")
                }

                if (dog.description.isNotBlank()) {
                    ExpandableText(
                        text = dog.description,
                        collapsedMaxLines = 2
                    )
                }

                dog.specialNeeds?.let {
                    Text(
                        text = "⚠️ $it",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!isBackground) {
                    TextButton(
                        onClick = onOpenDetail,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Ver ficha completa",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (dog.source == AnimalSource.HUACHITOS) {
                    SourceAttribution(sourceName = "Huachitos.cl", sourceUrl = dog.sourceUrl)
                }
            }
        }
    }
}
