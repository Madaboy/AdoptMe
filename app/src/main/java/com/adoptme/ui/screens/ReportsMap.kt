package com.adoptme.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.adoptme.data.model.Report
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"
private const val REPORTS_SOURCE = "adoptme-reports"
private const val REPORTS_LAYER = "adoptme-reports-layer"
private const val USER_SOURCE = "adoptme-user"
private const val USER_LAYER = "adoptme-user-layer"

@Composable
fun ReportsMap(
    reports: List<Report>,
    userLocation: LatLng?,
    modifier: Modifier = Modifier,
    onMapReady: (MapLibreMap) -> Unit = {},
    onReportClick: (Report) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var loadedStyle by remember { mutableStateOf<Style?>(null) }
    val currentReports by rememberUpdatedState(reports)
    val currentOnReportClick by rememberUpdatedState(onReportClick)

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { mapInstance ->
                map = mapInstance
                mapInstance.setStyle(Style.Builder().fromUri(MAP_STYLE)) { style ->
                    addMapLayers(style)
                    loadedStyle = style
                    updateMapSources(style, reports, userLocation)
                }
                onMapReady(mapInstance)

                mapInstance.addOnMapClickListener { latLng ->
                    val point = mapInstance.projection.toScreenLocation(latLng)
                    val features = mapInstance.queryRenderedFeatures(point, REPORTS_LAYER)
                    val reportId = features.firstOrNull()?.getStringProperty("reportId")
                    val report = currentReports.find { it.id.toString() == reportId }
                    if (report != null) {
                        currentOnReportClick(report)
                    }
                    false
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        mapView.onStart()
        mapView.onResume()

        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(loadedStyle, reports, userLocation) {
        loadedStyle?.let { updateMapSources(it, reports, userLocation) }
    }

    LaunchedEffect(userLocation, map) {
        val mapInstance = map ?: return@LaunchedEffect
        val target = userLocation ?: LatLng(-33.4489, -70.6693)
        val zoom = if (userLocation != null) 13.5 else 10.5
        mapInstance.animateCamera(
            CameraUpdateFactory.newLatLngZoom(target, zoom),
            700
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize()
    )
}

private fun addMapLayers(style: Style) {
    style.addSource(GeoJsonSource(USER_SOURCE, FeatureCollection.fromFeatures(emptyList<Feature>())))
    style.addLayer(
        CircleLayer(USER_LAYER, USER_SOURCE).withProperties(
            PropertyFactory.circleColor("#1E1B1A"),
            PropertyFactory.circleRadius(9f),
            PropertyFactory.circleStrokeColor("#FFFFFF"),
            PropertyFactory.circleStrokeWidth(3f)
        )
    )

    style.addSource(GeoJsonSource(REPORTS_SOURCE, FeatureCollection.fromFeatures(emptyList<Feature>())))
    style.addLayer(
        CircleLayer(REPORTS_LAYER, REPORTS_SOURCE).withProperties(
            PropertyFactory.circleColor("#D9534F"),
            PropertyFactory.circleRadius(7f),
            PropertyFactory.circleStrokeColor("#FFFFFF"),
            PropertyFactory.circleStrokeWidth(2f)
        )
    )
}

private fun updateMapSources(style: Style, reports: List<Report>, userLocation: LatLng?) {
    val reportFeatures = reports.mapNotNull { report ->
        val latitude = report.latitude ?: return@mapNotNull null
        val longitude = report.longitude ?: return@mapNotNull null
        Feature.fromGeometry(Point.fromLngLat(longitude, latitude)).apply {
            addStringProperty("type", report.type.name)
            addStringProperty("reportId", report.id.toString())
        }
    }
    style.getSourceAs<GeoJsonSource>(REPORTS_SOURCE)
        ?.setGeoJson(FeatureCollection.fromFeatures(reportFeatures))

    val userFeatures = userLocation?.let {
        listOf(Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude)))
    } ?: emptyList()
    style.getSourceAs<GeoJsonSource>(USER_SOURCE)
        ?.setGeoJson(FeatureCollection.fromFeatures(userFeatures))
}
