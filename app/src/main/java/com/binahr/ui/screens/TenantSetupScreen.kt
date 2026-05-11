package com.binahr.ui.screens

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
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.TokenManager
import com.binahr.ui.components.HRButton
import com.binahr.ui.components.HRTextField
import com.binahr.ui.theme.*
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
    var code          by remember { mutableStateOf("") }
    var isLoading    by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf<String?>(null) }
    var resolvedName   by remember { mutableStateOf<String?>(null) }
    var resolvedDomain by remember { mutableStateOf<String?>(null) }

    val focusMgr  = LocalFocusManager.current
    val scope     = rememberCoroutineScope()

    // Phase 1 — look up the company code. Does NOT save domain yet.
    fun resolve() {
        val trimmed = code.trim().lowercase()
        if (trimmed.isBlank()) {
            error = "Kode perusahaan tidak boleh kosong"
            return
        }
        error = null
        resolvedName   = null
        resolvedDomain = null
        isLoading = true
        focusMgr.clearFocus()

        scope.launch {
            try {
                val response = ApiConfig.apiService.resolveTenant(trimmed)
                if (response.success && response.data != null) {
                    resolvedName   = response.data.name?.takeIf { it.isNotBlank() } ?: response.data.tenantId
                    resolvedDomain = response.data.domain
                } else {
                    error = response.message?.takeIf { it.isNotBlank() }
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

    // Phase 2 — user confirmed the domain, save it and proceed to Login.
    fun confirm() {
        val domain = resolvedDomain ?: return
        TokenManager.tenantDomain = domain
        onDomainSaved()
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
                .height(220.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.statusBarsPadding(),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(OrangeSurface, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Apartment,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Selamat Datang di BINA HR",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Masukkan kode perusahaan dari HR admin Anda",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
            }
        }

        HorizontalDivider(color = DividerColor)

        // ── Form card ───────────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

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
                        resolvedName   = null
                        resolvedDomain = null
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
                // Shown once the API found a matching tenant. User must tap
                // "Lanjutkan ke Login" to confirm before the domain is saved.
                AnimatedVisibility(visible = resolvedDomain != null) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OrangeSurface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = resolvedName ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OrangePrimary,
                                    )
                                    Text(
                                        text = resolvedDomain ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrangeHover,
                                    )
                                    Text(
                                        text = "Login Anda akan terhubung ke sistem perusahaan ini.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                HRButton(
                    text = when {
                        isLoading        -> "Memeriksa…"
                        resolvedDomain != null -> "Lanjutkan ke Login"
                        else             -> "Cari Perusahaan"
                    },
                    onClick = {
                        if (!isLoading) {
                            if (resolvedDomain != null) confirm() else resolve()
                        }
                    },
                    enabled = !isLoading,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Info box ─────────────────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangeSurface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            Icons.Filled.Apartment,
                            contentDescription = null,
                            tint = OrangePrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp),
                        )
                        Text(
                            text = "Kode perusahaan memastikan aplikasi terhubung ke database " +
                                   "perusahaan Anda. Data karyawan perusahaan lain tidak bisa diakses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OrangeHover,
                        )
                    }
                }
            }
        }
    }
}


