package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.CutiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutiScreen(onBack: () -> Unit, vm: CutiViewModel = viewModel()) {
    var showForm by remember { mutableStateOf(false) }
    var formType by remember { mutableStateOf("Cuti Tahunan") }
    var formStart by remember { mutableStateOf("") }
    var formEnd by remember { mutableStateOf("") }
    var formReason by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf(false) }

    val applications by vm.applications.collectAsStateWithLifecycle()
    val balances by vm.balances.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    // Close form and refresh on successful submission.
    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            showForm = false
            formStart = ""; formEnd = ""; formReason = ""
            vm.clearSubmitResult()
        }
    }

    // Cuti Tahunan balance
    val sisaCuti = balances.firstOrNull { it.leaveType.contains("Tahunan", ignoreCase = true) }?.remainingDays?.toInt() ?: 0
    val totalCuti = balances.firstOrNull { it.leaveType.contains("Tahunan", ignoreCase = true) }?.allocatedDays?.toInt() ?: 12

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Pengajuan Cuti", onBack = onBack)

        // Error banner
        error?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(msg, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { vm.clearError() }) { Icon(Icons.Filled.Close, null) }
                }
            }
        }

        // Sisa Cuti Card
        HRCard(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Sisa Cuti Tahunan", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$sisaCuti", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        Text(" / $totalCuti hari", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    }
                }
                LinearProgressIndicator(
                    progress = { if (totalCuti > 0) sisaCuti.toFloat() / totalCuti else 0f },
                    modifier = Modifier.width(100.dp).height(8.dp),
                    color = GreenPrimary,
                    trackColor = Green50,
                )
            }
        }

        HRButton(
            text = "Ajukan Cuti Baru",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Pengajuan")

        if (isLoading && applications.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(applications) { item ->
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(item.leaveType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CalendarMonth, null, modifier = Modifier.size(16.dp), tint = TextTertiary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${item.startDate} - ${item.endDate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("(${item.totalDays} hari)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = AccentBlue)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Alasan: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                    }
                }
            }
        }

        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = { Text("Ajukan Cuti Baru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = PlusJakartaSans) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                            OutlinedTextField(
                                value = formType, onValueChange = {}, readOnly = true,
                                label = { Text("Jenis Cuti") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                            )
                            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                listOf("Cuti Tahunan", "Cuti Sakit", "Cuti Besar", "Cuti Melahirkan").forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { formType = t; typeExpanded = false })
                                }
                            }
                        }
                        OutlinedTextField(
                            value = formStart, onValueChange = { formStart = it; formError = false },
                            label = { Text("Tanggal Mulai (yyyy-MM-dd)") },
                            isError = formError && formStart.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = formEnd, onValueChange = { formEnd = it },
                            label = { Text("Tanggal Selesai (yyyy-MM-dd)") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = formReason, onValueChange = { formReason = it; formError = false },
                            label = { Text("Alasan / Keterangan") },
                            minLines = 2,
                            isError = formError && formReason.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (formError) {
                            Text("Tanggal mulai dan alasan wajib diisi", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formStart.isBlank() || formReason.isBlank()) {
                                formError = true
                            } else {
                                vm.submit(formType, formStart, formEnd.ifBlank { formStart }, formReason)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                        else Text("Kirim", fontFamily = PlusJakartaSans)
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






















