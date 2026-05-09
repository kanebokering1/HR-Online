package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.PengumumanViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PengumumanScreen(onBack: () -> Unit, vm: PengumumanViewModel = viewModel()) {
    val announcements by vm.announcements.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    var expandedId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Pengumuman", onBack = onBack)

        if (isLoading && announcements.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (announcements.isEmpty()) {
            EmptyState(title = "Belum Ada Pengumuman", subtitle = "Tidak ada pengumuman saat ini", modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(announcements) { item ->
                    HRCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        item.category?.let { cat ->
                                            StatusBadge(
                                                text = cat,
                                                type = when (cat) {
                                                    "Libur" -> BadgeType.ERROR
                                                    "Event" -> BadgeType.INFO
                                                    "Kebijakan" -> BadgeType.WARNING
                                                    "Training" -> BadgeType.NEUTRAL
                                                    "Prestasi" -> BadgeType.SUCCESS
                                                    else -> BadgeType.NEUTRAL
                                                },
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(item.publishedAt ?: "", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = if (expandedId == item.id) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { expandedId = if (expandedId == item.id) null else item.id },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(
                                    text = if (expandedId == item.id) "Tutup" else "Baca Selengkapnya",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GreenPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}






















