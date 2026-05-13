package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.clickable
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
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.LemburViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemburScreen(onBack: () -> Unit, onAjukanLembur: () -> Unit = {}, onNavigateDetail: (String) -> Unit = {}, vm: LemburViewModel = viewModel()) {
    val requests by vm.requests.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val approvedCount = requests.count { it.approvalState.equals("approved", ignoreCase = true) }
    val pendingCount = requests.count { it.approvalState.equals("pending", ignoreCase = true) || it.approvalState.equals("submitted", ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        BinaTopBar(title = "Lembur", onBack = onBack)

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
            onClick = { onAjukanLembur() },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Lembur")

        if (isLoading && requests.isEmpty()) {
            SkeletonListScreen()
        } else if (requests.isEmpty() && error != null) {
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(requests) { item ->
                HRCard(onClick = { onNavigateDetail(item.id) }) {
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
        }  // end else (requests not empty / no error)

    }
}
