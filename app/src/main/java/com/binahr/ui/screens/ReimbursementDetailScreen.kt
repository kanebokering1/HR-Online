package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
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
import com.binahr.ui.viewmodel.ReimbursementViewModel
import com.binahr.util.toRupiah
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReimbursementDetailScreen(
    id: String,
    onBack: () -> Unit,
    vm: ReimbursementViewModel = viewModel(),
) {
    val claims by vm.claims.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    val item = claims.firstOrNull { it.id == id }

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
        topBar = { BinaTopBar(title = "Detail Reimbursement", onBack = onBack) },
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = item.amount.toLong().toRupiah(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = AccentIndigo,
                                        fontWeight = FontWeight.SemiBold,
                                    )
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
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Diajukan: ${formatApiDate(item.submittedAt ?: item.createdAt)}",
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
                                icon = Icons.Filled.Payments,
                                label = "Jumlah",
                                value = item.amount.toLong().toRupiah(),
                            )
                            HorizontalDivider()
                            DetailRow(
                                icon = Icons.Filled.Category,
                                label = "Mata Uang",
                                value = item.currency,
                            )
                            if (!item.approvedAt.isNullOrBlank()) {
                                HorizontalDivider()
                                DetailRow(
                                    icon = Icons.Filled.HourglassEmpty,
                                    label = "Disetujui pada",
                                    value = formatApiDate(item.approvedAt),
                                )
                            }
                            if (!item.paidAt.isNullOrBlank()) {
                                HorizontalDivider()
                                DetailRow(
                                    icon = Icons.Filled.CalendarMonth,
                                    label = "Dibayarkan pada",
                                    value = formatApiDate(item.paidAt),
                                )
                            }
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
