@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.adoptme.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.adoptme.R
import com.adoptme.core.di.AppContainer
import com.adoptme.data.model.AnimalSource
import com.adoptme.data.model.Dog
import com.adoptme.ui.components.InfoChip
import com.adoptme.ui.components.SourceAttribution
import com.adoptme.ui.theme.Ink
import com.adoptme.ui.theme.CoralAccent

private val LavenderCard = Color(0xFFE9E3FF)
private val ButterCard = Color(0xFFFFF1C9)
private val MintCard = Color(0xFFDDF5EA)

/**
 * Ficha completa de una mascota en formato carta:
 * foto grande, tres tarjetas pastel, descripción y acciones redondeadas.
 */
@Composable
fun DogDetailSheet(
    dog: Dog,
    isInterested: Boolean,
    onDismiss: () -> Unit,
    onToggleInterest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var gallery by remember(dog.sourceUrl, dog.imageUrl) {
        mutableStateOf(listOfNotNull(dog.imageUrl))
    }
    val images = gallery.ifEmpty { listOfNotNull(dog.imageUrl) }
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })
    var fullscreenPage by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(dog.sourceUrl, dog.imageUrl) {
        gallery = AppContainer.repository.fetchGallery(dog)
            .ifEmpty { listOfNotNull(dog.imageUrl) }
    }

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
                DetailPhotoHeader(
                    dog = dog,
                    images = images,
                    pagerState = pagerState,
                    isInterested = isInterested,
                    onClose = onDismiss,
                    onToggleInterest = onToggleInterest,
                    onOpenImage = { fullscreenPage = it }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    DetailTitle(dog)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailInfoCard(
                            label = "Sexo",
                            value = dog.gender?.replaceFirstChar { it.uppercase() } ?: "No informado",
                            color = LavenderCard,
                            modifier = Modifier.weight(1f)
                        )
                        DetailInfoCard(
                            label = "Edad",
                            value = dog.age ?: "No informado",
                            color = ButterCard,
                            modifier = Modifier.weight(1f)
                        )
                        DetailInfoCard(
                            label = "Raza",
                            value = dog.breed ?: dog.species.label,
                            color = MintCard,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Sobre ${dog.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dog.description.ifBlank {
                                if (dog.source == AnimalSource.HUACHITOS) {
                                    "La fuente no entrega una descripción de ${dog.name}."
                                } else {
                                    "Quien publica no agregó una descripción de ${dog.name}."
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Ficha",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            DetailRow("Especie", dog.species.label)
                            DetailRow("Tamaño", dog.size ?: "No informado")
                            DetailRow("Energía", dog.energyLevel ?: "No informado")
                            DetailRow("Ubicación", dog.location.ifBlank { "No informado" })
                        }
                    }

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

                    DetailActions(
                        dog = dog,
                        isInterested = isInterested,
                        onToggleInterest = onToggleInterest
                    )
                }
            }
        }
    }

    val fullscreen = fullscreenPage
    if (fullscreen != null && images.isNotEmpty()) {
        FullScreenGalleryDialog(
            images = images,
            initialPage = fullscreen.coerceIn(0, images.lastIndex),
            onDismiss = { fullscreenPage = null }
        )
    }
}

@Composable
private fun DetailPhotoHeader(
    dog: Dog,
    images: List<String>,
    pagerState: PagerState,
    isInterested: Boolean,
    onClose: () -> Unit,
    onToggleInterest: () -> Unit,
    onOpenImage: (Int) -> Unit
) {
    val fallbackColor = remember(dog.backgroundColorHex) {
        runCatching {
            Color(android.graphics.Color.parseColor(dog.backgroundColorHex))
        }.getOrDefault(Color(0xFFE9EDF4))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        if (images.isEmpty()) {
            Text(dog.emoji, fontSize = 84.sp)
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = images.size > 1
            ) { page ->
                val image = images.getOrNull(page)
                if (image != null) {
                    SubcomposeAsyncImage(
                        model = image,
                        contentDescription = "Foto ${page + 1} de ${dog.name}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onOpenImage(page) },
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
                }
            }

            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(if (index == pagerState.currentPage) 18.dp else 7.dp)
                                .background(
                                    if (index == pagerState.currentPage) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.55f)
                                    },
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        }

        Surface(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(42.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.94f),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Ink,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        if (!dog.isMine) {
            Surface(
                onClick = onToggleInterest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(42.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isInterested) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isInterested) {
                            "Quitar de intereses"
                        } else {
                            "Guardar en intereses"
                        },
                        tint = if (isInterested) CoralAccent else Ink,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.92f)
            ) {
                Text(
                    text = "Tu publicación",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailTitle(dog: Dog) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = "Nombre",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = dog.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📍", fontSize = 12.sp)
            Text(
                text = buildString {
                    append(dog.location.ifBlank { "Ubicación no informada" })
                    dog.distanceKm?.let { append(" · ${"%.1f".format(it)} km") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailInfoCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color,
        modifier = modifier.height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Ink.copy(alpha = 0.62f)
            )
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
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
private fun DetailActions(
    dog: Dog,
    isInterested: Boolean,
    onToggleInterest: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!dog.sourceUrl.isNullOrBlank()) {
            CircleDetailButton(
                contentDescription = "Ver original en Huachitos.cl",
                onClick = { runCatching { uriHandler.openUri(dog.sourceUrl) } }
            ) {
                Text("↗", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        CircleDetailButton(
            contentDescription = "Compartir ficha",
            onClick = {
                val text = buildString {
                    append("${dog.name} · ${dog.species.label}")
                    if (dog.location.isNotBlank()) append(" · ${dog.location}")
                    dog.sourceUrl?.let { append("\n$it") }
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir ficha"))
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        if (dog.isMine) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Tu publicación",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }
        } else if (isInterested) {
            OutlinedButton(
                onClick = onToggleInterest,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = CoralAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "Quitar interés",
                    color = CoralAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onToggleInterest,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "Me interesa",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CircleDetailButton(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(52.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun FullScreenGalleryDialog(
    images: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { images.size }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = images.size > 1
            ) { page ->
                SubcomposeAsyncImage(
                    model = images[page],
                    contentDescription = "Foto ${page + 1} de ${images.size}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se pudo cargar la imagen",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    success = { SubcomposeAsyncImageContent() }
                )
            }

            Surface(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(44.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar imagen",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(if (index == pagerState.currentPage) 18.dp else 7.dp)
                                .background(
                                    if (index == pagerState.currentPage) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.45f)
                                    },
                                    RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        }
    }
}
