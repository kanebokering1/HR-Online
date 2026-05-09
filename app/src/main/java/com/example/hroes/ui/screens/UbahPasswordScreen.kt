package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.UbahPasswordViewModel

@Composable
fun UbahPasswordScreen(
    onBack: () -> Unit,
    vm: UbahPasswordViewModel = viewModel(),
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val isLoading by vm.isLoading.collectAsState()
    val result    by vm.result.collectAsState()

    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
    val isValid = currentPassword.isNotEmpty() && newPassword.length >= 8 && passwordsMatch

    // Handle result
    result?.let { res ->
        if (res.isSuccess) {
            AlertDialog(
                onDismissRequest = { vm.clearResult(); onBack() },
                icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(48.dp)) },
                title = { Text("Berhasil!", fontWeight = FontWeight.Bold) },
                text = { Text("Password berhasil diubah. Sesi lain telah diakhiri untuk keamanan.") },
                confirmButton = {
                    TextButton(onClick = { vm.clearResult(); onBack() }) {
                        Text("OK", color = GreenPrimary)
                    }
                },
            )
        } else {
            AlertDialog(
                onDismissRequest = { vm.clearResult() },
                icon = { Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) },
                title = { Text("Gagal", fontWeight = FontWeight.Bold) },
                text = { Text(res.exceptionOrNull()?.message ?: "Terjadi kesalahan") },
                confirmButton = {
                    TextButton(onClick = { vm.clearResult() }) { Text("OK") }
                },
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Ubah Password", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HRCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keamanan Password", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Password minimal 8 karakter, disarankan menggunakan kombinasi huruf besar, huruf kecil, angka, dan simbol.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }

            // Current Password
            HRTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Password Saat Ini",
                leadingIcon = Icons.Filled.Lock,
                visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrent = !showCurrent }) {
                        Icon(
                            if (showCurrent) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                        )
                    }
                },
            )

            // New Password
            HRTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Password Baru",
                leadingIcon = Icons.Filled.LockOpen,
                visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNew = !showNew }) {
                        Icon(
                            if (showNew) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                        )
                    }
                },
                supportingText = if (newPassword.isNotEmpty() && newPassword.length < 8) {
                    { Text("Minimal 8 karakter") }
                } else null,
                isError = newPassword.isNotEmpty() && newPassword.length < 8,
            )

            // Confirm Password
            HRTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Password Baru",
                leadingIcon = Icons.Filled.LockOpen,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                        )
                    }
                },
                supportingText = if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    { Text("Password tidak cocok") }
                } else null,
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            )

            Spacer(modifier = Modifier.height(8.dp))

            HRButton(
                text = if (isLoading) "Menyimpan…" else "Simpan Password",
                onClick = { vm.changePassword(currentPassword, newPassword) },
                enabled = isValid && !isLoading,
            )
        }
    }
}























