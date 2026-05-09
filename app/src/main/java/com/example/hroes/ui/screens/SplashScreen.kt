package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.hroes.data.api.TokenManager
import com.example.hroes.ui.theme.*
import com.example.hroes.util.BiometricHelper
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit = onNavigateToLogin,
    onNavigateToTenantSetup: () -> Unit = onNavigateToLogin,
) {
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "logo_scale"
    )

    LaunchedEffect(Unit) {
        showLogo = true
        delay(400)
        showText = true
        delay(400)
        showLoader = true
        delay(1400)

        val hasToken = !TokenManager.token.isNullOrBlank()

        if (!hasToken) {
            // First-time install on multi-tenant build: no domain saved yet.
            // Send to TenantSetup so user can enter their company domain.
            val hasDomain  = !TokenManager.tenantDomain.isNullOrBlank()
            val isSingleTenant = com.example.hroes.BuildConfig.TENANT_DOMAIN.isNotBlank()
            if (!hasDomain && !isSingleTenant) {
                onNavigateToTenantSetup()
            } else {
                onNavigateToLogin()
            }
            return@LaunchedEffect
        }

        // Token exists. If user opted in to biometric AND device supports it,
        // require fingerprint/face before unlocking the app.
        val activity = context as? FragmentActivity
        val needsBiometric = activity != null &&
            BiometricHelper.isEnabled(context) &&
            BiometricHelper.isAvailable(context)

        if (needsBiometric) {
            BiometricHelper.authenticate(
                activity = activity!!,
                onSuccess = { onNavigateToMain() },
                onError = {
                    // On biometric failure/cancel, fall back to password login.
                    onNavigateToLogin()
                },
            )
        } else {
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Logo
            Box(
                modifier = Modifier.scale(logoScale),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "HR",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Name
            AnimatedVisibility(
                visible = showText,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BINA HR",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Platform HR & Payroll Indonesia",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSans,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Loading
            AnimatedVisibility(
                visible = showLoader,
                enter = fadeIn(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }

        // Version text
        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        ) {
            Text(
                text = "v2.0",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = PlusJakartaSans,
            )
        }
    }
}























