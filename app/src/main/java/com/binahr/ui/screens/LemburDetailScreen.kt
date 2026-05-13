package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
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
import com.binahr.ui.viewmodel.LemburViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LemburDetailScreen(
    id: String,
    onBack: () -> Unit,
    vm: LemburViewModel = viewModel(),
) {
    val requests by vm.requests.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    val item = requests.firstOrNull { it.id == id }

    fun formatApiDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        return try {
            val dateStr = iso.take(10)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(dateStr)?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(it)
            } ?: iso
        } catch (e: Exception) { iso }
    }

    Scaffold(
        topBar = { BinaTopBar(title = "Detail Lembur", onBack = onBack) },
    ) { innerPadding ->
        when {
            isLoading && item == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { SkeletonDetailScreen() }
            }

            item == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Data tidak ditemukan", color = TextSecondary)
                }
            }

            else -> {
                val isPending = item.approvalState.equals("pending", ignoreCase = true) ||
                    item.approvalState.equals("submitted", ignoreCase = true)

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
                                Text(
                                    text = "Lembur",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                StatusBadge(
                                    text = item.approvalState,
                                    type = when (item.approvalState.lowercase()) {
                                        "approved" -> BadgeType.SUCCESS
                                        "rejected" -> BadgeType.ERROR
                                        else -> BadgeType.WARNING
                                    },
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Diajukan: ${formatApiDate(item.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                            )
                        }
                    }

                    // Detail info
                    HRCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            DetailRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = "Tanggal Lembur",
                                value = formatApiDate(item.overtimeDate),
                            )
                            HorizontalDivider()
                            DetailRow(
                                icon = Icons.Filled.Schedule,
                                label = "Jam Mulai",
                                value = item.startTime ?: "-",
                            )
                            HorizontalDivider()
                            DetailRow(
                                icon = Icons.Filled.Schedule,
                                label = "Jam Selesai",
                                value = item.endTime ?: "-",
                            )
                            HorizontalDivider()
                            DetailRow(
                                icon = Icons.Filled.AccessTime,
                                label = "Total Jam",
                                value = "%.1f jam".format(item.hours),
                            )
                            if (!item.reason.isNullOrBlank()) {
                                HorizontalDivider()
                                DetailRow(
                                    icon = Icons.Filled.Info,
                                    label = "Alasan",
                                    value = item.reason,
                                )
                            }
                            if (!item.approvedAt.isNullOrBlank()) {
                                HorizontalDivider()
                                DetailRow(
                                    icon = Icons.Filled.HourglassEmpty,
                                    label = "Disetujui pada",
                                    value = formatApiDate(item.approvedAt),
                                )
                            }
                        }
                    }

                    // Cancel button for pending
                    if (isPending) {
                        OutlinedButton(
                            onClick = {
                                vm.cancel(item.id)
                                onBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Batalkan Pengajuan")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = GreenPrimary)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
