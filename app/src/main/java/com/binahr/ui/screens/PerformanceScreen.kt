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
import com.binahr.ui.viewmodel.PerformanceViewModel

@Composable
fun PerformanceScreen(onBack: () -> Unit, vm: PerformanceViewModel = viewModel()) {
    val cycles by vm.cycles.collectAsStateWithLifecycle()
    val selectedDetail by vm.selectedDetail.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()

    LaunchedEffect(submitResult) {
        submitResult?.let { vm.clearSubmitResult() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Performa Karyawan", onBack = onBack)

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

        if (isLoading && cycles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (cycles.isEmpty()) {
            EmptyState(
                title = "Belum Ada Siklus",
                subtitle = "Belum ada siklus penilaian kinerja",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(cycles) { cycle ->
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cycle.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${cycle.startDate ?: ""} - ${cycle.endDate ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                                StatusBadge(
                                    text = cycle.status ?: "-",
                                    type = when (cycle.status?.lowercase()) {
                                        "active" -> BadgeType.SUCCESS
                                        "closed" -> BadgeType.NEUTRAL
                                        else -> BadgeType.WARNING
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { vm.loadDetail(cycle.id) },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text("Lihat Detail", style = MaterialTheme.typography.labelMedium, color = GreenPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail bottom sheet
    selectedDetail?.let { detail ->
        var reviewScore by remember { mutableStateOf("") }
        var reviewNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { vm.clearDetail() },
            title = { Text(detail.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Periode: ${detail.startDate} - ${detail.endDate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    if (detail.goals.isNotEmpty()) {
                        Text("Target Kinerja:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        detail.goals.forEach { goal ->
                            Row(Modifier.padding(start = 8.dp)) {
                                Icon(Icons.Filled.FiberManualRecord, null, modifier = Modifier.size(8.dp).padding(top = 5.dp), tint = GreenPrimary)
                                Spacer(Modifier.width(6.dp))
                                Text(goal.title, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    HorizontalDivider(color = DividerColor)
                    Text("Kirim Penilaian Diri:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = reviewScore,
                        onValueChange = { reviewScore = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Nilai (0-100)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = reviewNotes,
                        onValueChange = { reviewNotes = it },
                        label = { Text("Catatan Penilaian") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val score = reviewScore.toDoubleOrNull()
                        if (score != null) {
                            vm.submitReview(detail.id, score, reviewNotes.ifBlank { null })
                            vm.clearDetail()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Kirim") }
            },
            dismissButton = {
                TextButton(onClick = { vm.clearDetail() }) { Text("Tutup") }
            },
        )
    }
}






















