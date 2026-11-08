@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.adoptme.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adoptme.R
import com.adoptme.core.media.PetImageStore
import com.adoptme.data.model.AnimalSpecies
import com.adoptme.ui.components.AdoptMeSwitchRow
import com.adoptme.ui.components.AdoptMeTextField
import com.adoptme.ui.theme.CoralPrimary
import com.adoptme.viewmodel.PublishViewModel
import kotlinx.coroutines.launch

/**
 * Ficha de adopción por pasos:
 * 1) Fotos · 2) Descripción y opciones · 3) Confirmación y casillas legales.
 * Solo front-end visual tipo "liquid glass" (no es blur real).
 */
@Composable
fun PublishScreen(
    viewModel: PublishViewModel,
    onClose: () -> Unit = {},
    onPublished: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    var step by rememberSaveable { mutableStateOf(0) }
    val photos = remember { mutableStateListOf<Uri>() }

    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf(AnimalSpecies.DOG) }
    var age by rememberSaveable { mutableStateOf("") }
    var breed by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var specialNeeds by rememberSaveable { mutableStateOf("") }

    var vaccinated by rememberSaveable { mutableStateOf(false) }
    var neutered by rememberSaveable { mutableStateOf(false) }
    var houseTrained by rememberSaveable { mutableStateOf(false) }
    var goodWithKids by rememberSaveable { mutableStateOf(true) }
    var goodWithDogs by rememberSaveable { mutableStateOf(true) }
    var goodWithCats by rememberSaveable { mutableStateOf(true) }
    var size by rememberSaveable { mutableStateOf("Mediano") }
    var energyLevel by rememberSaveable { mutableStateOf("Media") }

    var veracity by rememberSaveable { mutableStateOf(false) }
    var responsible by rememberSaveable { mutableStateOf(false) }
    var photoConsent by rememberSaveable { mutableStateOf(false) }
    var notSale by rememberSaveable { mutableStateOf(false) }

    var isPublishing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        uris.forEach { uri ->
            if (photos.size < 6 && photos.none { it == uri }) {
                photos.add(uri)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFCE9E4),
                        Color(0xFFF7F5F3),
                        Color(0xFFEDF1F6)
                    )
                )
            )
    ) {
        GlassBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            WizardHeader(
                step = step,
                onClose = onClose
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (step) {
                    0 -> PhotoStep(
                        photos = photos,
                        onAdd = {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onRemove = { photos.remove(it) }
                    )

                    1 -> DescriptionStep(
                        name = name,
                        onNameChange = { name = it },
                        species = species,
                        onSpeciesChange = { species = it },
                        age = age,
                        onAgeChange = { age = it },
                        breed = breed,
                        onBreedChange = { breed = it },
                        location = location,
                        onLocationChange = { location = it },
                        description = description,
                        onDescriptionChange = { description = it },
                        specialNeeds = specialNeeds,
                        onSpecialNeedsChange = { specialNeeds = it },
                        size = size,
                        onSizeChange = { size = it },
                        energyLevel = energyLevel,
                        onEnergyLevelChange = { energyLevel = it },
                        vaccinated = vaccinated,
                        onVaccinatedChange = { vaccinated = it },
                        neutered = neutered,
                        onNeuteredChange = { neutered = it },
                        houseTrained = houseTrained,
                        onHouseTrainedChange = { houseTrained = it },
                        goodWithKids = goodWithKids,
                        onGoodWithKidsChange = { goodWithKids = it },
                        goodWithDogs = goodWithDogs,
                        onGoodWithDogsChange = { goodWithDogs = it },
                        goodWithCats = goodWithCats,
                        onGoodWithCatsChange = { goodWithCats = it }
                    )

                    else -> ConfirmStep(
                        name = name,
                        species = species,
                        location = location,
                        photoCount = photos.size,
                        veracity = veracity,
                        onVeracityChange = { veracity = it },
                        responsible = responsible,
                        onResponsibleChange = { responsible = it },
                        photoConsent = photoConsent,
                        onPhotoConsentChange = { photoConsent = it },
                        notSale = notSale,
                        onNotSaleChange = { notSale = it }
                    )
                }
            }

            WizardFooter(
                step = step,
                canContinue = when (step) {
                    0 -> photos.isNotEmpty()
                    1 -> name.isNotBlank() && location.isNotBlank()
                    else -> veracity && responsible && photoConsent && notSale
                },
                isPublishing = isPublishing,
                onBack = { step = (step - 1).coerceAtLeast(0) },
                onContinue = {
                    when (step) {
                        0 -> {
                            if (photos.isEmpty()) {
                                onShowToast("Agrega al menos una foto")
                            } else {
                                step = 1
                            }
                        }
                        1 -> {
                            if (name.isBlank() || location.isBlank()) {
                                onShowToast("Completa el nombre y la comuna")
                            } else {
                                step = 2
                            }
                        }
                        else -> {
                            isPublishing = true
                            scope.launch {
                                try {
                                    val imagePath = photos.firstOrNull()?.let { uri ->
                                        runCatching {
                                            PetImageStore.copyToInternalStorage(context, uri)
                                        }.getOrNull()
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
                                        onPublished()
                                    }
                                } finally {
                                    isPublishing = false
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------------------
// Fondo "liquid glass" (solo visual, sin blur real)
// -------------------------------------------------------------------------
@Composable
private fun GlassBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-70).dp, y = 120.dp)
                .background(CoralPrimary.copy(alpha = 0.16f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 40.dp)
                .background(Color(0xFFF3B8A4).copy(alpha = 0.20f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp)
                .background(Color(0xFFBFD3F0).copy(alpha = 0.22f), CircleShape)
        )
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.66f),
                        Color.White.copy(alpha = 0.28f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.7f), shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E1B1A)
            )
        }
        content()
    }
}

// -------------------------------------------------------------------------
// Header y footer del wizard
// -------------------------------------------------------------------------
@Composable
private fun WizardHeader(step: Int, onClose: () -> Unit) {
    val titles = listOf("Agrega las fotos", "Descripción y opciones", "Confirma y publica")
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    tint = Color(0xFF1E1B1A)
                )
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dar en adopción",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1E1B1A)
                )
                Text(
                    text = titles[step],
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6F6864)
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${step + 1} / 3",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1E1B1A),
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                )
            }
        }
        LinearProgressIndicator(
            progress = { (step + 1) / 3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .padding(horizontal = 20.dp),
            color = CoralPrimary,
            trackColor = Color.White.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun WizardFooter(
    step: Int,
    canContinue: Boolean,
    isPublishing: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step > 0) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(52.dp)
            ) {
                Text("Atrás")
            }
        } else {
            Spacer(Modifier.height(52.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinue,
            enabled = canContinue && !isPublishing,
            modifier = Modifier
                .weight(if (step > 0) 1.4f else 2f)
                .height(54.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E1B1A),
                contentColor = Color.White
            )
        ) {
            if (isPublishing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = if (step == 2) "Publicar" else "Continuar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Paso 1: Fotos
// -------------------------------------------------------------------------
@Composable
private fun PhotoStep(
    photos: List<Uri>,
    onAdd: () -> Unit,
    onRemove: (Uri) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Sube fotos de tu mascota",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1E1B1A),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Las fotos reales ayudan a encontrar un hogar más rápido.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F6864),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))

        if (photos.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clickable(onClick = onAdd)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CoralPrimary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "Agregar fotos",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1E1B1A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "JPG, PNG o WEBP · hasta 6 fotos",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6F6864),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                PhotoTipsCard()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(photos, key = { _, uri -> uri.toString() }) { _, uri ->
                    PhotoThumb(uri = uri, onRemove = { onRemove(uri) })
                }
                if (photos.size < 6) {
                    item {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.5f))
                                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
                                .clickable(onClick = onAdd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Agregar otra foto",
                                tint = Color(0xFF1E1B1A),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoTipsCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = "Para una foto que enamore",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF1E1B1A),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoTip("☀️", "Buena luz", Modifier.weight(1f))
            PhotoTip("🐾", "Rostro visible", Modifier.weight(1f))
            PhotoTip("📷", "Sin filtros", Modifier.weight(1f))
        }
    }
}

@Composable
private fun PhotoTip(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6F6864),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhotoThumb(uri: Uri, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Foto de la mascota",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Surface(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(26.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Quitar foto",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Paso 2: Descripción y opciones
// -------------------------------------------------------------------------
@Composable
private fun DescriptionStep(
    name: String,
    onNameChange: (String) -> Unit,
    species: AnimalSpecies,
    onSpeciesChange: (AnimalSpecies) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    specialNeeds: String,
    onSpecialNeedsChange: (String) -> Unit,
    size: String,
    onSizeChange: (String) -> Unit,
    energyLevel: String,
    onEnergyLevelChange: (String) -> Unit,
    vaccinated: Boolean,
    onVaccinatedChange: (Boolean) -> Unit,
    neutered: Boolean,
    onNeuteredChange: (Boolean) -> Unit,
    houseTrained: Boolean,
    onHouseTrainedChange: (Boolean) -> Unit,
    goodWithKids: Boolean,
    onGoodWithKidsChange: (Boolean) -> Unit,
    goodWithDogs: Boolean,
    onGoodWithDogsChange: (Boolean) -> Unit,
    goodWithCats: Boolean,
    onGoodWithCatsChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(title = "Ficha básica") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Especie",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1E1B1A)
                )
                SegmentedControl(
                    options = AnimalSpecies.entries.map { it.label },
                    selectedOption = species.label,
                    onOptionSelected = { selected ->
                        onSpeciesChange(
                            AnimalSpecies.entries.firstOrNull { it.label == selected }
                                ?: AnimalSpecies.DOG
                        )
                    }
                )
            }

            AdoptMeTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Nombre *",
                placeholder = "Ej: Firulais"
            )
            AdoptMeTextField(
                value = age,
                onValueChange = onAgeChange,
                label = "Edad",
                placeholder = "Ej: 2 años"
            )
            AdoptMeTextField(
                value = breed,
                onValueChange = onBreedChange,
                label = "Raza / Tipo",
                placeholder = "Ej: Mestizo"
            )
            AdoptMeTextField(
                value = location,
                onValueChange = onLocationChange,
                label = "Comuna / Ubicación *",
                placeholder = "Ej: Providencia, Santiago"
            )
        }

        GlassCard(title = "Sobre su carácter") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tamaño",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1E1B1A)
                )
                SegmentedControl(
                    options = listOf("Pequeño", "Mediano", "Grande"),
                    selectedOption = size,
                    onOptionSelected = onSizeChange
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nivel de energía",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1E1B1A)
                )
                SegmentedControl(
                    options = listOf("Baja", "Media", "Alta"),
                    selectedOption = energyLevel,
                    onOptionSelected = onEnergyLevelChange
                )
            }
            AdoptMeTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Descripción",
                placeholder = "Cuéntanos cómo es, qué le gusta y qué hogar necesita…",
                singleLine = false,
                height = 110
            )
        }

        GlassCard(title = "Salud y cuidados") {
            AdoptMeSwitchRow("¿Está vacunado/a?", vaccinated, onVaccinatedChange)
            AdoptMeSwitchRow("¿Está esterilizado/a?", neutered, onNeuteredChange)
            AdoptMeSwitchRow("¿Sabe hacer sus necesidades fuera?", houseTrained, onHouseTrainedChange)
            AdoptMeTextField(
                value = specialNeeds,
                onValueChange = onSpecialNeedsChange,
                label = "Necesidades especiales (opcional)",
                placeholder = "Ej: medicación, dieta, movilidad…"
            )
        }

        GlassCard(title = "Convivencia") {
            AdoptMeSwitchRow("¿Es bueno/a con niños?", goodWithKids, onGoodWithKidsChange)
            AdoptMeSwitchRow("¿Se lleva bien con perros?", goodWithDogs, onGoodWithDogsChange)
            AdoptMeSwitchRow("¿Se lleva bien con gatos?", goodWithCats, onGoodWithCatsChange)
        }
    }
}

