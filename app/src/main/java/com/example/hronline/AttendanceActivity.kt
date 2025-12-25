package com.example.hronline

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.hronline.data.AttendanceRecord
import com.example.hronline.data.AttendanceStorage
import com.example.hronline.data.AttendanceType
import com.example.hronline.ui.theme.SplashTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AttendanceScreenState {
    ATTENDANCE_FORM,
    REPORT
}

class AttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val attendanceType = intent.getStringExtra("attendance_type") ?: "CHECK_IN"
        val type = try {
            AttendanceType.valueOf(attendanceType)
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            AttendanceType.CHECK_IN
        }
        
        setContent {
            SplashTheme {
                AttendanceScreen(
                    attendanceType = type,
                    activity = this@AttendanceActivity,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    attendanceType: AttendanceType,
    activity: ComponentActivity,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val primaryColor = Color(0xFFFF6568)
    
    // Screen State Management - lebih simple dan robust
    var screenState by remember { mutableStateOf(AttendanceScreenState.ATTENDANCE_FORM) }
    
    // State untuk Attendance Form
    var locationName by remember { mutableStateOf("Mendeteksi lokasi...") }
    var isLocationLoading by remember { mutableStateOf(true) }
    var showFaceDetection by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    
    // State untuk Report
    var attendanceRecord by remember { mutableStateOf<AttendanceRecord?>(null) }
    var isLate by remember { mutableStateOf(false) }
    
    // Location Service
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Get location - hanya jika masih di form
    LaunchedEffect(screenState) {
        if (screenState == AttendanceScreenState.ATTENDANCE_FORM && isLocationLoading) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        updateLocationName(context, loc) { name ->
                            locationName = name
                            isLocationLoading = false
                        }
                    } else {
                        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                            .setWaitForAccurateLocation(false)
                            .setMinUpdateIntervalMillis(500)
                            .setMaxUpdateDelayMillis(1000)
                            .build()
                        
                        val callback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                fusedLocationClient.removeLocationUpdates(this)
                                updateLocationName(context, result.lastLocation) { name ->
                                    locationName = name
                                    isLocationLoading = false
                                }
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
                    }
                }
            } else {
                locationName = "Izin lokasi ditolak"
                isLocationLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (screenState == AttendanceScreenState.REPORT) "Laporan Absensi" else "Absensi",
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (screenState == AttendanceScreenState.REPORT) {
                            screenState = AttendanceScreenState.ATTENDANCE_FORM
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated Content Transition
            AnimatedContent(
                targetState = screenState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300)) using
                    SizeTransform(clip = false)
                },
                label = "screen_transition"
            ) { state ->
                when (state) {
                    AttendanceScreenState.ATTENDANCE_FORM -> {
                        AttendanceFormScreen(
                            modifier = Modifier.padding(innerPadding),
                            attendanceType = attendanceType,
                            locationName = locationName,
                            isLocationLoading = isLocationLoading,
                            showFaceDetection = showFaceDetection,
                            primaryColor = primaryColor,
                            onFaceDetectionStart = { showFaceDetection = true },
                            onConfirmClick = {
                                showConfirmDialog = true
                            },
                            onConfirmDialogConfirm = {
                                showConfirmDialog = false
                                scope.launch {
                                    processAttendance(
                                        context = context,
                                        attendanceType = attendanceType,
                                        locationName = locationName,
                                        onLoading = { isLoading ->
                                            showLoadingDialog = isLoading
                                        },
                                onSuccess = { record, late ->
                                    // Update state langsung karena sudah di coroutine scope
                                    attendanceRecord = record
                                    isLate = late
                                    showLoadingDialog = false
                                    screenState = AttendanceScreenState.REPORT
                                }
                                    )
                                }
                            },
                            onConfirmDialogCancel = { showConfirmDialog = false }
                        )
                    }
                    AttendanceScreenState.REPORT -> {
                        attendanceRecord?.let { record ->
                            AttendanceReportScreen(
                                modifier = Modifier.padding(innerPadding),
                                record = record,
                                isLate = isLate,
                                primaryColor = primaryColor,
                                onBackToHomeClick = {
                                    activity.finish()
                                }
                            )
                        }
                    }
                }
            }
            
            // Dialog Konfirmasi
            if (showConfirmDialog) {
                ConfirmDialog(
                    attendanceType = attendanceType,
                    locationName = locationName,
                    primaryColor = primaryColor,
                    onConfirm = {
                        showConfirmDialog = false
                        scope.launch {
                            processAttendance(
                                context = context,
                                attendanceType = attendanceType,
                                locationName = locationName,
                                onLoading = { isLoading ->
                                    showLoadingDialog = isLoading
                                },
                                onSuccess = { record, late ->
                                    // Update state langsung karena sudah di coroutine scope
                                    attendanceRecord = record
                                    isLate = late
                                    showLoadingDialog = false
                                    screenState = AttendanceScreenState.REPORT
                                }
                            )
                        }
                    },
                    onCancel = { showConfirmDialog = false }
                )
            }
            
            // Dialog Loading
            if (showLoadingDialog) {
                LoadingDialog(primaryColor)
            }
        }
    }
}

