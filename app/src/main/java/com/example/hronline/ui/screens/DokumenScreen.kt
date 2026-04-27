package com.example.hronline.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

data class DokumenItem(
    val id: Int,
    val name: String,
    val type: String,
    val size: String,
    val date: String,
    val category: String,
)

@Composable
fun DokumenScreen(onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = listOf("Semua", "Kontrak", "Sertifikat", "SK", "Lainnya")

    val allDocs = remember {
        listOf(
            DokumenItem(1, "Kontrak Kerja 2024", "PDF", "1.2 MB", "01 Jan 2024", "Kontrak"),
            DokumenItem(2, "Sertifikat Training AWS", "PDF", "850 KB", "15 Feb 2026", "Sertifikat"),
            DokumenItem(3, "SK Pengangkatan", "PDF", "500 KB", "01 Jan 2024", "SK"),
            DokumenItem(4, "Sertifikat Training Kotlin", "PDF", "720 KB", "10 Dec 2025", "Sertifikat"),
            DokumenItem(5, "Addendum Kontrak 2025", "PDF", "980 KB", "01 Jan 2025", "Kontrak"),
            DokumenItem(6, "SK Kenaikan Gaji", "PDF", "450 KB", "01 Jul 2025", "SK"),
            DokumenItem(7, "BPJS Ketenagakerjaan", "PDF", "350 KB", "01 Jan 2024", "Lainnya"),
            DokumenItem(8, "NPWP", "PDF", "200 KB", "01 Jan 2024", "Lainnya"),
        )
    }

    val filteredDocs = if (selectedCategory == "Semua") allDocs else allDocs.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Dokumen Saya", onBack = onBack)

        // Category filter
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = cat == selectedCategory,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontFamily = PlusJakartaSans) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        // Count
        Text(
            "${filteredDocs.size} dokumen",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredDocs) { doc ->
                val (icon, iconColor, iconBg) = when (doc.category) {
                    "Kontrak" -> Triple(Icons.Filled.Handshake, AccentBlue, AccentBlueLight)
                    "Sertifikat" -> Triple(Icons.Filled.WorkspacePremium, AccentAmber, AccentAmberLight)
                    "SK" -> Triple(Icons.Filled.Gavel, AccentPurple, AccentPurpleLight)
                    else -> Triple(Icons.Filled.Description, TextTertiary, SurfaceLight)
                }

                HRCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(iconBg, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("${doc.type} • ${doc.size} • ${doc.date}", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.Download, contentDescription = "Unduh", tint = GreenPrimary)
                        }
                    }
                }
            }
        }
    }
}
