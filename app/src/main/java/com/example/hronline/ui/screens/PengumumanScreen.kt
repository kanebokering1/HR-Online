package com.example.hronline.ui.screens

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
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

data class PengumumanItem(
    val id: Int,
    val title: String,
    val date: String,
    val category: String,
    val preview: String,
    val isImportant: Boolean,
)

@Composable
fun PengumumanScreen(onBack: () -> Unit) {
    val items = remember {
        listOf(
            PengumumanItem(1, "Libur Hari Raya Idul Fitri 2026", "10 Apr 2026", "Libur", "Diberitahukan kepada seluruh karyawan bahwa kantor akan libur pada tanggal 28 Maret - 7 April 2026 dalam rangka Hari Raya Idul Fitri 1447H.", true),
            PengumumanItem(2, "Gathering Tahunan Perusahaan", "05 Apr 2026", "Event", "Acara gathering tahun ini akan diadakan di Bali pada tanggal 20-22 Mei 2026. Seluruh karyawan wajib hadir.", true),
            PengumumanItem(3, "Update Kebijakan Work From Home", "01 Apr 2026", "Kebijakan", "Mulai bulan Mei 2026, kebijakan WFH diperbarui menjadi maksimal 2 hari per minggu dengan persetujuan atasan.", false),
            PengumumanItem(4, "Jadwal Training Q2 2026", "28 Mar 2026", "Training", "Berikut adalah jadwal training untuk Q2 2026. Pastikan Anda mendaftar sesuai departemen masing-masing.", false),
            PengumumanItem(5, "Perubahan Jam Kerja Ramadhan", "20 Mar 2026", "Kebijakan", "Selama bulan Ramadhan, jam kerja diubah menjadi 08:00 - 15:30 WIB. Berlaku mulai 1 Maret 2026.", true),
            PengumumanItem(6, "Employee of the Month - Februari", "01 Mar 2026", "Prestasi", "Selamat kepada Siti Nurhaliza dari departemen Marketing sebagai Employee of the Month Februari 2026.", false),
        )
    }

    var expandedId by remember { mutableIntStateOf(-1) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Pengumuman", onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items) { item ->
                HRCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatusBadge(
                                        text = item.category,
                                        type = when (item.category) {
                                            "Libur" -> BadgeType.ERROR
                                            "Event" -> BadgeType.INFO
                                            "Kebijakan" -> BadgeType.WARNING
                                            "Training" -> BadgeType.NEUTRAL
                                            "Prestasi" -> BadgeType.SUCCESS
                                            else -> BadgeType.NEUTRAL
                                        },
                                    )
                                    if (item.isImportant) {
                                        StatusBadge(text = "Penting", type = BadgeType.ERROR)
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
                            Text(item.date, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = if (expandedId == item.id) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = { expandedId = if (expandedId == item.id) -1 else item.id },
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