// -------------------------------------------------------------------------
// Paso 3: Confirmación y casillas legales
// -------------------------------------------------------------------------
@Composable
private fun ConfirmStep(
    name: String,
    species: AnimalSpecies,
    location: String,
    photoCount: Int,
    veracity: Boolean,
    onVeracityChange: (Boolean) -> Unit,
    responsible: Boolean,
    onResponsibleChange: (Boolean) -> Unit,
    photoConsent: Boolean,
    onPhotoConsentChange: (Boolean) -> Unit,
    notSale: Boolean,
    onNotSaleChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard(title = "Resumen de la ficha") {
            ConfirmRow("Nombre", name.ifBlank { "—" })
            ConfirmRow("Especie", species.label)
            ConfirmRow("Ubicación", location.ifBlank { "—" })
            ConfirmRow("Fotos", "$photoCount")
        }

        GlassCard(title = "Compromisos legales") {
            ConfirmCheck(
                checked = veracity,
                onCheckedChange = onVeracityChange,
                text = "Declaro que la información de la ficha es veraz."
            )
            ConfirmCheck(
                checked = responsible,
                onCheckedChange = onResponsibleChange,
                text = "Me comprometo a coordinar la adopción de forma responsable, según la Ley 21.020."
            )
            ConfirmCheck(
                checked = photoConsent,
                onCheckedChange = onPhotoConsentChange,
                text = "Tengo autorización para publicar las fotos de la mascota."
            )
            ConfirmCheck(
                checked = notSale,
                onCheckedChange = onNotSaleChange,
                text = "Confirmo que no se trata de una venta de animales."
            )
        }

        Text(
            text = "Al publicar aceptas los Términos y Condiciones y la Política de Privacidad.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6F6864),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F6864)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E1B1A),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ConfirmCheck(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = CoralPrimary,
                uncheckedColor = Color(0xFF6F6864)
            )
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF1E1B1A),
            modifier = Modifier
                .weight(1f)
                .padding(top = 10.dp)
        )
    }
}

// -------------------------------------------------------------------------
// Control segmentado
// -------------------------------------------------------------------------
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
                color = Color.White.copy(alpha = 0.5f),
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
                    CoralPrimary
                } else {
                    Color.Transparent
                },
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) {
                            Color.White
                        } else {
                            Color(0xFF6F6864)
                        }
                    )
                }
            }
        }
    }
}
