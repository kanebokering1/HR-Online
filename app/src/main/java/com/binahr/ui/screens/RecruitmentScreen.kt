package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
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
import com.binahr.ui.viewmodel.RecruitmentViewModel

@Composable
fun RecruitmentScreen(
    onBack: () -> Unit,
    vm: RecruitmentViewModel = viewModel(),
) {
    val postings by vm.postings.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        BinaTopBar(title = "Lowongan Kerja", onBack = onBack)

        error?.let { msg ->
            InfoCallout(
                message = msg,
                type = CalloutType.ERROR,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (isLoading && postings.isEmpty()) {
            SkeletonListScreen()
        } else if (postings.isEmpty()) {
            EmptyState(
                title = "Belum Ada Lowongan",
                subtitle = "Tidak ada lowongan kerja aktif saat ini",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(postings) { posting ->
                    HRCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        posting.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (!posting.departmentName.isNullOrBlank()) {
                                        Text(
                                            posting.departmentName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                        )
                                    }
                                }
                                StatusBadge(
                                    text = posting.status,
                                    type = when (posting.status.lowercase()) {
                                        "open" -> BadgeType.SUCCESS
                                        "closed" -> BadgeType.NEUTRAL
                                        else -> BadgeType.WARNING
                                    },
                                )
                            }
                            if (!posting.location.isNullOrBlank() || !posting.closesAt.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (!posting.location.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.LocationOn,
                                                null,
                                                modifier = Modifier.size(14.dp),
                                                tint = TextSecondary,
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                posting.location,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                            )
                                        }
                                    }
                                    if (!posting.closesAt.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.CalendarMonth,
                                                null,
                                                modifier = Modifier.size(14.dp),
                                                tint = TextSecondary,
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Tutup: ${posting.closesAt}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
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
    }
}
