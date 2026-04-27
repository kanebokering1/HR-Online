package com.example.hronline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

@Composable
fun DataDiriScreen(onBack: () -> Unit) {
    val sections = listOf(
        "Informasi Pribadi" to listOf(
            "Nama Lengkap" to "Aries Adityanto",
            "NIK Karyawan" to "2024001",
            "Tempat, Tgl Lahir" to "Jakarta, 15 Maret 1995",
            "Jenis Kelamin" to "Laki-laki",
            "Agama" to "Islam",
            "Status Pernikahan" to "Belum Menikah",
            "Golongan Darah" to "O",
        ),
        "Informasi Kontak" to listOf(
            "Email Pribadi" to "aries.a@gmail.com",
            "Email Kantor" to "aries.adityanto@xyz.co.id",
            "No. HP" to "+62 812-3456-7890",
            "Alamat" to "Jl. Merdeka No. 123, Jakarta Selatan",
        ),
        "Informasi Pekerjaan" to listOf(
            "Jabatan" to "Staff IT",
            "Departemen" to "IT & Development",
            "Tgl Bergabung" to "1 Januari 2024",
            "Status" to "Karyawan Tetap",
            "Level" to "Junior",
            "Atasan Langsung" to "Budi Santoso",
        ),
        "Informasi Pendidikan" to listOf(
            "Pendidikan Terakhir" to "S1 Teknik Informatika",
            "Universitas" to "Universitas Indonesia",
            "Tahun Lulus" to "2020",
            "IPK" to "3.67",
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Data Diri", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Profile header
            HRCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarImage(initials = "AA", size = 64.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Aries Adityanto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Staff IT", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        StatusBadge(text = "Aktif", type = BadgeType.SUCCESS)
                    }
                }
            }

            sections.forEach { (title, items) ->
                SectionHeader(title = title)
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        items.forEachIndexed { index, (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextTertiary, modifier = Modifier.weight(1f))
                                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
                            }
                            if (index < items.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
