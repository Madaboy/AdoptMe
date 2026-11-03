@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.adoptme.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adoptme.core.location.LocationProvider
import com.adoptme.data.model.Report
import com.adoptme.data.model.ReportType
import com.adoptme.ui.components.*
import com.adoptme.viewmodel.ReportsViewModel
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

private val radiusOptions = listOf(1f, 5f, 10f, 25f)

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onShowToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val reports by viewModel.reports.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var hasPermission by remember { mutableStateOf(LocationProvider.hasPermission(context)) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    var selectedType by rememberSaveable { mutableStateOf<ReportType?>(null) }
    var locationText by rememberSaveable { mutableStateOf("") }
    var descriptionText by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var radiusKm by rememberSaveable { mutableStateOf(10f) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!hasPermission) {
            locationError = "Sin permiso de ubicación. Puedes activarlo desde Ajustes."
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = LocationProvider.hasPermission(context)
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) return@LaunchedEffect
        isLocating = true
        locationError = null
        val location = LocationProvider.currentLocation(context)
        if (location != null) {
            userLocation = LatLng(location.latitude, location.longitude)
        } else {
            locationError = "No pudimos obtener tu ubicación. Revisa que el GPS esté activo."
        }
        isLocating = false
    }

    LaunchedEffect(toastMessage) {
        val message = toastMessage
        if (message != null) {
            onShowToast(message)
            viewModel.clearToast()
        }
    }

    val reportsWithDistance = remember(reports, userLocation) {
        reports.map { report ->
            val distance = if (
                userLocation != null &&
                report.latitude != null &&
                report.longitude != null
            ) {
                LocationProvider.distanceKm(
                    fromLatitude = userLocation!!.latitude,
                    fromLongitude = userLocation!!.longitude,
                    toLatitude = report.latitude,
                    toLongitude = report.longitude
                )
            } else {
                null
            }
            NearbyReport(report = report, distanceKm = distance)
        }
    }

    val nearbyReports = remember(reportsWithDistance, radiusKm, userLocation) {
        if (userLocation == null) {
            reportsWithDistance.sortedByDescending { it.report.createdAt }
        } else {
            reportsWithDistance
                .filter { it.distanceKm != null && it.distanceKm <= radiusKm }
                .sortedBy { it.distanceKm }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = "Reportes",
            subtitle = when {
                userLocation != null -> "${nearbyReports.size} alertas en ${radiusKm.toInt()} km"
                hasPermission -> "Obteniendo tu ubicación…"
                else -> "Activa tu ubicación para ver alertas cercanas"
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(270.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            ReportsMap(
                reports = nearbyReports.map { it.report },
                userLocation = userLocation,
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map = it }
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗺️", fontSize = 12.sp)
                    Text(
                        text = when {
                            isLocating -> "Ubicando…"
                            userLocation != null -> "Tu ubicación"
                            else -> "Vista de Santiago"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (userLocation != null) {
                SmallFloatingActionButton(
                    onClick = {
                        val user = userLocation ?: return@SmallFloatingActionButton
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(user, 14.0),
                            600
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Centrar en mi ubicación"
                    )
                }
            }

            if (isLocating) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Buscando tu ubicación",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (!hasPermission) {
                PermissionCard(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(18.dp),
                    onRequest = {
                        permissionRequested = true
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onSettings = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
            }

            locationError?.let { error ->
                if (hasPermission) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (userLocation != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("Radio de alertas")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            radiusOptions.forEach { radius ->
                                RadiusChip(
                                    label = "${radius.toInt()} km",
                                    selected = radiusKm == radius,
                                    onClick = { radiusKm = radius }
                                )
                            }
                        }
                    }
                }
            }

            item {
                AdoptMeCardSection("Reportar una situación") {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType?.let { "${it.emoji} ${it.label}" } ?: "Tipo de reporte",
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
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            ReportType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text("${type.emoji} ${type.label}") },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    AdoptMeTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        label = "Referencia de ubicación",
                        placeholder = "Ej: Av. Providencia 1234"
                    )

                    Text(
                        text = if (userLocation != null) {
                            "📍 Se guardará con tu ubicación actual en el mapa."
                        } else {
                            "Sin GPS: escribe una referencia; el reporte no aparecerá en el mapa."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AdoptMeTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = "Descripción",
                        placeholder = "Describe la situación y el estado del animal…",
                        singleLine = false,
                        height = 90
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = "AdoptMe no es un servicio de emergencia ni veterinario. Si hay peligro inmediato, contacta a los servicios locales.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val type = selectedType
                            val coords = userLocation
                            val finalLocation = locationText.trim().ifBlank {
                                coords?.let {
                                    "Ubicación actual (${"%.4f".format(it.latitude)}, ${"%.4f".format(it.longitude)})"
                                }.orEmpty()
                            }
                            if (type != null && finalLocation.isNotBlank()) {
                                viewModel.addReport(
                                    type = type,
                                    location = finalLocation,
                                    description = descriptionText,
                                    latitude = coords?.latitude,
                                    longitude = coords?.longitude
                                )
                                selectedType = null
                                locationText = ""
                                descriptionText = ""
                            } else {
                                onShowToast("Completa el tipo y la ubicación del reporte")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            text = "Enviar reporte",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                SectionTitle("Alertas cercanas")
            }

            if (nearbyReports.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🐾", fontSize = 28.sp)
                            Text(
                                text = if (userLocation != null) {
                                    "No hay alertas en este radio"
                                } else {
                                    "Aún no hay alertas ubicadas"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (userLocation != null) {
                                    "Amplía el radio o crea el primer reporte."
                                } else {
                                    "Activa tu ubicación y crea un reporte para verlo en el mapa."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(nearbyReports, key = { it.report.id }) { nearby ->
                ReportCard(
                    report = nearby.report,
                    distanceKm = nearby.distanceKm
                )
            }
        }
    }
}

private data class NearbyReport(
    val report: Report,
    val distanceKm: Double?
)

@Composable
private fun PermissionCard(
    modifier: Modifier = Modifier,
    onRequest: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📍", fontSize = 26.sp)
            Text(
                text = "Activa tu ubicación",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Necesitamos tu ubicación para centrar el mapa y mostrarte alertas cercanas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Permitir")
                }
                TextButton(onClick = onSettings) {
                    Text("Ajustes")
                }
            }
        }
    }
}

@Composable
private fun RadiusChip(
    label: String,
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
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}

@Composable
fun ReportCard(report: Report, distanceKm: Double? = null) {
    val (iconBg, iconFg) = when (report.type) {
        ReportType.DANGER -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        ReportType.LOST -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        ReportType.AT_RISK -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ReportType.STREET -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(report.type.emoji, fontSize = 21.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = report.type.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = iconFg
                    )
                    Text(
                        text = reportTimeAgo(report),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = buildString {
                            append("📍 ${report.location}")
                            distanceKm?.let { append(" · ${"%.1f".format(it)} km") }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

private fun reportTimeAgo(report: Report): String {
    if (report.createdAt <= 0) return report.timeAgo
    return DateUtils.getRelativeTimeSpanString(
        report.createdAt,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
