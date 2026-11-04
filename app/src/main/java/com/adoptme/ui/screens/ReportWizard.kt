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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.adoptme.R
import com.adoptme.core.media.ReportImageStore
import com.adoptme.data.model.ReportType
import com.adoptme.ui.components.AdoptMeTextField
import com.adoptme.ui.theme.CoralPrimary
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

/**
 * Wizard de reporte adaptado del de publicación:
 * 1) Foto · 2) Descripción · 3) Confirmación legal.
 */
@Composable
fun ReportWizard(
    userLocation: LatLng?,
    onClose: () -> Unit,
    onSubmit: (ReportType, String, String, String?) -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    var step by rememberSaveable { mutableStateOf(0) }
    var photo by remember { mutableStateOf<Uri?>(null) }
    var selectedType by rememberSaveable { mutableStateOf<ReportType?>(null) }
    var locationText by rememberSaveable { mutableStateOf("") }
    var descriptionText by rememberSaveable { mutableStateOf("") }
    var veracity by rememberSaveable { mutableStateOf(false) }
    var emergencyNotice by rememberSaveable { mutableStateOf(false) }
    var photoConsent by rememberSaveable { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photo = uri
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
        WizardDecor()

        Column(modifier = Modifier.fillMaxSize()) {
            ReportWizardHeader(step = step, onClose = onClose)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (step) {
                    0 -> ReportPhotoStep(
                        photo = photo,
                        onPick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onRemove = { photo = null }
                    )

                    1 -> ReportDetailsStep(
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it },
                        locationText = locationText,
                        onLocationChange = { locationText = it },
                        descriptionText = descriptionText,
                        onDescriptionChange = { descriptionText = it },
                        hasLocation = userLocation != null
                    )

                    else -> ReportConfirmStep(
                        selectedType = selectedType,
                        locationText = locationText,
                        userLocation = userLocation,
                        hasPhoto = photo != null,
                        veracity = veracity,
                        onVeracityChange = { veracity = it },
                        emergencyNotice = emergencyNotice,
                        onEmergencyNoticeChange = { emergencyNotice = it },
                        photoConsent = photoConsent,
                        onPhotoConsentChange = { photoConsent = it }
                    )
                }
            }

            ReportWizardFooter(
                step = step,
                canContinue = when (step) {
                    0 -> photo != null
                    1 -> selectedType != null &&
                        (locationText.isNotBlank() || userLocation != null)
                    else -> veracity && emergencyNotice && photoConsent
                },
                isPublishing = isPublishing,
                onBack = { step = (step - 1).coerceAtLeast(0) },
                onContinue = {
                    when (step) {
                        0 -> {
                            if (photo == null) onShowToast("Agrega una foto del reporte") else step = 1
                        }
                        1 -> {
                            if (selectedType == null) {
                                onShowToast("Selecciona el tipo de reporte")
                            } else if (locationText.isBlank() && userLocation == null) {
                                onShowToast("Escribe una referencia de ubicación")
                            } else {
                                step = 2
                            }
                        }
                        else -> {
                            val type = selectedType
                            if (type != null) {
                                isPublishing = true
                                scope.launch {
                                    try {
                                        val imagePath = photo?.let { uri ->
                                            runCatching {
                                                ReportImageStore.save(context, uri)
                                            }.getOrElse {
                                                onShowToast("No pudimos procesar la foto. Intenta con otra.")
                                                isPublishing = false
                                                return@launch
                                            }
                                        }
                                        onSubmit(type, locationText, descriptionText, imagePath)
                                    } finally {
                                        isPublishing = false
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun WizardDecor() {
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
private fun ReportWizardHeader(step: Int, onClose: () -> Unit) {
    val titles = listOf("Foto del reporte", "Cuéntanos qué pasa", "Confirma y envía")
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
                    text = "Reportar una situación",
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
private fun ReportWizardFooter(
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
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onContinue,
            enabled = canContinue && !isPublishing,
            modifier = Modifier
                .weight(1.4f)
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
                    text = if (step == 2) "Enviar reporte" else "Continuar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ReportPhotoStep(photo: Uri?, onPick: () -> Unit, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Agrega una foto",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1E1B1A)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Una imagen clara ayuda a ubicar y entender la situación.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F6864)
        )
        Spacer(Modifier.height(16.dp))

        if (photo == null) {
            GlassReportCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable(onClick = onPick)
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
                        text = "Agregar foto",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1E1B1A)
                    )
                    Text(
                        text = "JPG, PNG o WEBP · 1 imagen",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6F6864)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                AsyncImage(
                    model = photo,
                    contentDescription = "Foto del reporte",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
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
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onPick) {
                Text("Cambiar foto", color = CoralPrimary)
            }
        }
    }
}

@Composable
private fun ReportDetailsStep(
    selectedType: ReportType?,
    onTypeSelected: (ReportType) -> Unit,
    locationText: String,
    onLocationChange: (String) -> Unit,
    descriptionText: String,
    onDescriptionChange: (String) -> Unit,
    hasLocation: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassReportCard(title = "Tipo de reporte") {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedType?.label ?: "Selecciona una opción",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = Color(0xFFE5DFDB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ReportType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = {
                                onTypeSelected(type)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        GlassReportCard(title = "Ubicación y descripción") {
            AdoptMeTextField(
                value = locationText,
                onValueChange = onLocationChange,
                label = "Referencia de ubicación",
                placeholder = "Ej: Av. Providencia 1234"
            )
            Text(
                text = if (hasLocation) {
                    "📍 Se guardará con tu ubicación actual en el mapa."
                } else {
                    "Sin GPS: escribe una referencia; no aparecerá en el mapa."
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6F6864)
            )
            AdoptMeTextField(
                value = descriptionText,
                onValueChange = onDescriptionChange,
                label = "Descripción",
                placeholder = "Describe la situación y el estado del animal…",
                singleLine = false,
                height = 110
            )
        }

        GlassReportCard {
            Text(
                text = "AdoptMe no es un servicio de emergencia ni veterinario. Si hay peligro inmediato, contacta a los servicios locales.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB4433A)
            )
        }
    }
}

@Composable
private fun ReportConfirmStep(
    selectedType: ReportType?,
    locationText: String,
    userLocation: LatLng?,
    hasPhoto: Boolean,
    veracity: Boolean,
    onVeracityChange: (Boolean) -> Unit,
    emergencyNotice: Boolean,
    onEmergencyNoticeChange: (Boolean) -> Unit,
    photoConsent: Boolean,
    onPhotoConsentChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassReportCard(title = "Resumen del reporte") {
            ReportSummaryRow("Tipo", selectedType?.label ?: "—")
            ReportSummaryRow(
                "Ubicación",
                locationText.ifBlank {
                    if (userLocation != null) "Ubicación actual" else "—"
                }
            )
            ReportSummaryRow("Foto", if (hasPhoto) "1 imagen" else "Sin imagen")
        }

        GlassReportCard(title = "Confirmación") {
            ReportCheck(
                checked = veracity,
                onCheckedChange = onVeracityChange,
                text = "Declaro que la información del reporte es veraz."
            )
            ReportCheck(
                checked = emergencyNotice,
                onCheckedChange = onEmergencyNoticeChange,
                text = "Entiendo que AdoptMe no es un servicio de emergencia ni veterinario."
            )
            ReportCheck(
                checked = photoConsent,
                onCheckedChange = onPhotoConsentChange,
                text = "Tengo autorización para publicar la foto del reporte."
            )
        }

        Text(
            text = "La foto se guarda comprimida en el dispositivo. En el backend se almacenará solo su clave, nunca el binario en la base de datos.",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6F6864),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReportSummaryRow(label: String, value: String) {
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
private fun ReportCheck(
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

@Composable
private fun GlassReportCard(
    modifier: Modifier = Modifier,
    title: String? = null,
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
            .padding(18.dp),
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
