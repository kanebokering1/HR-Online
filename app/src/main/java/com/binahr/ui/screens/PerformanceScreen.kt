package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun PerformanceScreen(
    onBack: () -> Unit,
    onNavigateDetail: (String) -> Unit = {},
    vm: PerformanceViewModel = viewModel(),
) {
    val cycles by vm.cycles.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        BinaTopBar(title = "Performa Karyawan", onBack = onBack)

        error?.let { msg ->
            InfoCallout(message = msg, type = CalloutType.ERROR, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        if (isLoading && cycles.isEmpty()) {
            SkeletonListScreen()
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
                                onClick = { onNavigateDetail(cycle.id) },
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

}






















