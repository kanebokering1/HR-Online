package com.binahr.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.data.AttendanceRecord
import com.binahr.data.AttendanceStorage
import com.binahr.data.api.model.AttendanceLogDto
import com.binahr.data.AttendanceType
import com.binahr.ui.components.BinaTopBar
import com.binahr.ui.theme.*
import com.binahr.util.CameraHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private fun DrawScope.drawBracket(rect: RectF, color: Color, strokeWidth: Float, armLength: Float) {
    val x = rect.left; val y = rect.top; val w = rect.width(); val h = rect.height()
    // TL
    drawLine(color, Offset(x, y + armLength), Offset(x, y), strokeWidth = strokeWidth)
    drawLine(color, Offset(x, y), Offset(x + armLength, y), strokeWidth = strokeWidth)
    // TR
    drawLine(color, Offset(x + w - armLength, y), Offset(x + w, y), strokeWidth = strokeWidth)
    drawLine(color, Offset(x + w, y), Offset(x + w, y + armLength), strokeWidth = strokeWidth)
    // BL
    drawLine(color, Offset(x, y + h - armLength), Offset(x, y + h), strokeWidth = strokeWidth)
    drawLine(color, Offset(x, y + h), Offset(x + armLength, y + h), strokeWidth = strokeWidth)
    // BR
    drawLine(color, Offset(x + w - armLength, y + h), Offset(x + w, y + h), strokeWidth = strokeWidth)
    drawLine(color, Offset(x + w, y + h - armLength), Offset(x + w, y + h), strokeWidth = strokeWidth)
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun AttendanceFaceScreen(
    attendanceType: String,
    lat: Double,
    lon: Double,
    address: String,
    onBack: () -> Unit,
    onSuccess: (record: AttendanceRecord) -> Unit,
    vm: AttendanceViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val isCheckIn = attendanceType != "CHECK_OUT"

    val checkInResult by vm.checkInResult.collectAsStateWithLifecycle()
    val checkOutResult by vm.checkOutResult.collectAsStateWithLifecycle()

    val now = remember { Date() }
    val timeStr = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now) }
    val dateStr = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(now)
    }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) cameraPermLauncher.launch(Manifest.permission.CAMERA)
    }

    // Face detection state
    var faceDetected by remember { mutableStateOf(false) }
    var autoCapturing by remember { mutableStateOf(false) }
    var faceBoundingBox by remember { mutableStateOf<RectF?>(null) }
    var previewSize by remember { mutableStateOf(Size(1f, 1f)) }

    val bracketColor by animateColorAsState(
        targetValue = if (faceDetected) OrangePrimary else Color.White.copy(alpha = 0.8f),
        animationSpec = tween(300),
        label = "bracket_color",
    )

    // CameraX setup
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }

    val faceDetectorOptions = remember {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()
    }
    val faceDetector = remember { FaceDetection.getClient(faceDetectorOptions) }

    // Observe API results
    LaunchedEffect(checkInResult, checkOutResult) {
        val result: Result<AttendanceLogDto>? = checkInResult ?: checkOutResult
        result?.let {
            it.onSuccess { log ->
                val record = AttendanceRecord(
                    id = log.id,
                    type = if (isCheckIn) AttendanceType.CHECK_IN else AttendanceType.CHECK_OUT,
                    date = dateStr,
                    time = "$timeStr WIB",
                    location = log.checkInAddress ?: address,
                    timestamp = System.currentTimeMillis(),
                    faceVerified = true,
                )
                AttendanceStorage.saveAttendance(context, record)
                onSuccess(record)
            }.onFailure {
                autoCapturing = false
                faceDetected = false
            }
            vm.clearResults()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        if (hasCameraPermission) {
            // ── CameraX Preview ────────────────────────────────────────
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !autoCapturing) {
                                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                previewSize = Size(imageProxy.width.toFloat(), imageProxy.height.toFloat())
                                faceDetector.process(inputImage)
                                    .addOnSuccessListener { faces ->
                                        if (faces.isNotEmpty() && !autoCapturing) {
                                            val box = faces[0].boundingBox
                                            faceBoundingBox = RectF(
                                                box.left.toFloat(), box.top.toFloat(),
                                                box.right.toFloat(), box.bottom.toFloat()
                                            )
                                            if (!faceDetected) faceDetected = true
                                        } else if (!autoCapturing) {
                                            faceDetected = false
                                            faceBoundingBox = null
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                imageAnalysis,
                                imageCapture,
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )

            // ── Face bracket overlay ───────────────────────────────────
            val box = faceBoundingBox
            if (box != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scaleX = size.width / previewSize.height  // rotated for front camera
                    val scaleY = size.height / previewSize.width
                    val padding = 30f
                    val scaled = RectF(
                        box.left * scaleX - padding,
                        box.top * scaleY - padding,
                        box.right * scaleX + padding,
                        box.bottom * scaleY + padding,
                    )
                    drawBracket(scaled, bracketColor, strokeWidth = 4.dp.toPx(), armLength = 30.dp.toPx())
                }
            }

            // Auto-trigger capture when face detected
            LaunchedEffect(faceDetected) {
                if (faceDetected && !autoCapturing) {
                    delay(800L) // brief "lock" delay to let animation show
                    autoCapturing = true
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                val result = CameraHelper.saveImageProxyLocally(context, imageProxy)
                                imageProxy.close()
                                scope.launch {
                                    val base64 = result?.second
                                    if (isCheckIn) vm.checkIn(lat, lon, address, base64)
                                    else vm.checkOut(lat, lon, address)
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                autoCapturing = false
                                faceDetected = false
                            }
                        },
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ────────────────────────────────────────────────
            BinaTopBar(
                title = if (isCheckIn) "Absen Masuk" else "Absen Pulang",
                onBack = onBack,
            )

            // ── Shift info card ────────────────────────────────────────
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

            Spacer(Modifier.weight(1f))

            // ── Detecting label ────────────────────────────────────────
            if (!hasCameraPermission) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Izin kamera diperlukan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { cameraPermLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Izinkan Kamera")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (autoCapturing) {
                        CircularProgressIndicator(color = OrangePrimary, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Menyimpan data absensi...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Text(
                            text = "MENDETEKSI WAJAH OTOMATIS",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (faceDetected) OrangePrimary else Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (faceDetected)
                                "Wajah terdeteksi — sedang mengambil foto..."
                            else
                                "Pastikan wajah terlihat jelas dan tidak memakai aksesori\nyang menutupi wajah seperti masker atau kacamata hitam.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
