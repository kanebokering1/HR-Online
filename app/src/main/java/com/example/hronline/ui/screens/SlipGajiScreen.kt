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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

data class SlipGajiItem(
    val month: String,
    val year: Int,
    val gajiPokok: Long,
    val tunjangan: Long,
    val potongan: Long,
    val total: Long,
    val status: String,
)

@Composable
fun SlipGajiScreen(onBack: () -> Unit) {
    var selectedYear by remember { mutableIntStateOf(2026) }
    val years = listOf(2024, 2025, 2026)

    val slipList = remember(selectedYear) {
        val months = if (selectedYear == 2026) listOf("Januari", "Februari", "Maret") else
            listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        months.mapIndexed { i, m ->
            SlipGajiItem(
                month = m,
                year = selectedYear,
                gajiPokok = 8_500_000,
                tunjangan = 2_500_000 + (i * 100_000L),
                potongan = 850_000 + (i * 50_000L),
                total = 10_150_000 + (i * 50_000L),
                status = "Dibayar",
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Slip Gaji", onBack = onBack)

        // Year chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(years) { year ->
                FilterChip(
                    selected = year == selectedYear,
                    onClick = { selectedYear = year },
                    label = { Text("$year", fontFamily = PlusJakartaSans) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        // Total Summary
        HRCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            gradientBorder = Brush.horizontalGradient(listOf(GreenPrimary, TealAccent)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Penghasilan $selectedYear", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp ${String.format("%,d", slipList.sumOf { it.total })}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slip List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(slipList.reversed()) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(item.month, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${item.year}", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            StatusBadge(text = item.status, type = BadgeType.SUCCESS)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        GajiRow("Gaji Pokok", item.gajiPokok, GreenPrimary)
                        GajiRow("Tunjangan", item.tunjangan, AccentBlue)
                        GajiRow("Potongan", -item.potongan, AccentRed)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Total", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Rp ${String.format("%,d", item.total)}",
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GajiRow(label: String, amount: Long, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = "${if (amount < 0) "-" else ""}Rp ${String.format("%,d", kotlin.math.abs(amount))}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}
