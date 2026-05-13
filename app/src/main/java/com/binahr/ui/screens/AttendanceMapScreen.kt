package com.binahr.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.BinaTopBar
import com.binahr.ui.components.HRButton
import com.binahr.ui.theme.*
import com.binahr.util.LocationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6_371_000.0
    val phi1 = Math.toRadians(lat1); val phi2 = Math.toRadians(lat2)
    val dphi = Math.toRadians(lat2 - lat1); val dlambda = Math.toRadians(lon2 - lon1)
    val a = sin(dphi / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dlambda / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

@Composable
fun AttendanceMapScreen(
    attendanceType: String,
    onBack: () -> Unit,
    onProceed: (lat: Double, lon: Double, address: String) -> Unit,
    vm: AttendanceViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isCheckIn = attendanceType != "CHECK_OUT"

    val geofenceEnabled by vm.geofenceEnabled.collectAsStateWithLifecycle()
    val officeLat by vm.officeLat.collectAsStateWithLifecycle()
    val officeLng by vm.officeLng.collectAsStateWithLifecycle()
    val officeRadius by vm.officeRadius.collectAsStateWithLifecycle()

    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLon by remember { mutableDoubleStateOf(0.0) }
    var address by remember { mutableStateOf("Mendeteksi lokasi...") }
    var locationLoading by remember { mutableStateOf(true) }

    val now = remember { Date() }
    val timeStr = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val dateStr = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(now)
    }

    // Compute distance & inside-radius flag
    val distanceMeters = remember(userLat, userLon, officeLat, officeLng, geofenceEnabled) {
        if (!geofenceEnabled || userLat == 0.0 || userLon == 0.0) 0.0
        else haversineMeters(userLat, userLon, officeLat, officeLng)
    }
    val isInsideRadius = remember(distanceMeters, geofenceEnabled, officeRadius, locationLoading) {
        if (locationLoading) false
        else if (!geofenceEnabled) true   // No geo config → always allow
        else distanceMeters <= officeRadius
    }

    // Map camera state
    val defaultLatLng = LatLng(-6.2, 106.816666)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 15f)
    }

    // GPS permission
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                val result = LocationHelper.getCurrentLocation(context)
                if (result is LocationHelper.LocationResult.Success) {
                    userLat = result.latitude; userLon = result.longitude; address = result.address
                    val latLng = LatLng(result.latitude, result.longitude)
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
                locationLoading = false
            }
        } else { locationLoading = false }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val result = LocationHelper.getCurrentLocation(context)
            if (result is LocationHelper.LocationResult.Success) {
                userLat = result.latitude; userLon = result.longitude; address = result.address
                val latLng = LatLng(result.latitude, result.longitude)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
            }
            locationLoading = false
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Re-center on office when geo loaded
    LaunchedEffect(geofenceEnabled, officeLat, officeLng) {
        if (geofenceEnabled && officeLat != 0.0 && officeLng != 0.0 && userLat == 0.0) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(officeLat, officeLng), 17f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Full-screen Google Map ──────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = LocationHelper.hasLocationPermission(context)),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
        ) {
            // Geofence circle on map
            if (geofenceEnabled && officeLat != 0.0 && officeLng != 0.0) {
                Circle(
                    center = LatLng(officeLat, officeLng),
                    radius = officeRadius.toDouble(),
                    strokeColor = if (isInsideRadius) TealAccent else AccentRed,
                    strokeWidth = 3f,
                    fillColor = if (isInsideRadius)
                        TealAccent.copy(alpha = 0.12f)
                    else
                        AccentRed.copy(alpha = 0.08f),
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────
            BinaTopBar(
                title = if (isCheckIn) "Absen Masuk" else "Absen Pulang",
                onBack = onBack,
            )

            // ── Shift info card overlay ────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCheckIn) OrangePrimary else AccentRed,
                ),
                elevation = CardDefaults.cardElevation(6.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "SHIFT 1",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                        Text(
                            text = "08:00 - 17:00",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // ── Detected location badge (visible when inside radius) ───
            if (!locationLoading && geofenceEnabled && isInsideRadius) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = OrangeSurface),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.LocationOn, null, tint = TealAccent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Lokasi Terdeteksi: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Text(
                            address.take(40),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Distance hint when outside radius ──────────────────────
            if (!locationLoading && geofenceEnabled && !isInsideRadius && distanceMeters > 0) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentRedLight),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.LocationOn, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "%.0f meter dari lokasi kantor — mendekati area yang ditentukan".format(distanceMeters),
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentRed,
                        )
                    }
                }
            }

            // ── Bottom action area ─────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = if (isInsideRadius) TealAccent else TextTertiary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = when {
                                locationLoading -> "Mendeteksi lokasi GPS..."
                                userLat == 0.0 -> "Lokasi belum tersedia"
                                else -> address.take(50)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    HRButton(
                        text = if (isCheckIn) "ABSEN MASUK" else "ABSEN PULANG",
                        onClick = { onProceed(userLat, userLon, address) },
                        enabled = isInsideRadius && !locationLoading,
                        containerColor = if (isCheckIn) OrangePrimary else AccentRed,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
