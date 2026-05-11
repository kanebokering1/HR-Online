package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.binahr.ui.components.GradientTopBar
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.KalenderViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*

@Composable
fun KalenderScreen(onBack: () -> Unit, vm: KalenderViewModel = viewModel()) {
    val holidays by vm.holidays.collectAsStateWithLifecycle()
    val calendar = remember { Calendar.getInstance() }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val today = remember { calendar.get(Calendar.DAY_OF_MONTH) }
    val todayMonth = remember { calendar.get(Calendar.MONTH) }
    val todayYear = remember { calendar.get(Calendar.YEAR) }

    val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val dayHeaders = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    // Build calendar grid
    val calGrid = remember(month, year) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val grid = mutableListOf<Int?>()
        repeat(firstDayOfWeek) { grid.add(null) }
        for (d in 1..daysInMonth) grid.add(d)
        while (grid.size % 7 != 0) grid.add(null)
        grid
    }

    // API holidays mapped by day-of-month for current month
    val holidayEvents = remember(holidays, month, year) {
        val prefix = String.format("%04d-%02d", year, month + 1)
        holidays.filter { it.date.startsWith(prefix) }
            .associate { h ->
                h.date.substringAfterLast("-").toIntOrNull() to h.name
            }.filterKeys { it != null }.mapKeys { it.key!! }
    }
    // fallback static events if no API data
    val events = holidayEvents.ifEmpty {
        mapOf(
            10 to "Libur Nasional",
            25 to "Gajian",
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Kalender", onBack = onBack)

        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    if (month == 0) { month = 11; year-- } else month--
                }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Sebelumnya") }
                Text(
                    "${monthNames[month]} $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = {
                    if (month == 11) { month = 0; year++ } else month++
                }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Berikutnya") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day headers
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (day == "Min" || day == "Sab") AccentRed else TextTertiary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid — static (no Lazy) to avoid nested scroll crash
            Column {
                calGrid.chunked(7).forEachIndexed { rowIndex, week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEachIndexed { colIndex, day ->
                            val isToday = day == today && month == todayMonth && year == todayYear
                            val isSunday = colIndex == 0
                            val isSaturday = colIndex == 6
                            val hasEvent = events.containsKey(day)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (day != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                when {
                                                    isToday -> GreenPrimary
                                                    else -> Color.Transparent
                                                },
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$day",
                                                fontSize = 13.sp,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isToday -> Color.White
                                                    isSunday || isSaturday -> AccentRed
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                            )
                                            if (hasEvent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(
                                                            if (isToday) Color.White else AccentBlue,
                                                            CircleShape,
                                                        )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Events Legend
            Text("Agenda Bulan Ini", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            events.forEach { (day, event) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(SurfaceLight, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AccentBlueLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(event, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}






















