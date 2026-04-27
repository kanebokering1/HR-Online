package com.example.hronline.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hronline.data.*
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    val records = remember(selectedMonth, selectedYear) {
        AttendanceStorage.getAttendanceByMonth(context, selectedMonth, selectedYear)
    }
    val grouped = remember(records) { AttendanceStorage.groupRecordsByDate(records) }

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

    // Summary
    val totalDays = grouped.size
    val lateDays = grouped.count { (_, pair) ->
        val checkIn = pair.first
        if (checkIn != null) {
            try {
                val h = checkIn.time.substringBefore(":").trim().toInt()
                h >= 8 && checkIn.time.substringAfter(":").substringBefore(" ").trim().toInt() > 0
            } catch (_: Exception) { false }
        } else false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Riwayat Absensi", onBack = onBack.takeIf { true })

        // Month chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(12) { index ->
                val month = index + 1
                val isSelected = month == selectedMonth
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedMonth = month },
                    label = { Text(monthNames[index], fontFamily = PlusJakartaSans, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        // Summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(Icons.Filled.CheckCircle, "$totalDays", "Hadir", GreenPrimary, Green50, Modifier.weight(1f))
            StatCard(Icons.Filled.Schedule, "$lateDays", "Telat", AccentOrange, AccentOrangeLight, Modifier.weight(1f))
            StatCard(Icons.Filled.EventBusy, "0", "Izin", AccentBlue, AccentBlueLight, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Attendance List
        if (grouped.isEmpty()) {
            EmptyState(
                title = "Belum Ada Data",
                subtitle = "Tidak ada data absensi untuk bulan ini",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val sortedDates = grouped.keys.sortedByDescending { dateStr ->
                    try {
                        SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).parse(dateStr)?.time ?: 0
                    } catch (_: Exception) { 0L }
                }
                items(sortedDates) { dateStr ->
                    val (checkIn, checkOut) = grouped[dateStr] ?: Pair(null, null)
                    HistoryItem(dateStr = dateStr, checkIn = checkIn, checkOut = checkOut)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    dateStr: String,
    checkIn: AttendanceRecord?,
    checkOut: AttendanceRecord?,
) {
    var expanded by remember { mutableStateOf(false) }

    // Check if weekend
    val isWeekend = remember(dateStr) {
        try {
            val date = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).parse(dateStr)
            val cal = Calendar.getInstance().apply { time = date!! }
            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        } catch (_: Exception) { false }
    }

    // Format short date
    val shortDate = remember(dateStr) {
        try {
            val date = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).parse(dateStr)
            SimpleDateFormat("dd MMM", Locale.forLanguageTag("id-ID")).format(date!!)
        } catch (_: Exception) { dateStr }
    }

    val dayName = remember(dateStr) {
        try {
            val date = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).parse(dateStr)
            SimpleDateFormat("EEEE", Locale.forLanguageTag("id-ID")).format(date!!)
        } catch (_: Exception) { "" }
    }

    HRCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = shortDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWeekend) AccentRed else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWeekend) AccentRed.copy(alpha = 0.7f) else TextTertiary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Masuk", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Text(
                            text = checkIn?.time ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (checkIn != null) GreenPrimary else TextTertiary,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Keluar", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Text(
                            text = checkOut?.time ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (checkOut != null) AccentRed else TextTertiary,
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = TextTertiary,
                )
            }

            // Expandable detail
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (checkIn != null) {
                        Text("Check In", style = MaterialTheme.typography.labelSmall, color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Lokasi: ${checkIn.location}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            "Wajah: ${if (checkIn.faceVerified) "Terverifikasi ✓" else "Tidak"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (checkOut != null) {
                        Text("Check Out", style = MaterialTheme.typography.labelSmall, color = AccentRed, fontWeight = FontWeight.SemiBold)
                        Text("Lokasi: ${checkOut.location}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            "Wajah: ${if (checkOut.faceVerified) "Terverifikasi ✓" else "Tidak"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }
    }
}
