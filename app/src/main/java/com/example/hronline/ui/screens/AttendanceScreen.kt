package com.example.hronline.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hronline.data.*
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*
import com.example.hronline.util.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private enum class AttendanceState { FORM, LOADING, SUCCESS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    attendanceType: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val type = if (attendanceType == "CHECK_OUT") AttendanceType.CHECK_OUT else AttendanceType.CHECK_IN

    var state by remember { mutableStateOf(AttendanceState.FORM) }
    var location by remember { mutableStateOf("Mendeteksi lokasi...") }
    var locationLoading by remember { mutableStateOf(true) }
    var faceProgress by remember { mutableFloatStateOf(0f) }
    var showConfirm by remember { mutableStateOf(false) }
    var savedRecord by remember { mutableStateOf<AttendanceRecord?>(null) }

    val now = remember { Date() }
    val timeStr = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val dateStr = remember { SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(now) }
    val hour = remember { SimpleDateFormat("HH", Locale.getDefault()).format(now).toInt() }
    val isLate = if (type == AttendanceType.CHECK_IN) hour >= 8 else hour < 17

    // Permission launcher
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val result = LocationHelper.getCurrentLocation(context)
                location = when (result) {
                    is LocationHelper.LocationResult.Success -> result.address
                    is LocationHelper.LocationResult.Error -> result.message
                    LocationHelper.LocationResult.PermissionDenied -> "Izin lokasi ditolak"
                }
                locationLoading = false
            }
        } else {
            location = "Izin lokasi ditolak"
            locationLoading = false
        }
    }

    // Fetch location on launch
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val result = LocationHelper.getCurrentLocation(context)
            location = when (result) {
                is LocationHelper.LocationResult.Success -> result.address
                is LocationHelper.LocationResult.Error -> result.message
                LocationHelper.LocationResult.PermissionDenied -> "Izin lokasi ditolak"
            }
            locationLoading = false
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Face detection simulation
    LaunchedEffect(Unit) {
        while (faceProgress < 1f) {
            delay(50)
            faceProgress = (faceProgress + 0.02f).coerceAtMost(1f)
        }
    }

    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(
            title = if (type == AttendanceType.CHECK_IN) "Check In" else "Check Out",
            onBack = onBack,
        )

        when (state) {
            AttendanceState.FORM -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Shift Info Card
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text("Jam Kerja", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                                    Text("08:00 - 17:00 WIB", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlusJakartaSans,
                                        color = if (isLate) AccentRed else GreenPrimary,
                                    )
                                    StatusBadge(
                                        text = if (isLate) "Terlambat" else "Tepat Waktu",
                                        type = if (isLate) BadgeType.ERROR else BadgeType.SUCCESS,
                                    )
                                }
                            }
                        }
                    }

                    // Location Card
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lokasi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Map placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceGreenTint),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (locationLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GreenPrimary)
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.Map, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Face Detection Card
                    HRCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Face, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifikasi Wajah", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Animated face circle
                            Box(contentAlignment = Alignment.Center) {
                                val infiniteTransition = rememberInfiniteTransition(label = "face_scan")
                                val borderAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f, targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                                    label = "border_pulse",
                                )
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .border(
                                            3.dp,
                                            Brush.sweepGradient(
                                                listOf(
                                                    GreenPrimary.copy(alpha = borderAlpha),
                                                    TealAccent.copy(alpha = borderAlpha),
                                                    GreenPrimary.copy(alpha = borderAlpha),
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Face,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = if (faceProgress >= 1f) GreenPrimary else TextTertiary,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { faceProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GreenPrimary,
                                trackColor = Green50,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (faceProgress >= 1f) "Wajah Terverifikasi ✓" else "Memverifikasi wajah...",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (faceProgress >= 1f) GreenPrimary else TextTertiary,
                            )
                        }
                    }

                    // Submit Button
                    HRButton(
                        text = if (type == AttendanceType.CHECK_IN) "Check In Sekarang" else "Check Out Sekarang",
                        onClick = { showConfirm = true },
                        enabled = faceProgress >= 1f && !locationLoading,
                        containerColor = if (type == AttendanceType.CHECK_IN) GreenPrimary else AccentRed,
                    )
                }
            }

            AttendanceState.LOADING -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GreenPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Menyimpan data absensi...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AttendanceState.SUCCESS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Success icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Green50, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (type == AttendanceType.CHECK_IN) "Check In Berhasil!" else "Check Out Berhasil!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Detail Card
                    savedRecord?.let { record ->
                        HRCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Detail Absensi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailRow("Waktu", record.time)
                                DetailRow("Tanggal", record.date)
                                DetailRow("Lokasi", record.location)
                                DetailRow("Verifikasi Wajah", if (record.faceVerified) "Terverifikasi ✓" else "Tidak Terverifikasi")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HRCard {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Status", style = MaterialTheme.typography.bodyMedium)
                                StatusBadge(
                                    text = if (isLate) "Terlambat" else "Tepat Waktu",
                                    type = if (isLate) BadgeType.ERROR else BadgeType.SUCCESS,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HRButton(
                        text = "Kembali ke Dashboard",
                        onClick = onBack,
                    )
                }
            }
        }
    }

    // Confirmation Bottom Sheet
    if (showConfirm) {
        ModalBottomSheet(
            onDismissRequest = { showConfirm = false },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Konfirmasi ${if (type == AttendanceType.CHECK_IN) "Check In" else "Check Out"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow("Waktu", "$timeStr WIB")
                DetailRow("Tanggal", dateStr)
                DetailRow("Lokasi", location)
                DetailRow("Wajah", "Terverifikasi ✓")
                Spacer(modifier = Modifier.height(20.dp))
                HRButton(
                    text = "Konfirmasi",
                    onClick = {
                        showConfirm = false
                        state = AttendanceState.LOADING
                        scope.launch {
                            delay(1000)
                            val record = AttendanceRecord(
                                id = System.currentTimeMillis().toString(),
                                type = type,
                                date = dateStr,
                                time = "$timeStr WIB",
                                location = location,
                                timestamp = System.currentTimeMillis(),
                                faceVerified = true,
                            )
                            AttendanceStorage.saveAttendance(context, record)
                            savedRecord = record
                            state = AttendanceState.SUCCESS
                        }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
