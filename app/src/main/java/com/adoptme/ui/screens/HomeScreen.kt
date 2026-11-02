@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
            onSpeciesSelected = viewModel::setSpeciesFilter
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    subMessage = errorMessage!!,
                    action = {
                        Button(
                            onClick = viewModel::loadDogs,
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
                            OutlinedButton(onClick = { viewModel.setSpeciesFilter(null) }) {
                                Text("Ver todos")
                            }
                        } else {
                            OutlinedButton(onClick = viewModel::loadDogs) {
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                    translationY = 18f
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
                Spacer(Modifier.height(4.dp))
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
    onSpeciesSelected: (AnimalSpecies?) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🐾", fontSize = 20.sp)
                    Text(
                        text = "AdoptMe",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (remaining > 0) "$remaining por descubrir" else "Catálogo al día",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
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
            }
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
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.labelLarge
            )
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = null
    )
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
    val rotation = (dragOffset / 22f).coerceIn(-14f, 14f)
    val fallbackColor = remember(dog.backgroundColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(dog.backgroundColorHex)) }
            .getOrDefault(Color(0xFFEFEEE4))
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBackground) 0.dp else 3.dp)
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
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(26.dp)
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
                                Text(dog.emoji, fontSize = 72.sp)
                            }
                        },
                        success = {
                            SubcomposeAsyncImageContent()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.28f)
                                    ),
                                    startY = 220f
                                )
                            )
                    )
                } else {
                    Text(dog.emoji, fontSize = 80.sp)
                }

                if (dog.source == AnimalSource.HUACHITOS) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = "Huachitos.cl",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (dog.distanceKm != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        DistanceBadge(dog.distanceKm)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dog.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = dog.species.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("📍", fontSize = 11.sp)
                    Text(
                        text = dog.location.ifBlank { "Ubicación no informada" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

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
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Ver ficha completa  →",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (dog.source == AnimalSource.HUACHITOS) {
                    SourceAttribution(
                        sourceName = "Huachitos.cl",
                        sourceUrl = dog.sourceUrl
                    )
                }
            }
        }
    }
}
