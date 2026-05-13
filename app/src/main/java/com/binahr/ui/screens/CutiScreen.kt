package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.binahr.ui.viewmodel.CutiViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutiScreen(onBack: () -> Unit, onAjukanCuti: () -> Unit = {}, onNavigateDetail: (String) -> Unit = {}, vm: CutiViewModel = viewModel()) {

    // Format ISO-8601 API date string (e.g. "2026-05-12T00:00:00.000000Z") → "dd MMM yyyy"
    fun formatApiDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "-"
        return try {
            val dateStr = iso.take(10) // "yyyy-MM-dd"
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(dateStr)?.let {
                SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(it)
            } ?: iso
        } catch (e: Exception) { iso }
    }
    // Localized label for leave type key
    fun leaveTypeLabel(key: String?): String = when (key?.lowercase()) {
        "annual"    -> "Cuti Tahunan"
        "sick"      -> "Cuti Sakit"
        "maternity" -> "Cuti Melahirkan"
        "personal"  -> "Keperluan Penting"
        "unpaid"    -> "Cuti Tidak Berbayar"
        else        -> key?.replaceFirstChar { it.uppercase() } ?: "-"
    }

    val applications by vm.applications.collectAsStateWithLifecycle()
    val balances by vm.balances.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    // Cuti Tahunan balance
    val sisaCuti = balances.firstOrNull { it.leaveType.contains("Tahunan", ignoreCase = true) }?.remainingDays?.toInt() ?: 0
    val totalCuti = balances.firstOrNull { it.leaveType.contains("Tahunan", ignoreCase = true) }?.allocatedDays?.toInt() ?: 12

    Column(modifier = Modifier.fillMaxSize()) {
        BinaTopBar(title = "Pengajuan Cuti", onBack = onBack)

        // Error banner
        error?.let { msg ->
            InfoCallout(message = msg, type = CalloutType.ERROR, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
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
            onClick = { onAjukanCuti() },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Pengajuan")

        if (isLoading && applications.isEmpty()) {
            SkeletonListScreen()
        } else if (applications.isEmpty() && error != null) {
            EmptyState(
                title = "Gagal Memuat Data",
                subtitle = error ?: "Terjadi kesalahan. Coba lagi.",
                modifier = Modifier.weight(1f),
                action = {
                    HRButton(
                        text = "Coba Lagi",
                        onClick = { vm.clearError(); vm.load() },
                        modifier = Modifier.padding(top = 8.dp),
                    )
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(applications) { item ->
                    HRCard(onClick = { onNavigateDetail(item.id) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(leaveTypeLabel(item.leaveType), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
                                Text("${formatApiDate(item.startDate)} - ${formatApiDate(item.endDate)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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

    }
}
