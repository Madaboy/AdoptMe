package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.adoptme.data.model.AnimalSource
import com.adoptme.data.model.Dog
import com.adoptme.ui.components.*
import com.adoptme.viewmodel.MyAdoptionsViewModel

@Composable
fun MyAdoptionsScreen(
    viewModel: MyAdoptionsViewModel,
    onExplore: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    val interestedDogs by viewModel.interestedDogs.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var detailDog by remember { mutableStateOf<Dog?>(null) }

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
        ScreenHeader(
            title = "Intereses",
            subtitle = "${interestedDogs.size} mascota${if (interestedDogs.size == 1) "" else "s"} guardada${if (interestedDogs.size == 1) "" else "s"}"
        )

        if (interestedDogs.isEmpty()) {
            EmptyState(
                message = "Aún no tienes intereses",
                subMessage = "Desliza a la derecha o toca la huella en Adoptar para guardar una mascota.",
                action = {
                    Button(
                        onClick = onExplore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Explorar mascotas")
                    }
                }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(interestedDogs, key = { "${it.source}-${it.id}" }) { dog ->
                    InterestedDogCard(
                        dog = dog,
                        onClick = { detailDog = dog },
                        onRemove = { viewModel.removeInterest(dog) }
                    )
                }
            }
        }
    }

    val selectedDog = detailDog
    if (selectedDog != null) {
        DogDetailSheet(
            dog = selectedDog,
            isInterested = true,
            onDismiss = { detailDog = null },
            onToggleInterest = {
                viewModel.removeInterest(selectedDog)
                detailDog = null
            }
        )
    }
}

@Composable
fun InterestedDogCard(dog: Dog, onClick: () -> Unit = {}, onRemove: () -> Unit) {
    val fallbackColor = remember(dog.backgroundColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(dog.backgroundColorHex)) }
            .getOrDefault(Color(0xFFEFEEE4))
    }

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
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
                                    modifier = Modifier.size(22.dp)
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
                                Text(dog.emoji, fontSize = 40.sp)
                            }
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                } else {
                    Text(dog.emoji, fontSize = 40.sp)
                }

                if (dog.source == AnimalSource.HUACHITOS) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = "Huachitos.cl",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.92f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = dog.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dog.location.ifBlank { "Ubicación no informada" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                dog.age?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onRemove,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Quitar de intereses",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
