package com.binahr.ui.components


import com.binahr.BuildConfig
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.binahr.ui.theme.*
import com.binahr.util.CameraHelper
import java.io.File
import java.util.concurrent.Executors

/**
 * Selfie camera card for attendance check-in.
 * When [captured] is null: shows live CameraX preview (front camera) + shutter button.
 * When [captured] is non-null: shows the captured photo with a "Retake" option.
 *
 * [onCaptured] is called with (file, base64) once the shutter is pressed and photo saved.
 * [onRetake] is called when the user taps "Ambil ulang".
 */
@Composable
fun SelfieCameraCard(
    captured: File?,
    capturedBitmap: Bitmap?,
    onCaptured: (File, String) -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keep a reference to ImageCapture so shutter can trigger it.
    val imageCaptureRef = remember { mutableStateOf<ImageCapture?>(null) }
    val isCapturing = remember { mutableStateOf(false) }

    HRCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Camera, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (captured == null) "Foto Selfie" else "Selfie Tersimpan ✓",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(12.dp))

            if (captured == null) {
                // Live camera preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            bindCamera(ctx, previewView, lifecycleOwner, imageCaptureRef)
                            previewView
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Shutter button
                    Box(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .size(56.dp)
                            .background(OrangePrimary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick = {
                                val capture = imageCaptureRef.value ?: return@IconButton
                                isCapturing.value = true
                                capture.takePicture(
                                    Executors.newSingleThreadExecutor(),
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val result = CameraHelper.saveImageProxyLocally(context, image)
                                            image.close()
                                            isCapturing.value = false
                                            if (result != null) {
                                                onCaptured(result.first, result.second)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isCapturing.value = false
                                        }
                                    },
                                )
                            },
                            enabled = !isCapturing.value,
                        ) {
                            Icon(Icons.Filled.Camera, contentDescription = "Ambil foto", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }

                    if (isCapturing.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center).size(40.dp),
                            color = OrangePrimary,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Tekan tombol kamera untuk mengambil foto selfie", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
            } else {
                // Show captured photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap.asImageBitmap(),
                            contentDescription = "Foto selfie",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    // Green checkmark overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(OrangePrimary, CircleShape)
                            .padding(4.dp),
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Foto selfie berhasil diambil ✓", style = MaterialTheme.typography.bodySmall, color = OrangePrimary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onRetake) {
                    Text("Ambil ulang", fontSize = 12.sp, color = TextTertiary)
                }
            }
        }
    }
}

private fun bindCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCaptureRef: MutableState<ImageCapture?>,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageCaptureRef.value = imageCapture

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageCapture,
            )
        } catch (e: Exception) {
            // Camera bind failed — hardware unavailable
        }
    }, ContextCompat.getMainExecutor(context))
}