// Attendance Form Screen
@Composable
fun AttendanceFormScreen(
    modifier: Modifier = Modifier,
    attendanceType: AttendanceType,
    locationName: String,
    isLocationLoading: Boolean,
    showFaceDetection: Boolean,
    primaryColor: Color,
    onFaceDetectionStart: () -> Unit,
    onConfirmClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onConfirmDialogConfirm: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onConfirmDialogCancel: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Shift Card
        ShiftCard(primaryColor, attendanceType)
        
        // Maps Placeholder
        MapsPlaceholder(
            locationName = locationName,
            isLoading = isLocationLoading,
            primaryColor = primaryColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Face Detection Area
        AnimatedVisibility(visible = showFaceDetection) {
            FaceDetectionArea(primaryColor) {
                onConfirmClick()
            }
        }
        
        // Tombol Absen
        if (!showFaceDetection) {
            val currentTime = remember { mutableStateOf(Date()) }
            LaunchedEffect(Unit) {
                while (true) {
                    currentTime.value = Date()
                    delay(1000)
                }
            }
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTimeStr = timeFormat.format(currentTime.value)
            val currentHour = currentTimeStr.split(":")[0].toInt()
            val currentMinute = currentTimeStr.split(":")[1].toInt()
            
            val canCheckOut = if (attendanceType == AttendanceType.CHECK_OUT) {
                currentHour > 17 || (currentHour == 17 && currentMinute >= 0)
            } else {
                true
            }
            
            Button(
                onClick = onFaceDetectionStart,
                enabled = canCheckOut && !isLocationLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canCheckOut && !isLocationLoading) primaryColor else Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (attendanceType == AttendanceType.CHECK_IN) "ABSEN MASUK" else "ABSEN PULANG",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Attendance Report Screen
@Composable
fun AttendanceReportScreen(
    modifier: Modifier = Modifier,
    record: AttendanceRecord,
    isLate: Boolean,
    primaryColor: Color,
    onBackToHomeClick: () -> Unit
) {
    val successColor = Color(0xFF4CAF50)
    val lateColor = Color(0xFFFF9800)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Success Header Card
        SuccessHeaderCard(
            attendanceType = record.type,
            isLate = isLate,
            primaryColor = primaryColor,
            successColor = successColor,
            lateColor = lateColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Detail Information Card
        DetailInformationCard(
            record = record,
            primaryColor = primaryColor,
            successColor = successColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Card
        StatusCard(
            attendanceType = record.type,
            isLate = isLate,
            faceVerified = record.faceVerified,
            primaryColor = primaryColor,
            successColor = successColor,
            lateColor = lateColor
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Back to Home Button
        Button(
            onClick = onBackToHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Kembali ke Beranda",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Helper Functions
private fun updateLocationName(
    context: android.content.Context,
    location: Location?,
    onResult: (String) -> Unit
) {
    if (location == null) {
        onResult("Lokasi tidak terdeteksi")
        return
    }
    
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val street = address.getAddressLine(0) ?: ""
                    val city = address.locality ?: address.subAdminArea ?: ""
                    val locationText = when {
                        street.isNotEmpty() -> "$street, $city"
                        city.isNotEmpty() -> city
                        else -> "Lat: ${String.format(Locale.getDefault(), "%.4f", location.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", location.longitude)}"
                    }
                    onResult(locationText)
                } else {
                    onResult("Lat: ${String.format(Locale.getDefault(), "%.4f", location.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", location.longitude)}")
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val street = address.getAddressLine(0) ?: ""
                val city = address.locality ?: address.subAdminArea ?: ""
                val locationText = when {
                    street.isNotEmpty() -> "$street, $city"
                    city.isNotEmpty() -> city
                    else -> "Lat: ${String.format(Locale.getDefault(), "%.4f", location.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", location.longitude)}"
                }
                onResult(locationText)
            } else {
                onResult("Lat: ${String.format(Locale.getDefault(), "%.4f", location.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", location.longitude)}")
            }
        }
    } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
        onResult("Lat: ${String.format(Locale.getDefault(), "%.4f", location.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", location.longitude)}")
    }
}

