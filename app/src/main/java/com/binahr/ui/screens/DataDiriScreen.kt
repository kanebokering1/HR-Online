package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.ProfileViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DataDiriScreen(onBack: () -> Unit, vm: ProfileViewModel = viewModel()) {
    val employee by vm.employee.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val saveResult by vm.saveResult.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var snackMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveResult) {
        saveResult?.let { result ->
            result
                .onSuccess {
                    showEditDialog = false
                    snackMessage = "Profil berhasil diperbarui"
                }
                .onFailure { e ->
                    snackMessage = e.message ?: "Gagal menyimpan"
                }
            vm.clearSaveResult()
        }
    }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

    val displayName = employee?.fullName ?: vm.cachedName ?: "Karyawan"

    val sections = listOf(
        "Informasi Pribadi" to listOf(
            "Nama Lengkap" to (employee?.fullName ?: "-"),
            "NIK Karyawan" to (employee?.employeeNumber ?: "-"),
            "NPWP" to (employee?.npwp ?: "-"),
            "BPJS Kesehatan" to (employee?.bpjsKesehatan ?: "-"),
            "BPJS TK" to (employee?.bpjsKetenagakerjaan ?: "-"),
        ),
        "Informasi Kontak" to listOf(
            "No. HP" to (employee?.phone ?: "-"),
            "Alamat" to (employee?.address ?: "-"),
        ),
        "Informasi Pekerjaan" to listOf(
            "Jabatan" to (employee?.positionName ?: "-"),
            "Departemen" to (employee?.departmentName ?: "-"),
            "Tgl Bergabung" to (employee?.hireDate ?: "-"),
        ),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GradientTopBar(
                title = "Data Diri",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit kontak",
                            tint = Color.White,
                        )
                    }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Profile header
                HRCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarImage(initials = displayName.take(2).uppercase(), size = 64.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                employee?.positionName ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                            StatusBadge(text = "Aktif", type = BadgeType.SUCCESS)
                        }
                    }
                }

                sections.forEach { (title, items) ->
                    SectionHeader(title = title)
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            items.forEachIndexed { index, (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextTertiary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1.2f),
                                    )
                                }
                                if (index < items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = DividerColor,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showEditDialog) {
        EditContactDialog(
            initialPhone = employee?.phone ?: "",
            initialAddress = employee?.address ?: "",
            isSaving = isLoading,
            onSave = { phone, address -> vm.save(phone, address) },
            onDismiss = { showEditDialog = false },
        )
    }
}

// ─── Edit Contact Dialog ──────────────────────────────────────────────────────

@Composable
private fun EditContactDialog(
    initialPhone: String,
    initialAddress: String,
    isSaving: Boolean,
    onSave: (phone: String, address: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var phone by remember { mutableStateOf(initialPhone) }
    var address by remember { mutableStateOf(initialAddress) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Edit Kontak", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(phone, address) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Simpan", fontFamily = PlusJakartaSans)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Batal", color = TextSecondary, fontFamily = PlusJakartaSans)
            }
        },
    )
}
