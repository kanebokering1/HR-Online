package com.example.hronline.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hronline.data.auth.GoogleSignInHelper
import com.example.hronline.ui.components.HRButton
import com.example.hronline.ui.components.HRTextField
import com.example.hronline.ui.theme.*
import com.example.hronline.data.repository.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
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
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "HR",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlusJakartaSans,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "HROES",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Masuk ke akun Anda",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSans,
                    )
                }
            }

            // Form Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Masukkan email/NIK dan kata sandi Anda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Email/NIK
                    HRTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email atau NIK",
                        leadingIcon = Icons.Filled.Email,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    HRTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Kata Sandi",
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password",
                                )
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remember + Forgot
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
                            Text(
                                text = "Ingat Saya",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            text = "Lupa Kata Sandi?",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Hubungi HR Admin untuk reset password.")
                                }
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Button
                    HRButton(
                        text = "Masuk",
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                loginError = "Email dan password wajib diisi"
                                return@HRButton
                            }
                            loginError = null
                            isLoading = true
                            scope.launch {
                                try {
                                    val result = AuthRepository().login(email, password)
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { onLoginSuccess() },
                                        onFailure = { e -> loginError = e.message ?: "Login gagal" },
                                    )
                                } catch (e: Exception) {
                                    isLoading = false
                                    loginError = e.message ?: "Terjadi kesalahan tidak terduga"
                                }
                            }
                        },
                        isLoading = isLoading,
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loginError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    // ── Divider "atau" ─────────────────────────────────
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Sign-In button
                        OutlinedButton(
                            onClick = {
                                if (isLoading) return@OutlinedButton
                                loginError = null
                                isLoading = true
                                scope.launch {
                                    try {
                                        val tokenResult = GoogleSignInHelper.signIn(context)
                                        tokenResult.fold(
                                            onSuccess = { idToken ->
                                                val result = AuthRepository().loginWithGoogle(idToken)
                                                isLoading = false
                                                result.fold(
                                                    onSuccess = { onLoginSuccess() },
                                                    onFailure = { e -> loginError = e.message ?: "Login Google gagal" },
                                                )
                                            },
                                            onFailure = { e ->
                                                isLoading = false
                                                loginError = e.message ?: "Login Google dibatalkan"
                                            }
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
                            Text(
                                "Masuk dengan Google",
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Version
                    Text(
                        text = "HR Online v2.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
    }
}
