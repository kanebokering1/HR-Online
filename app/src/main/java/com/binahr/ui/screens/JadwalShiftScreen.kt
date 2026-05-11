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
import com.binahr.ui.viewmodel.KalenderViewModel

@Composable
fun JadwalShiftScreen(onBack: () -> Unit, vm: KalenderViewModel = viewModel()) {
    val shiftAssignments by vm.shiftAssignments.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Jadwal Shift", onBack = onBack)

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

        if (isLoading && shiftAssignments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else if (shiftAssignments.isEmpty()) {
            EmptyState(
                title = "Belum Ada Jadwal",
                subtitle = "Tidak ada jadwal shift untuk periode ini",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(shiftAssignments.sortedBy { it.date }) { assignment ->
                    HRCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(assignment.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(assignment.shiftName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        "${assignment.startTime} - ${assignment.endTime}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}






















