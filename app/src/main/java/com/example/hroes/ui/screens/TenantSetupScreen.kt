package com.example.hroes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.TokenManager
import com.example.hroes.ui.components.HRButton
import com.example.hroes.ui.components.HRTextField
import com.example.hroes.ui.theme.*
import kotlinx.coroutines.launch

/**
 * First-launch screen for multi-tenant builds.
 *
 * The user enters a short "Kode Perusahaan" (= Stancl tenant ID, e.g. "SML").
 * The screen calls the public /api/v1/tenant/resolve?code=SML endpoint to look
 * up the registered domain, then stores it in TokenManager and proceeds to Login.
 *
 * On single-tenant builds (BuildConfig.TENANT_DOMAIN is set), this screen is
 * bypassed entirely by SplashScreen.
 */
@Composable
fun TenantSetupScreen(onDomainSaved: () -> Unit) {
    var code        by remember { mutableStateOf("") }
    var isLoading   by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf<String?>(null) }
    var resolvedName by remember { mutableStateOf<String?>(null) }

    val focusMgr  = LocalFocusManager.current
    val scope     = rememberCoroutineScope()

    fun resolve() {
        val trimmed = code.trim().lowercase()
        if (trimmed.isBlank()) {
            error = "Kode perusahaan tidak boleh kosong"
            return
        }
        error = null
        resolvedName = null
        isLoading = true
        focusMgr.clearFocus()

        scope.launch {
            try {
                val response = ApiConfig.apiService.resolveTenant(trimmed)
                if (response.success && response.data != null) {
                    val domain = response.data.domain
                    resolvedName = response.data.name
                    // Brief pause so user can see the company name confirmation
                    kotlinx.coroutines.delay(600)
                    TokenManager.tenantDomain = domain
                    onDomainSaved()
                } else {
                    error = response.message.takeIf { it.isNotBlank() }
                        ?: "Kode tidak ditemukan. Periksa kembali kode dari HR admin Anda."
                }
            } catch (e: retrofit2.HttpException) {
                error = when (e.code()) {
                    404 -> "Kode perusahaan tidak ditemukan. Pastikan kode dari HR admin Anda sudah benar."
                    429 -> "Terlalu banyak percobaan. Tunggu sebentar dan coba lagi."
                    else -> "Gagal menghubungi server (${e.code()}). Coba lagi."
                }
            } catch (e: java.io.IOException) {
                error = "Tidak ada koneksi internet. Periksa jaringan Anda dan coba lagi."
            } catch (e: Exception) {
                error = "Terjadi kesalahan. Coba lagi."
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ─────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
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
                    Icon(
                        Icons.Filled.Apartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Selamat Datang di BINA HR",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Masukkan kode perusahaan dari HR admin Anda",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
        }

        // ── Form card ───────────────────────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {

                Text(
                    text = "Kode Perusahaan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kode ini diberikan oleh admin HR atau IT perusahaan Anda. " +
                           "Biasanya berupa singkatan nama perusahaan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                HRTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        error = null
                        resolvedName = null
                    },
                    label = "Kode Perusahaan",
                    placeholder = "Contoh: SML",
                    leadingIcon = Icons.Filled.Apartment,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (!isLoading) resolve() }),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Error ────────────────────────────────────────────────────────
                AnimatedVisibility(visible = error != null) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // ── Success confirmation ──────────────────────────────────────────
                AnimatedVisibility(visible = resolvedName != null) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Green50,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Ditemukan: ${resolvedName ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = GreenPrimary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                HRButton(
                    text = if (isLoading) "Memeriksa…" else "Lanjutkan",
                    onClick = { if (!isLoading) resolve() },
                    enabled = !isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Info box ─────────────────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Green50,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            Icons.Filled.Apartment,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp),
                        )
                        Text(
                            text = "Kode perusahaan memastikan aplikasi terhubung ke database " +
                                   "perusahaan Anda. Data karyawan perusahaan lain tidak bisa diakses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimary,
                        )
                    }
                }
            }
        }
    }
}


