package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.binahr.data.api.TokenManager
import com.binahr.ui.theme.*
import com.binahr.util.BiometricHelper
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
        val hasDomain = !TokenManager.tenantDomain.isNullOrBlank()
        val isSingleTenant = com.binahr.BuildConfig.TENANT_DOMAIN.isNotBlank()

        if (!hasToken) {
            // First-time install on multi-tenant build: no domain saved yet.
            // Send to TenantSetup so user can enter their company domain.
            if (!hasDomain && !isSingleTenant) {
                onNavigateToTenantSetup()
            } else {
                onNavigateToLogin()
            }
            return@LaunchedEffect
        }

        // Token exists but domain is missing on a multi-tenant build.
        // This means the token was issued in the central DB (no tenant context),
        // so all employee lookups return null and every screen shows empty data.
        // Clear the stale token and force re-auth through TenantSetup.
        if (!hasDomain && !isSingleTenant) {
            TokenManager.token = null
            onNavigateToTenantSetup()
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
            .background(MaterialTheme.colorScheme.background),
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
                            OrangeSurface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "HR",
                        color = OrangePrimary,
                        fontSize = 38.sp,
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
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Platform HR & Payroll Indonesia",
                        color = TextSecondary,
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
                    color = OrangePrimary,
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
                text = "v${com.binahr.BuildConfig.VERSION_NAME}",
                color = TextTertiary,
                fontSize = 12.sp,
                fontFamily = PlusJakartaSans,
            )
        }
    }
}
