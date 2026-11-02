package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.adoptme.data.model.Dog
import com.adoptme.ui.components.EmptyState
import com.adoptme.ui.components.SectionTitle
import com.adoptme.ui.components.StatusChip
import com.adoptme.viewmodel.MyPetViewModel

@Composable
fun MyPetScreen(
    viewModel: MyPetViewModel,
    onPublish: () -> Unit,
    onOpenMore: () -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val myPets by viewModel.myPets.collectAsState()
    var detailDog by remember { mutableStateOf<Dog?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mi Mascota",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (myPets.isEmpty()) {
                        "Tu espacio para dar en adopción"
                    } else {
                        "${myPets.size} publicación${if (myPets.size == 1) "" else "es"} activa${if (myPets.size == 1) "" else "s"}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenMore) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Más opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (myPets.isEmpty()) {
            EmptyState(
                message = "Aún no registras una mascota",
                subMessage = "Publica su ficha para que una familia pueda encontrarla. Solo necesitas el nombre y la comuna.",
                action = {
                    Button(
                        onClick = onPublish,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Registrar a mi mascota")
                    }
                }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
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
                                text = "Ficha publicada",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Comparte el perfil de tu mascota con personas de confianza. Las adopciones se coordinan directamente contigo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                item {
                    SectionTitle("Mis publicaciones")
                }

                items(myPets, key = { it.id }) { dog ->
                    MyPetCard(
                        dog = dog,
                        onClick = { detailDog = dog },
                        onShare = {
                            onShowToast("Compartir ${dog.name} estará disponible pronto")
                        }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = onPublish,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Publicar otra mascota")
                    }
                }
            }
        }
    }

    val selectedDog = detailDog
    if (selectedDog != null) {
        DogDetailSheet(
            dog = selectedDog,
            isInterested = false,
            onDismiss = { detailDog = null },
            onToggleInterest = {}
        )
    }
}

@Composable
private fun MyPetCard(dog: Dog, onClick: () -> Unit = {}, onShare: () -> Unit) {
    val fallbackColor = Color(0xFFEFEEE4).let {
        runCatching { Color(android.graphics.Color.parseColor(dog.backgroundColorHex)) }
            .getOrDefault(it)
    }

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(fallbackColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (dog.imageUrl != null) {
                    SubcomposeAsyncImage(
                        model = dog.imageUrl,
                        contentDescription = "Foto de ${dog.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { SubcomposeAsyncImageContent() },
                        error = { Text(dog.emoji, fontSize = 30.sp) },
                        success = { SubcomposeAsyncImageContent() }
                    )
                } else {
                    Text(dog.emoji, fontSize = 32.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dog.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    StatusChip(
                        text = "En adopción",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = buildString {
                        append(dog.species.label)
                        dog.age?.let { append(" · $it") }
                        dog.size?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = dog.location.ifBlank { "Ubicación no informada" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                TextButton(
                    onClick = onShare,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Compartir ficha",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
