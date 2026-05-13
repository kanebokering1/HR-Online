package com.binahr.ui.screens


import com.binahr.BuildConfig
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
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.ApprovalViewModel
import com.binahr.util.DateTimeUtils

@Composable
fun ApprovalsScreen(onBack: () -> Unit, onNavigateDetail: (String) -> Unit = {}, vm: ApprovalViewModel = viewModel()) {
    val approvals by vm.approvals.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val actionResult by vm.actionResult.collectAsStateWithLifecycle()
    var activeFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionResult) {
        actionResult?.let {
            vm.clearActionResult()
            vm.load(activeFilter)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BinaTopBar(title = "Persetujuan", onBack = onBack)

        // Status filter chips
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(null to "Semua", "pending" to "Pending", "approved" to "Disetujui", "rejected" to "Ditolak").forEach { (value, label) ->
                FilterChip(
                    selected = activeFilter == value,
                    onClick = {
                        activeFilter = value
                        vm.load(value)
                    },
                    label = { Text(label, fontFamily = PlusJakartaSans) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                    ),
                )
            }
        }

        error?.let { msg ->
            InfoCallout(message = msg, type = CalloutType.ERROR, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        if (isLoading && approvals.isEmpty()) {
            SkeletonListScreen()
        } else if (approvals.isEmpty()) {
            EmptyState(
                title = "Tidak Ada Data",
                subtitle = "Tidak ada item yang menunggu persetujuan",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(approvals) { item ->
                    var showNotesDialog by remember { mutableStateOf<String?>(null) } // "approve" or "reject"
                    var notes by remember { mutableStateOf("") }

                    HRCard(onClick = { onNavigateDetail(item.id) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.title ?: readableApprovalType(item.approvableType),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(item.requesterName ?: "-", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(DateTimeUtils.toDateTime(item.createdAt), style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
                                StatusBadge(
                                    text = item.status,
                                    type = when (item.status.lowercase()) {
                                        "approved" -> BadgeType.SUCCESS
                                        "rejected" -> BadgeType.ERROR
                                        else -> BadgeType.WARNING
                                    },
                                )
                            }

                            if (item.status.equals("pending", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { showNotesDialog = "reject" },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                                        shape = RoundedCornerShape(8.dp),
                                    ) { Text("Tolak", fontFamily = PlusJakartaSans) }
                                    Button(
                                        onClick = { showNotesDialog = "approve" },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                    ) { Text("Setujui", fontFamily = PlusJakartaSans) }
                                }
                            }

                            if (showNotesDialog != null) {
                                AlertDialog(
                                    onDismissRequest = { showNotesDialog = null; notes = "" },
                                    title = { Text(if (showNotesDialog == "approve") "Setujui Permintaan" else "Tolak Permintaan") },
                                    text = {
                                        OutlinedTextField(
                                            value = notes,
                                            onValueChange = { notes = it },
                                            label = { Text("Catatan (opsional)") },
                                            minLines = 2,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                if (showNotesDialog == "approve") vm.approve(item.id, notes.ifBlank { null })
                                                else vm.reject(item.id, notes.ifBlank { null })
                                                showNotesDialog = null; notes = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (showNotesDialog == "approve") GreenPrimary else AccentRed,
                                            ),
                                        ) { Text(if (showNotesDialog == "approve") "Setujui" else "Tolak") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showNotesDialog = null; notes = "" }) { Text("Batal") }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun readableApprovalType(type: String?): String {
    if (type.isNullOrBlank()) return "Permintaan"
    return when {
        type.contains("LeaveApplication", ignoreCase = true) -> "Pengajuan Cuti"
        type.contains("OvertimeRequest", ignoreCase = true) -> "Pengajuan Lembur"
        type.contains("Reimbursement", ignoreCase = true) -> "Reimbursement"
        type.contains("Payroll", ignoreCase = true) -> "Penggajian"
        type.contains("Loan", ignoreCase = true) -> "Pinjaman"
        else -> type.substringAfterLast("\\").substringAfterLast("/")
            .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
            .trim()
            .ifBlank { "Permintaan" }
    }
}




















