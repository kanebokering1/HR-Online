package com.binahr.ui.screens


import com.binahr.BuildConfig
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.data.*
import com.binahr.data.api.model.AttendanceLogDto
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.AttendanceViewModel
import com.binahr.util.CameraHelper
import com.binahr.util.LocationHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private enum class AttendanceState { FORM, LOADING, SUCCESS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    attendanceType: String,
    onBack: () -> Unit,
    vm: AttendanceViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val type = if (attendanceType == "CHECK_OUT") AttendanceType.CHECK_OUT else AttendanceType.CHECK_IN

    var state by remember { mutableStateOf(AttendanceState.FORM) }
    var location by remember { mutableStateOf("Mendeteksi lokasi...") }
    var locationLoading by remember { mutableStateOf(true) }
    var savedRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var capturedLat by remember { mutableStateOf(0.0) }
    var capturedLon by remember { mutableStateOf(0.0) }
    var apiError by remember { mutableStateOf<String?>(null) }

    // Camera state
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedBase64 by remember { mutableStateOf<String?>(null) }
    // Both check-in and check-out require a selfie photo.
    val selfieRequired = true
    val selfieReady = !selfieRequired || capturedFile != null

    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val checkInResult by vm.checkInResult.collectAsStateWithLifecycle()
    val checkOutResult by vm.checkOutResult.collectAsStateWithLifecycle()

    val now = remember { Date() }
    val timeStr = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val dateStr = remember { SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(now) }
    val hour = remember { SimpleDateFormat("HH", Locale.getDefault()).format(now).toInt() }
    val isLate = if (type == AttendanceType.CHECK_IN) hour >= 8 else hour < 17

    // GPS permission launcher
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                val result = LocationHelper.getCurrentLocation(context)
                when (result) {
                    is LocationHelper.LocationResult.Success -> {
                        capturedLat = result.latitude; capturedLon = result.longitude; location = result.address
                    }
                    is LocationHelper.LocationResult.Error -> location = result.message
                    LocationHelper.LocationResult.PermissionDenied -> location = "Izin lokasi ditolak"
                }
                locationLoading = false
            }
        } else {
            location = "Izin lokasi ditolak"; locationLoading = false
        }
    }

    // Camera permission state — drives whether SelfieCameraCard is shown
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera permission launcher
    val cameraPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    // Storage permission launcher (only needed for API 23-28 for gallery save)
    val storagePermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    // Fetch GPS on launch
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val result = LocationHelper.getCurrentLocation(context)
            when (result) {
                is LocationHelper.LocationResult.Success -> {
                    capturedLat = result.latitude; capturedLon = result.longitude; location = result.address
                }
                is LocationHelper.LocationResult.Error -> location = result.message
                LocationHelper.LocationResult.PermissionDenied -> location = "Izin lokasi ditolak"
            }
            locationLoading = false
        } else {
            permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Request camera permission upfront if not yet granted.
        if (selfieRequired && !hasCameraPermission) {
            cameraPermLauncher.launch(Manifest.permission.CAMERA)
        }
        // Request storage permission for gallery save on Android < 10.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // Observe ViewModel results
    LaunchedEffect(checkInResult, checkOutResult) {
        val result: Result<AttendanceLogDto>? = checkInResult ?: checkOutResult
        result?.let {
            it.onSuccess { log: AttendanceLogDto ->
                val record = AttendanceRecord(
                    id = log.id,
                    type = type,
                    date = dateStr,
                    time = "$timeStr WIB",
                    location = log.checkInAddress ?: location,
                    timestamp = System.currentTimeMillis(),
                    faceVerified = capturedFile != null,
                )
                AttendanceStorage.saveAttendance(context, record)
                savedRecord = record
                state = AttendanceState.SUCCESS
            }.onFailure { e: Throwable ->
                apiError = e.message
                state = AttendanceState.FORM
            }
            vm.clearResults()
        }
    }

    var showConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(
            title = if (type == AttendanceType.CHECK_IN) "Check In" else "Check Out",
            onBack = onBack,
        )

        when (state) {
            AttendanceState.FORM -> {
                // API error banner
                apiError?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(err, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { apiError = null }) { Icon(Icons.Filled.Close, contentDescription = null) }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Shift + time info
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
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PlusJakartaSans,
                                        color = if (isLate) AccentRed else OrangePrimary,
                                    )
                                    StatusBadge(
                                        text = if (isLate) "Terlambat" else "Tepat Waktu",
                                        type = if (isLate) BadgeType.ERROR else BadgeType.SUCCESS,
                                    )
                                }
                            }
                        }
                    }

                    // Location card
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lokasi GPS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (locationLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = OrangePrimary, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Mendeteksi lokasi...", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Filled.Place, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(location, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                if (capturedLat != 0.0) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "%.6f, %.6f".format(capturedLat, capturedLon),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                }
                            }
                        }
                    }

                    // Selfie camera (only for check-in)
                    if (selfieRequired) {
                        if (hasCameraPermission) {
                            SelfieCameraCard(
                                captured = capturedFile,
                                capturedBitmap = capturedBitmap,
                                onCaptured = { file, base64 ->
                                    capturedFile = file
                                    capturedBase64 = base64
                                    capturedBitmap = CameraHelper.loadSelfie(file)
                                },
                                onRetake = {
                                    capturedFile = null
                                    capturedBase64 = null
                                    capturedBitmap = null
                                },
                            )
                        } else {
                            // Camera permission was denied — show a prompt to re-request
                            HRCard {
                                Column(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        Icons.Filled.CameraAlt,
                                        contentDescription = null,
                                        tint = OrangePrimary,
                                        modifier = Modifier.size(40.dp),
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Izin Kamera Diperlukan",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Foto selfie wajib untuk check-in. Izinkan kamera agar absensi dapat dilanjutkan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { cameraPermLauncher.launch(Manifest.permission.CAMERA) },
                                    ) {
                                        Text("Izinkan Kamera")
                                    }
                                }
                            }
                        }
                    }

                    // Submit button
                    HRButton(
                        text = if (type == AttendanceType.CHECK_IN) "Check In Sekarang" else "Check Out Sekarang",
                        onClick = { showConfirm = true },
                        enabled = selfieReady && !locationLoading && !isLoading,
                        containerColor = if (type == AttendanceType.CHECK_IN) OrangePrimary else AccentRed,
                    )
                }
            }

            AttendanceState.LOADING -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = OrangePrimary)
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
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(OrangeSurface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (type == AttendanceType.CHECK_IN) "Check In Berhasil!" else "Check Out Berhasil!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    savedRecord?.let { record ->
                        HRCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Detail Absensi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailRow("Waktu", record.time)
                                DetailRow("Tanggal", record.date)
                                DetailRow("Lokasi", record.location)
                                DetailRow("Foto Selfie", if (record.faceVerified) "Tersimpan ✓" else "Tidak ada")
                                DetailRow("GPS", if (capturedLat != 0.0) "Tercatat ✓" else "Tidak tersedia")
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
                    HRButton(text = "Kembali ke Dashboard", onClick = onBack)
                }
            }
        }
    }

    // Confirmation bottom sheet
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
                DetailRow("Lokasi", location.take(60) + if (location.length > 60) "..." else "")
                if (type == AttendanceType.CHECK_IN) {
                    DetailRow("Foto Selfie", if (capturedFile != null) "Siap ✓" else "Belum diambil")
                }
                DetailRow("GPS", if (capturedLat != 0.0) "%.5f, %.5f".format(capturedLat, capturedLon) else "Tidak tersedia")
                Spacer(modifier = Modifier.height(20.dp))
                HRButton(
                    text = "Konfirmasi",
                    onClick = {
                        showConfirm = false
                        state = AttendanceState.LOADING
                        scope.launch {
                            if (type == AttendanceType.CHECK_IN) {
                                vm.checkIn(capturedLat, capturedLon, location, capturedBase64)
                            } else {
                                vm.checkOut(capturedLat, capturedLon, location)
                            }
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

