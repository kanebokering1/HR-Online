package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.LemburViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LemburScreen(onBack: () -> Unit, vm: LemburViewModel = viewModel()) {
    var showForm by remember { mutableStateOf(false) }
    var formDate by remember { mutableStateOf("") }
    var formStart by remember { mutableStateOf("") }
    var formEnd by remember { mutableStateOf("") }
    var formReason by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    val requests by vm.requests.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            showForm = false
            formDate = ""; formStart = ""; formEnd = ""; formReason = ""
            vm.clearSubmitResult()
        }
    }

    val approvedCount = requests.count { it.approvalState.equals("approved", ignoreCase = true) }
    val pendingCount = requests.count { it.approvalState.equals("pending", ignoreCase = true) || it.approvalState.equals("submitted", ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Lembur", onBack = onBack)

        // Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(Icons.Filled.AccessTime, "$approvedCount", "Disetujui", AccentOrange, AccentOrangeLight, Modifier.weight(1f))
            StatCard(Icons.Filled.PendingActions, "$pendingCount", "Pending", AccentAmber, AccentAmberLight, Modifier.weight(1f))
        }

        HRButton(
            text = "Ajukan Lembur",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Lembur")

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(requests) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(item.overtimeDate, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${item.startTime} - ${item.endTime}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            StatusBadge(
                                text = item.approvalState,
                                type = when (item.approvalState.lowercase()) {
                                    "approved" -> BadgeType.SUCCESS
                                    "rejected" -> BadgeType.ERROR
                                    else -> BadgeType.WARNING
                                },
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Alasan: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        }

        // ── Form Dialog ────────────────────────────────────────────
        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = {
                    Text(
                        "Ajukan Lembur",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formDate,
                            onValueChange = { formDate = it; formError = false },
                            label = { Text("Tanggal Lembur (dd MMM yyyy)") },
                            isError = formError && formDate.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formStart,
                                onValueChange = { formStart = it; formError = false },
                                label = { Text("Jam Mulai") },
                                placeholder = { Text("17:00") },
                                isError = formError && formStart.isBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = formEnd,
                                onValueChange = { formEnd = it },
                                label = { Text("Jam Selesai") },
                                placeholder = { Text("20:00") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        OutlinedTextField(
                            value = formReason,
                            onValueChange = { formReason = it; formError = false },
                            label = { Text("Alasan Lembur") },
                            minLines = 2,
                            isError = formError && formReason.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (formError) {
                            Text(
                                "Tanggal, jam mulai, dan alasan wajib diisi",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formDate.isBlank() || formStart.isBlank() || formReason.isBlank()) {
                                formError = true
                            } else {
                                vm.submit(formDate, formStart, formEnd.ifBlank { formStart }, formReason)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Kirim", fontFamily = PlusJakartaSans)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForm = false; formError = false }) {
                        Text("Batal", color = TextSecondary, fontFamily = PlusJakartaSans)
                    }
                },
            )
        }
    }
}






















