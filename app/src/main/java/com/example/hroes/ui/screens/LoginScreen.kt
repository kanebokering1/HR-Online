package com.example.hroes.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hroes.BuildConfig
import com.example.hroes.data.api.TokenManager
import com.example.hroes.data.auth.GoogleSignInHelper
import com.example.hroes.data.repository.AuthRepository
import com.example.hroes.ui.components.HRButton
import com.example.hroes.ui.components.HRTextField
import com.example.hroes.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val isSingleTenant = BuildConfig.TENANT_DOMAIN.isNotBlank()

    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe    by remember { mutableStateOf(false) }
    var isLoading     by remember { mutableStateOf(false) }
    var loginError    by remember { mutableStateOf<String?>(null) }

    val scope            = rememberCoroutineScope()
    val snackbarState    = remember { SnackbarHostState() }
    val context          = LocalContext.current
    val focusManager     = LocalFocusManager.current

    // Helper: perform login with current state
    // tenantDomain was already resolved and stored by TenantSetupScreen.
    fun doLogin(domainOverride: String? = null) {
        val domain = domainOverride ?: TokenManager.tenantDomain
        if (!isSingleTenant && domain.isNullOrBlank()) {
            loginError = "Kode perusahaan belum diatur. Kembali dan masukkan kode perusahaan."
            return
        }
        if (email.isBlank() || password.isBlank()) {
            loginError = "Email dan kata sandi wajib diisi"
            return
        }
        loginError = null
        isLoading  = true
        focusManager.clearFocus()
        scope.launch {
            val result = AuthRepository().login(
                email        = email.trim(),
                password     = password,
                tenantDomain = domain,
            )
            isLoading = false
            result.fold(
                onSuccess = { onLoginSuccess() },
                onFailure = { e -> loginError = e.message ?: "Login gagal" },
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.statusBarsPadding(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "B",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlusJakartaSans,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BINA HR",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Platform HR & Payroll Indonesia",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSans,
                    )
                }
            }

            // ── Form card ──────────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {

                    Text(
                        text = "Masuk",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Masukkan email dan kata sandi Anda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Email ─────────────────────────────────────────────────
                    HRTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email atau NIK",
                        leadingIcon = Icons.Filled.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Password ──────────────────────────────────────────────
                    HRTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Kata Sandi",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { doLogin() }),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility
                                                  else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password",
                                )
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Remember + Lupa sandi ─────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = GreenPrimary),
                            )
                            Text("Ingat Saya", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "Lupa Kata Sandi?",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    snackbarState.showSnackbar("Hubungi HR Admin perusahaan Anda untuk reset password.")
                                }
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Error message ─────────────────────────────────────────
                    AnimatedVisibility(visible = loginError != null) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = loginError ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // ── Login button ──────────────────────────────────────────
                    HRButton(
                        text = "Masuk",
                        onClick = { doLogin() },
                        isLoading = isLoading,
                    )

                    // ── Google Sign-In ────────────────────────────────────────
                    if (GoogleSignInHelper.isConfigured()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                "  atau  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary,
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                if (isLoading) return@OutlinedButton
                                val domain = TokenManager.tenantDomain
                                if (!isSingleTenant && domain.isNullOrBlank()) {
                                    loginError = "Kode perusahaan belum diatur. Kembali dan masukkan kode perusahaan."
                                    return@OutlinedButton
                                }
                                loginError = null
                                isLoading = true
                                scope.launch {
                                    try {
                                        val tokenResult = GoogleSignInHelper.signIn(context)
                                        tokenResult.fold(
                                            onSuccess = { idToken ->
                                                val result = AuthRepository().loginWithGoogle(
                                                    idToken      = idToken,
                                                    tenantDomain = domain,
                                                )
                                                isLoading = false
                                                result.fold(
                                                    onSuccess = { onLoginSuccess() },
                                                    onFailure = { e -> loginError = e.message ?: "Login Google gagal" },
                                                )
                                            },
                                            onFailure = { e ->
                                                isLoading = false
                                                loginError = e.message ?: "Login Google dibatalkan"
                                            },
                                        )
                                    } catch (e: Exception) {
                                        isLoading = false
                                        loginError = e.message ?: "Terjadi kesalahan"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                        ) {
                            Text("Masuk dengan Google", fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "BINA HR v${com.example.hroes.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}
























