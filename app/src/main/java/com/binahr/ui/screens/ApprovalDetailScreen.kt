package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.ApprovalViewModel

@Composable
fun ApprovalDetailScreen(
    id: String,
    onBack: () -> Unit,
    vm: ApprovalViewModel = viewModel(),
) {
    val detail by vm.selectedDetail.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val actionResult by vm.actionResult.collectAsStateWithLifecycle()

    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(id) {
        vm.loadDetail(id)
    }

    LaunchedEffect(actionResult) {
        actionResult?.onSuccess {
            vm.clearActionResult()
            vm.clearDetail()
            onBack()
        }
        actionResult?.onFailure {
            vm.clearActionResult()
        }
    }

    DisposableEffect(Unit) {
        onDispose { vm.clearDetail() }
    }

    Scaffold(
        topBar = {
            BinaTopBar(title = "Detail Persetujuan", onBack = onBack)
        },
    ) { innerPadding ->
        when {
            isLoading && detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    SkeletonDetailScreen()
                }
            }

            error != null && detail == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                ) {
                    InfoCallout(message = error ?: "Terjadi kesalahan", type = CalloutType.ERROR)
                }
            }

            detail != null -> {
                val d = detail!!
                val isPending = d.status.equals("pending", ignoreCase = true) ||
                    d.status.equals("pending_approval", ignoreCase = true)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header card
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = d.title ?: d.approvableType,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (!d.requesterName.isNullOrBlank()) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Pengaju: ${d.requesterName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                        )
                                    }
                                }
                                StatusBadge(
                                    text = d.status,
                                    type = when (d.status.lowercase()) {
                                        "approved" -> BadgeType.SUCCESS
                                        "rejected" -> BadgeType.ERROR
                                        else -> BadgeType.WARNING
                                    },
                                )
                            }

                            if (!d.createdAt.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Diajukan: ${d.createdAt.take(10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                )
                            }

                            if (!d.notes.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Catatan:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                )
                                Text(
                                    text = d.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }

                    // Approval steps
                    if (d.steps.isNotEmpty()) {
                        SectionHeader(title = "Alur Persetujuan")
                        d.steps.forEach { step ->
                            HRCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(
                                        imageVector = when (step.status.lowercase()) {
                                            "approved" -> Icons.Filled.CheckCircle
                                            "rejected" -> Icons.Filled.Cancel
                                            else -> Icons.Outlined.HourglassEmpty
                                        },
                                        contentDescription = null,
                                        tint = when (step.status.lowercase()) {
                                            "approved" -> GreenPrimary
                                            "rejected" -> MaterialTheme.colorScheme.error
                                            else -> TextTertiary
                                        },
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Step ${step.step}: ${step.approverName ?: "Approver"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        Text(
                                            text = step.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                        )
                                        if (!step.notes.isNullOrBlank()) {
                                            Text(
                                                text = step.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                            )
                                        }
                                        if (!step.actedAt.isNullOrBlank()) {
                                            Text(
                                                text = step.actedAt.take(10),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextTertiary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons for pending items
                    if (isPending) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = { notes = ""; showRejectDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Tolak")
                            }
                            Button(
                                onClick = { notes = ""; showApproveDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Setujui")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // Approve Dialog
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Konfirmasi Persetujuan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Setujui pengajuan ini?")
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan (opsional)") },
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
                    onClick = {
                        showApproveDialog = false
                        vm.approve(id, notes.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                ) {
                    Text("Setujui")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }

    // Reject Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Konfirmasi Penolakan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tolak pengajuan ini?")
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Alasan penolakan (opsional)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRejectDialog = false
                        vm.reject(id, notes.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Tolak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }
}
