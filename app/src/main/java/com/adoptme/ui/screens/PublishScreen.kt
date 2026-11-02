package com.adoptme.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adoptme.R
import com.adoptme.core.media.PetImageStore
import com.adoptme.data.model.AnimalSpecies
import com.adoptme.ui.components.*
import com.adoptme.viewmodel.PublishViewModel
import kotlinx.coroutines.launch

@Composable
fun PublishScreen(
    viewModel: PublishViewModel,
    onClose: () -> Unit = {},
    onPublished: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf(AnimalSpecies.DOG) }
    var age by rememberSaveable { mutableStateOf("") }
    var breed by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var specialNeeds by rememberSaveable { mutableStateOf("") }
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var isPublishing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) selectedPhotoUri = uri
    }

    var vaccinated by rememberSaveable { mutableStateOf(false) }
    var neutered by rememberSaveable { mutableStateOf(false) }
    var houseTrained by rememberSaveable { mutableStateOf(false) }

    var goodWithKids by rememberSaveable { mutableStateOf(true) }
    var goodWithDogs by rememberSaveable { mutableStateOf(true) }
    var goodWithCats by rememberSaveable { mutableStateOf(true) }

    var size by rememberSaveable { mutableStateOf("Mediano") }
    var energyLevel by rememberSaveable { mutableStateOf("Media") }

    val toastMessage by viewModel.toastMessage.collectAsState()
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = "Dar en adopción",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Una ficha clara encuentra hogar más rápido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PhotoPickerSection(
                photoUri = selectedPhotoUri,
                onPick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemove = { selectedPhotoUri = null }
            )

            AdoptMeCardSection(title = "Ficha básica") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Especie",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SegmentedControl(
                        options = AnimalSpecies.entries.map { it.label },
                        selectedOption = species.label,
                        onOptionSelected = { selected ->
                            species = AnimalSpecies.entries.firstOrNull { it.label == selected }
                                ?: AnimalSpecies.DOG
                        }
                    )
                }

                AdoptMeTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre *",
                    placeholder = "Ej: Firulais"
                )
                AdoptMeTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = "Edad",
                    placeholder = "Ej: 2 años"
                )
                AdoptMeTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = "Raza / Tipo",
                    placeholder = "Ej: Mestizo"
                )
                AdoptMeTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Comuna / Ubicación *",
                    placeholder = "Ej: Providencia, Santiago",
                    trailingIcon = {
                        Text("📍", modifier = Modifier.padding(end = 10.dp))
                    }
                )
            }

            AdoptMeCardSection(title = "Sobre su carácter") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tamaño",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SegmentedControl(
                        options = listOf("Pequeño", "Mediano", "Grande"),
                        selectedOption = size,
                        onOptionSelected = { size = it }
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nivel de energía",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SegmentedControl(
                        options = listOf("Baja", "Media", "Alta"),
                        selectedOption = energyLevel,
                        onOptionSelected = { energyLevel = it }
                    )
                }

                AdoptMeTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Descripción",
                    placeholder = "Cuéntanos cómo es, qué le gusta y qué hogar necesita…",
                    singleLine = false,
                    height = 110
                )
            }

            AdoptMeCardSection(title = "Salud y cuidados") {
                AdoptMeSwitchRow("¿Está vacunado/a?", vaccinated) { vaccinated = it }
                AdoptMeSwitchRow("¿Está esterilizado/a?", neutered) { neutered = it }
                AdoptMeSwitchRow("¿Sabe hacer sus necesidades fuera?", houseTrained) { houseTrained = it }
                AdoptMeTextField(
                    value = specialNeeds,
                    onValueChange = { specialNeeds = it },
                    label = "Necesidades especiales (opcional)",
                    placeholder = "Ej: medicación, dieta, movilidad…"
                )
            }

            AdoptMeCardSection(title = "Convivencia") {
                AdoptMeSwitchRow("¿Es bueno/a con niños?", goodWithKids) { goodWithKids = it }
                AdoptMeSwitchRow("¿Se lleva bien con perros?", goodWithDogs) { goodWithDogs = it }
                AdoptMeSwitchRow("¿Se lleva bien con gatos?", goodWithCats) { goodWithCats = it }
            }

            Text(
                text = "Solo el nombre y la comuna son obligatorios. Mientras más información real completes, más fácil será encontrar el hogar correcto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    if (name.isBlank() || location.isBlank()) {
                        onShowToast("Completa el nombre y la comuna para publicar")
                    } else if (!isPublishing) {
                        isPublishing = true
                        coroutineScope.launch {
                            try {
                                val imagePath = selectedPhotoUri?.let { uri ->
                                    runCatching {
                                        PetImageStore.copyToInternalStorage(context, uri)
                                    }.getOrElse { error ->
                                        if (error is kotlinx.coroutines.CancellationException) throw error
                                        onShowToast("No pudimos guardar la foto. Intenta con otra imagen.")
                                        return@launch
                                    }
                                }

                                val published = viewModel.publishDog(
                                    name = name,
                                    species = species,
                                    age = age,
                                    breed = breed,
                                    location = location,
                                    description = description,
                                    vaccinated = vaccinated,
                                    neutered = neutered,
                                    size = size,
                                    energyLevel = energyLevel,
                                    goodWithKids = goodWithKids,
                                    goodWithDogs = goodWithDogs,
                                    goodWithCats = goodWithCats,
                                    houseTrained = houseTrained,
                                    specialNeeds = specialNeeds.ifBlank { null },
                                    imageUrl = imagePath
                                )
                                if (published) {
                                    onShowToast("🐾 ${name.trim()} ya está publicado para adopción")
                                    onPublished()
                                }
                            } finally {
                                isPublishing = false
                            }
                        }
                    }
                },
                enabled = !isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Publicando…",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_paw),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Publicar en adopción",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PhotoPickerSection(
    photoUri: Uri?,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Foto de tu mascota",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto seleccionada de la mascota",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Quitar foto",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📷", fontSize = 28.sp)
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Agregar foto",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "JPG, PNG o WEBP · máximo 15 MB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (photoUri != null) {
            TextButton(
                onClick = onPick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Cambiar foto",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Surface(
                onClick = { onOptionSelected(option) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    androidx.compose.ui.graphics.Color.Transparent
                },
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
