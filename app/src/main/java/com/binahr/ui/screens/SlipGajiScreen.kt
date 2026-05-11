package com.binahr.ui.screens


import com.binahr.BuildConfig
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
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.SlipGajiViewModel
import com.binahr.util.toRupiah
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SlipGajiScreen(onBack: () -> Unit, vm: SlipGajiViewModel = viewModel()) {
    var selectedYear by remember { mutableIntStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) }

    val allSlips by vm.slips.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val years = allSlips.mapNotNull {
        it.periodYear
            ?: it.periodLabel?.substringAfterLast('/')?.trimEnd()?.toIntOrNull()
    }.distinct().sorted().ifEmpty { listOf(selectedYear - 1, selectedYear) }

    val slipList = allSlips.filter { slip ->
        val y = slip.periodYear
            ?: slip.periodLabel?.substringAfterLast('/')?.trimEnd()?.toIntOrNull()
        y == selectedYear
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
                    text = slipList.sumOf { it.netSalary.toLong() }.toRupiah(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slip List
        if (isLoading && allSlips.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (allSlips.isEmpty() && error != null) {
            EmptyState(
                title = "Gagal Memuat Slip",
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
            items(slipList.reversed()) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(item.periodLabel ?: "-", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                            StatusBadge(text = "Dibayar", type = BadgeType.SUCCESS)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        GajiRow("Gaji Bruto", item.grossSalary.toLong(), GreenPrimary)
                        GajiRow("PPh21", -item.pph21TerAmount.toLong(), AccentRed)
                        GajiRow("BPJS (Karyawan)", -item.bpjsEmployeeAmount.toLong(), AccentOrange)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Take Home Pay", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                item.netSalary.toLong().toRupiah(),
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
        }  // end else (slips loaded)
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
            text = "${if (amount < 0) "-" else ""}${kotlin.math.abs(amount).toRupiah()}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}






















