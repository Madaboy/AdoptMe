@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.adoptme.R
import com.adoptme.data.model.AnimalSource
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
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val interestedDogs by viewModel.interestedDogs.collectAsState()

    var detailDog by remember { mutableStateOf<Dog?>(null) }

    val currentDog = dogs.getOrNull(currentIndex)

    LaunchedEffect(toastMessage) {
        val message = toastMessage
        if (message != null) {
            onShowToast(message)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Buscando mascotas en Huachitos.cl…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    message = "No hay más mascotas por ahora",
                    subMessage = "Huachitos.cl publica nuevos casos cada semana."
                )
            }

            else -> {
                DogCard(
                    dog = currentDog,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    onLike = viewModel::onLike,
                    onPass = viewModel::onPass,
                    onOpenDetail = { detailDog = currentDog }
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
fun DogCard(
    dog: Dog,
    modifier: Modifier = Modifier,
    onLike: () -> Unit,
    onPass: () -> Unit,
    onOpenDetail: () -> Unit = {}
) {
    var dragOffset by remember { mutableStateOf(0f) }
    val rotation = (dragOffset / 24f).coerceIn(-12f, 12f)
    val fallbackColor = remember(dog.backgroundColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(dog.backgroundColorHex)) }
            .getOrDefault(Color(0xFFE9EDF4))
    }

    Card(
        modifier = modifier
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
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fallbackColor)
                    .clickable(onClick = onOpenDetail)
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
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.45f to Color.Transparent,
                                0.72f to Color.Black.copy(alpha = 0.18f),
                                1f to Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_paw),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "AdoptMe",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            if (dog.source == AnimalSource.HUACHITOS) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Huachitos.cl",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp, bottom = 106.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dog.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    dog.age?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 1
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${dog.location.ifBlank { "Ubicación no informada" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    dog.distanceKm?.let {
                        Text(
                            text = "· ${"%.1f".format(it)} km",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OverlayChip(dog.species.label)
                    dog.gender?.let { OverlayChip(it.replaceFirstChar { c -> c.uppercase() }) }
                    dog.size?.let { OverlayChip(it) }
                    dog.energyLevel?.let { OverlayChip("⚡ $it") }
                    dog.vaccinated?.let { OverlayChip(if (it) "✓ Vacunado" else "Vacunas pendientes") }
                    if (dog.neutered == true) OverlayChip("Esterilizado")
                }

                TextButton(
                    onClick = onOpenDetail,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text(
                        text = "Ver ficha completa  →",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            ActionButtons(
                onPass = onPass,
                onLike = onLike,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun OverlayChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