private suspend fun processAttendance(
    context: android.content.Context,
    attendanceType: AttendanceType,
    locationName: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: (AttendanceRecord, Boolean) -> Unit
) {
    withContext(Dispatchers.Main) {
        onLoading(true)
    }
    
    delay(2000) // Simulasi loading
    
    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
    val currentTime = SimpleDateFormat("HH:mm WIB", Locale.getDefault()).format(Date())
    val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentHour = currentTimeStr.split(":")[0].toInt()
    val currentMinute = currentTimeStr.split(":")[1].toInt()
    
    val isLate = if (attendanceType == AttendanceType.CHECK_IN) {
        currentHour > 8 || (currentHour == 8 && currentMinute > 0)
    } else {
        false
    }
    
    val record = AttendanceRecord(
        id = System.currentTimeMillis().toString(),
        type = attendanceType,
        date = currentDate,
        time = currentTime,
        location = locationName,
        timestamp = System.currentTimeMillis(),
        faceVerified = true
    )
    
    // Simpan data absensi
    AttendanceStorage.saveAttendance(context, record)
    
    // Tutup loading dialog terlebih dahulu
    withContext(Dispatchers.Main) {
        onLoading(false)
    }
    
    // Delay untuk memastikan loading dialog benar-benar tertutup sebelum show report
    delay(300)
    
    // Pastikan callback dipanggil di Main thread
    withContext(Dispatchers.Main) {
        onSuccess(record, isLate)
    }
}

