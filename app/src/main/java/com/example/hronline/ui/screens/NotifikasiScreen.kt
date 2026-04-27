package com.example.hronline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.hronline.ui.components.GradientTopBar
import com.example.hronline.ui.theme.*

data class NotifItem(
    val id: Int,
    val title: String,
    val body: String,
    val time: String,
    val type: String,
    val isRead: Boolean,
)

@Composable
fun NotifikasiScreen(onBack: () -> Unit) {
    val items = remember {
        mutableStateListOf(
            NotifItem(1, "Cuti Disetujui", "Pengajuan cuti Anda tanggal 15-17 Mar 2026 telah disetujui oleh atasan.", "2 jam lalu", "cuti", false),
            NotifItem(2, "Slip Gaji Tersedia", "Slip gaji bulan Maret 2026 sudah tersedia. Silakan cek di menu Slip Gaji.", "5 jam lalu", "gaji", false),
            NotifItem(3, "Pengumuman Baru", "Ada pengumuman baru mengenai perubahan kebijakan WFH.", "1 hari lalu", "pengumuman", true),
            NotifItem(4, "Lembur Ditolak", "Pengajuan lembur tanggal 10 Mar 2026 ditolak. Alasan: tidak ada urgensi.", "2 hari lalu", "lembur", true),
            NotifItem(5, "Reminder Absensi", "Jangan lupa check out hari ini! Anda belum melakukan check out.", "3 hari lalu", "absensi", true),
            NotifItem(6, "Training Reminder", "Training \"Leadership 101\" akan dimulai besok pukul 09:00 WIB.", "4 hari lalu", "training", true),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Notifikasi", onBack = onBack)

        // Unread count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${items.count { !it.isRead }} belum dibaca",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            TextButton(onClick = {
                val updated = items.map { it.copy(isRead = true) }
                items.clear()
                items.addAll(updated)
            }) {
                Text("Tandai Semua Dibaca", color = GreenPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(items) { item ->
                val bgColor = if (!item.isRead) SurfaceGreenTint else Color.Transparent
                val iconColor = when (item.type) {
                    "cuti" -> AccentPurple
                    "gaji" -> GreenPrimary
                    "pengumuman" -> AccentBlue
                    "lembur" -> AccentOrange
                    "absensi" -> AccentRed
                    "training" -> AccentCyan
                    else -> TextTertiary
                }
                val iconBgColor = when (item.type) {
                    "cuti" -> AccentPurpleLight
                    "gaji" -> Green50
                    "pengumuman" -> AccentBlueLight
                    "lembur" -> AccentOrangeLight
                    "absensi" -> AccentRedLight
                    "training" -> AccentCyanLight
                    else -> SurfaceLight
                }
                val icon = when (item.type) {
                    "cuti" -> Icons.Filled.BeachAccess
                    "gaji" -> Icons.Filled.AccountBalanceWallet
                    "pengumuman" -> Icons.Filled.Campaign
                    "lembur" -> Icons.Filled.MoreTime
                    "absensi" -> Icons.Filled.Fingerprint
                    "training" -> Icons.Filled.School
                    else -> Icons.Filled.Notifications
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable {
                            val idx = items.indexOf(item)
                            if (idx >= 0) items[idx] = item.copy(isRead = true)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconBgColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(item.time, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(GreenPrimary, CircleShape)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
                if (items.indexOf(item) < items.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = DividerColor)
                }
            }
        }
    }
}
