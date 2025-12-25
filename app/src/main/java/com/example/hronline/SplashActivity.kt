package com.example.hronline

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hronline.ui.theme.SplashTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class SplashScreenState {
    Logo, FindingLocation
}

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashTheme {
                var location by remember { mutableStateOf("Unknown") }
                var screenState by remember { mutableStateOf(SplashScreenState.Logo) }
                val context = LocalContext.current

                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                fun navigateToMain(loc: String) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("user_location", loc)
                    startActivity(intent)
                    finish()
                }

                fun updateLocationName(loc: Location?) {
                    if (loc != null) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            // Mengambil maksimal 1 hasil alamat
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Use new API for Android 13+
                                geocoder.getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                                    if (addresses.isNotEmpty()) {
                                        val address = addresses[0]
                                        // Mencoba mengambil nama Kota/Kabupaten
                                        val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                        val country = address.countryName
                                        
                                        // Format: "Jakarta, Indonesia"
                                        val fullAddress = listOfNotNull(city, country).joinToString(", ")
                                        location = if (fullAddress.isBlank()) "Lokasi Terdeteksi" else fullAddress
                                        navigateToMain(location)
                                    } else {
                                        // Fallback jika geocoder gagal tapi koordinat ada
                                        location = "Lat: ${String.format("%.2f", loc.latitude)}, Lon: ${String.format("%.2f", loc.longitude)}"
                                        navigateToMain(location)
                                    }
                                }
                            } else {
                                // Use deprecated API for older Android versions
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                if (addresses != null && addresses.isNotEmpty()) {
                                    val address = addresses[0]
                                    // Mencoba mengambil nama Kota/Kabupaten
                                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                                    val country = address.countryName
                                    
                                    // Format: "Jakarta, Indonesia"
                                    val fullAddress = listOfNotNull(city, country).joinToString(", ")
                                    location = if (fullAddress.isBlank()) "Lokasi Terdeteksi" else fullAddress
                                } else {
                                    // Fallback jika geocoder gagal tapi koordinat ada
                                    location = "Lat: ${String.format("%.2f", loc.latitude)}, Lon: ${String.format("%.2f", loc.longitude)}"
                                }
                                navigateToMain(location)
                            }
                        } catch (e: Exception) {
                            // Fallback jika terjadi error jaringan pada Geocoder
                             location = "Lat: ${String.format("%.2f", loc.latitude)}, Lon: ${String.format("%.2f", loc.longitude)}"
                             navigateToMain(location)
                        }
                    } else {
                        location = "Lokasi Tidak Dikenal"
                        navigateToMain(location)
                    }
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        updateLocationName(locationResult.lastLocation)
                    }
                }

                @SuppressLint("MissingPermission")
                fun startLocationProcess() {
                    // 1. Coba ambil lokasi terakhir (Last Known Location) dulu karena lebih cepat
                    fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            // Jika ada, langsung gunakan!
                            updateLocationName(loc)
                        } else {
                            // 2. Jika tidak ada, baru minta update lokasi baru
                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                                .setWaitForAccurateLocation(false)
                                .setMinUpdateIntervalMillis(500)
                                .setMaxUpdateDelayMillis(1000)
                                .build()
                                
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                        }
                    }.addOnFailureListener {
                        // Jika gagal ambil lastLocation, paksa request update
                         val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                         fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                    }
                }

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) startLocationProcess() else updateLocationName(null)
                }

                LaunchedEffect(screenState) {
                    when (screenState) {
                        SplashScreenState.Logo -> {
                            delay(2000)
                            screenState = SplashScreenState.FindingLocation
                        }
                        SplashScreenState.FindingLocation -> {
                            // Timeout mechanism (Dinaikkan jadi 8 detik untuk emulator yang lambat)
                            val job = launch {
                                delay(8000) 
                                if (screenState == SplashScreenState.FindingLocation) {
                                    // Jika timeout, jangan kirim null, tapi kirim string default
                                    navigateToMain("Gagal Mendeteksi Lokasi")
                                }
                            }

                            delay(1000)
                            val playServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                            if (playServicesAvailable == ConnectionResult.SUCCESS) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    startLocationProcess()
                                } else {
                                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            } else {
                                job.cancel()
                                updateLocationName(null)
                            }
                        }
                    }
                }

                SplashScreenContent(state = screenState)
            }
        }
    }
}

// Warna Tema (Private agar tidak konflik)
private val PrimaryColor = Color(0xFFFF6568)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreenContent(state: SplashScreenState) {
    // Background Solid (Tanpa Gradien)
    Surface(modifier = Modifier.fillMaxSize(), color = PrimaryColor) {
        Box {
            
            // Elemen Dekoratif (Lingkaran tipis di background)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.8f,
                    center = Offset(size.width, 0f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.width * 0.5f,
                    center = Offset(0f, size.height)
                )
            }

            // Konten Animasi
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                },
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { targetState ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (targetState) {
                        SplashScreenState.Logo -> LogoStage()
                        SplashScreenState.FindingLocation -> FindingLocationStage()
                    }
                }
            }
            
            // Footer Copyright
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Powered by HR System",
                    color = Color.White.copy(alpha = 0.5f), // Teks putih transparan
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LogoStage() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Logo Scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000),
        label = "Logo Alpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Logo Aplikasi
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .alpha(alpha)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "HR ONLINE",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.alpha(alpha),
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun FindingLocationStage() {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Ripple effect circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .alpha(0.2f)
                    .background(Color.White, shape = MaterialTheme.shapes.extraLarge) 
            )
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Finding Location",
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TypingText(text = "Mencari lokasi Anda...")
    }
}

@Composable
fun TypingText(text: String) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        while(true) {
            for (i in 1..text.length) {
                displayedText = text.substring(0, i)
                delay(50)
            }
            delay(1000)
            displayedText = ""
            delay(300)
        }
    }

    Text(
        text = displayedText,
        color = Color.White, 
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    SplashTheme {
        SplashScreenContent(SplashScreenState.Logo)
    }
}
