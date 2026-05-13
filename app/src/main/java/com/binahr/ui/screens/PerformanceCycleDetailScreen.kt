package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.PerformanceViewModel

@Composable
fun PerformanceCycleDetailScreen(
    id: String,
    onBack: () -> Unit,
    vm: PerformanceViewModel = viewModel(),
) {
    val detail by vm.selectedDetail.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var reviewScore by remember { mutableStateOf("") }
    var reviewNotes by remember { mutableStateOf("") }

    LaunchedEffect(id) { vm.loadDetail(id) }

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            vm.clearSubmitResult()
            reviewScore = ""
            reviewNotes = ""
        }
        submitResult?.onFailure { vm.clearSubmitResult() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BinaTopBar(title = "Detail Siklus", onBack = onBack)

        if (isLoading && detail == null) {
            SkeletonDetailScreen()
        } else if (detail == null) {
            EmptyState(
                title = "Data Tidak Ditemukan",
                subtitle = "Siklus penilaian tidak tersedia",
                modifier = Modifier.weight(1f),
            )
        } else {
            val d = detail!!
            val isActive = d.status.lowercase() == "active"
            val hasReview = d.myReview != null

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                error?.let {
                    InfoCallout(message = it, type = CalloutType.ERROR)
                }

                // Header
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            d.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${d.startDate} – ${d.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Spacer(Modifier.height(8.dp))
                        StatusBadge(
                            text = d.status,
                            type = when (d.status.lowercase()) {
                                "active" -> BadgeType.SUCCESS
                                "closed" -> BadgeType.NEUTRAL
                                else -> BadgeType.WARNING
                            },
                        )
                    }
                }

                // Goals
                if (d.goals.isNotEmpty()) {
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Target Kinerja",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(8.dp))
                            d.goals.forEach { goal ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Icon(
                                        Icons.Filled.FiberManualRecord,
                                        null,
                                        modifier = Modifier
                                            .size(8.dp)
                                            .padding(top = 5.dp),
                                        tint = GreenPrimary,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            goal.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                        )
                                        if (!goal.description.isNullOrBlank()) {
                                            Text(
                                                goal.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                            )
                                        }
                                        Text(
                                            "Bobot: ${goal.weight}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Existing review
                if (hasReview) {
                    val rev = d.myReview!!
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Penilaian Diri Saya",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(8.dp))
                            PerfDetailRow(Icons.Filled.Star, "Nilai", "${rev.score ?: "-"} / 100")
                            if (!rev.notes.isNullOrBlank()) {
                                PerfDetailRow(Icons.Filled.Notes, "Catatan", rev.notes)
                            }
                            PerfDetailRow(Icons.Filled.AccessTime, "Dikirim", rev.submittedAt ?: "-")
                        }
                    }
                }

                // Submit form (only when active & no review yet)
                if (isActive && !hasReview) {
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Kirim Penilaian Diri",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = reviewScore,
                                onValueChange = {
                                    reviewScore = it.filter { c -> c.isDigit() || c == '.' }
                                },
                                label = { Text("Nilai (0–100)") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = reviewNotes,
                                onValueChange = { reviewNotes = it },
                                label = { Text("Catatan Penilaian") },
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(12.dp))
                            HRButton(
                                label = "Kirim Penilaian",
                                isLoading = isLoading,
                                enabled = reviewScore.toDoubleOrNull() != null,
                                onClick = {
                                    val score = reviewScore.toDoubleOrNull()
                                    if (score != null) {
                                        vm.submitReview(d.id, score, reviewNotes.ifBlank { null })
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerfDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