// Composable Components
@Composable
fun ShiftCard(primaryColor: Color, attendanceType: AttendanceType) {
    val currentTime = remember { mutableStateOf(Date()) }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = Date()
            delay(1000)
        }
    }
    
    val currentTimeStr = timeFormat.format(currentTime.value)
    val currentHour = currentTimeStr.split(":")[0].toInt()
    val currentMinute = currentTimeStr.split(":")[1].toInt()
    
    val isLate = if (attendanceType == AttendanceType.CHECK_IN) {
        currentHour > 8 || (currentHour == 8 && currentMinute > 0)
    } else {
        false
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SHIFT 1", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentTimeStr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLate) Color(0xFFD32F2F) else Color.Black
                    )
                    if (isLate && attendanceType == AttendanceType.CHECK_IN) {
                        Text("Terlambat", fontSize = 10.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateFormat.format(Date()), fontSize = 14.sp, color = Color.Black)
                Text("08:00 - 17:00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun MapsPlaceholder(locationName: String, isLoading: Boolean, primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0E0E0))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawLine(Color.White, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 25f)
            drawLine(Color.White, Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 25f)
            drawLine(Color.White, Offset(w * 0.4f, 0f), Offset(w * 0.4f, h), strokeWidth = 25f)
            drawLine(Color.White, Offset(w * 0.8f, 0f), Offset(w * 0.8f, h), strokeWidth = 25f)
        }
        Icon(
            Icons.Default.LocationOn, 
            "Pin", 
            tint = primaryColor, 
            modifier = Modifier.size(56.dp).align(Alignment.Center).offset(y = (-20).dp)
        )
        Surface(
            color = primaryColor.copy(alpha = 0.9f), 
            shape = RoundedCornerShape(8.dp), 
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Mendeteksi lokasi...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        0.2f, 
                        1f, 
                        infiniteRepeatable(tween(800), RepeatMode.Reverse)
                    )
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White.copy(alpha = alpha)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        locationName, 
                        color = Color.White, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FaceDetectionArea(primaryColor: Color, onFaceDetected: () -> Unit) {
    var isDetecting by remember { mutableStateOf(true) }
    var detectionProgress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (detectionProgress < 1f) {
            delay(50)
            detectionProgress += 0.02f
        }
        isDetecting = false
        delay(500)
        onFaceDetected()
    }
    
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val infiniteTransition = rememberInfiniteTransition()
            val scanOffset by infiniteTransition.animateFloat(
                -60f, 
                60f, 
                infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart)
            )
            Box(
                Modifier
                    .size(150.dp)
                    .border(3.dp, primaryColor, CircleShape)
                    .background(Color(0xFFF5F5F5), CircleShape)
                    .clip(CircleShape), 
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Face, "Face", tint = Color.Gray, modifier = Modifier.size(80.dp))
                if (isDetecting) {
                    Box(Modifier.width(120.dp).height(2.dp).offset(y = scanOffset.dp).background(primaryColor.copy(alpha = 0.8f)))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("MENDETEKSI WAJAH OTOMATIS", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (isDetecting) {
                Text(
                    "Pastikan wajah terlihat jelas...", 
                    textAlign = TextAlign.Center, 
                    color = Color.Gray, 
                    fontSize = 12.sp, 
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { detectionProgress }, 
                    modifier = Modifier.fillMaxWidth(0.6f).padding(top = 8.dp), 
                    color = primaryColor
                )
            } else {
                Text(
                    "Wajah terdeteksi!", 
                    textAlign = TextAlign.Center, 
                    color = Color(0xFF4CAF50), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    attendanceType: AttendanceType,
    locationName: String,
    primaryColor: Color,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (attendanceType == AttendanceType.CHECK_IN) "Konfirmasi Absen Masuk" else "Konfirmasi Absen Pulang",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Face, "Face", modifier = Modifier.size(80.dp), tint = Color.Gray)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AttendanceDetailRow("Waktu", SimpleDateFormat("HH:mm WIB", Locale.getDefault()).format(Date()))
                AttendanceDetailRow("Tanggal", SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date()))
                AttendanceDetailRow("Lokasi", locationName)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (attendanceType == AttendanceType.CHECK_IN) "KONFIRMASI ABSEN MASUK" else "KONFIRMASI ABSEN PULANG",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Batal", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AttendanceDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun LoadingDialog(primaryColor: Color) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = primaryColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Mengirim Data...", fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Report Screen Components
@Composable
fun SuccessHeaderCard(
    attendanceType: AttendanceType,
    @Suppress("UNUSED_PARAMETER") isLate: Boolean,
    @Suppress("UNUSED_PARAMETER") primaryColor: Color,
    successColor: Color,
    @Suppress("UNUSED_PARAMETER") lateColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(successColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = successColor,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Absensi Berhasil!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (attendanceType == AttendanceType.CHECK_IN) 
                    "Absensi Masuk telah tercatat" 
                else 
                    "Absensi Pulang telah tercatat",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DetailInformationCard(
    record: AttendanceRecord,
    primaryColor: Color,
    successColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Detail Absensi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ReportDetailRow(
                icon = Icons.Default.AccessTime,
                label = "Waktu Absensi",
                value = record.time,
                iconColor = primaryColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))
            
            ReportDetailRow(
                icon = Icons.Default.DateRange,
                label = "Tanggal Absensi",
                value = record.date,
                iconColor = primaryColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))
            
            ReportDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Lokasi",
                value = record.location,
                iconColor = primaryColor,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))
            
            ReportDetailRow(
                icon = Icons.Default.Face,
                label = "Verifikasi Wajah",
                value = if (record.faceVerified) "Berhasil" else "Gagal",
                iconColor = if (record.faceVerified) successColor else Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun StatusCard(
    attendanceType: AttendanceType,
    isLate: Boolean,
    faceVerified: Boolean,
    @Suppress("UNUSED_PARAMETER") primaryColor: Color,
    successColor: Color,
    lateColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (attendanceType == AttendanceType.CHECK_IN) {
                StatusRow(
                    label = "Status Kehadiran",
                    value = if (isLate) "Terlambat" else "Tepat Waktu",
                    icon = if (isLate) Icons.Default.Warning else Icons.Default.CheckCircle,
                    color = if (isLate) lateColor else successColor
                )
            } else {
                StatusRow(
                    label = "Status",
                    value = "Absen Pulang",
                    icon = Icons.Default.CheckCircle,
                    color = successColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))
            
            StatusRow(
                label = "Status Verifikasi",
                value = if (faceVerified) "Terverifikasi" else "Tidak Terverifikasi",
                icon = if (faceVerified) Icons.Default.Verified else Icons.Default.Error,
                color = if (faceVerified) successColor else Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun ReportDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    maxLines: Int = 1
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = maxLines
            )
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
