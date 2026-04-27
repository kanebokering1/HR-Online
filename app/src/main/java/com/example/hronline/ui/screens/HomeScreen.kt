package com.example.hronline.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hronline.data.AttendanceStorage
import com.example.hronline.navigation.Screen
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onNotificationClick: () -> Unit,
) {
    val context = LocalContext.current
    val todayAttendance = remember { AttendanceStorage.getTodayAttendance(context) }
    val hasCheckedIn = todayAttendance.any { it.type == com.example.hronline.data.AttendanceType.CHECK_IN }
    val hasCheckedOut = todayAttendance.any { it.type == com.example.hronline.data.AttendanceType.CHECK_OUT }

    val currentTime = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDate.value = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════════
        // HEADER
        // ══════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                // Top row: avatar + greeting + notification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarImage(initials = "AA", size = 48.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Selamat Pagi 👋",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontFamily = PlusJakartaSans,
                            )
                            Text(
                                text = "Aries Adityanto",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSans,
                            )
                        }
                    }
                    IconButton(onClick = onNotificationClick) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = AccentRed) {
                                    Text("3", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifikasi", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PT XYZ Tbk",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSans,
                )
            }
        }

        // ══════════════════════════════════════════
        // ATTENDANCE CARD
        // ══════════════════════════════════════════
        HRCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-16).dp),
            elevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = currentDate.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = currentTime.value,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlusJakartaSans,
                            color = GreenPrimary,
                        )
                    }
                    StatusBadge(
                        text = when {
                            hasCheckedOut -> "Selesai ✓"
                            hasCheckedIn -> "Sudah Check In"
                            else -> "Belum Absen"
                        },
                        type = when {
                            hasCheckedOut -> BadgeType.SUCCESS
                            hasCheckedIn -> BadgeType.INFO
                            else -> BadgeType.WARNING
                        },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { onNavigate(Screen.Attendance.createRoute("CHECK_IN")) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        enabled = !hasCheckedIn,
                    ) {
                        Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check In", fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSans)
                    }
                    Button(
                        onClick = { onNavigate(Screen.Attendance.createRoute("CHECK_OUT")) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        enabled = hasCheckedIn && !hasCheckedOut,
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check Out", fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSans)
                    }
                }
            }
        }

        // ══════════════════════════════════════════
        // SUMMARY STATS
        // ══════════════════════════════════════════
        SectionHeader(title = "Ringkasan Bulan Ini")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                StatCard(Icons.Filled.CheckCircle, "18", "Hadir", GreenPrimary, Green50)
            }
            item {
                StatCard(Icons.Filled.Schedule, "2", "Telat", AccentOrange, AccentOrangeLight)
            }
            item {
                StatCard(Icons.Filled.EventBusy, "1", "Izin", AccentBlue, AccentBlueLight)
            }
            item {
                StatCard(Icons.Filled.BeachAccess, "0", "Cuti", AccentPurple, AccentPurpleLight)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ══════════════════════════════════════════
        // MENU GRID (3x4)
        // ══════════════════════════════════════════
        SectionHeader(title = "Menu")

        val menuItems = listOf(
            MenuGridItem("Data Diri", Icons.Filled.Person, AccentBlue, AccentBlueLight, Screen.DataDiri.route),
            MenuGridItem("History", Icons.Filled.History, TealAccent, Green50, Screen.History.route),
            MenuGridItem("Kalender", Icons.Filled.CalendarMonth, AccentPurple, AccentPurpleLight, Screen.Kalender.route),
            MenuGridItem("Cuti", Icons.Filled.BeachAccess, AccentOrange, AccentOrangeLight, Screen.Cuti.route),
            MenuGridItem("Slip Gaji", Icons.Filled.AccountBalanceWallet, GreenPrimary, Green50, Screen.SlipGaji.route),
            MenuGridItem("Lembur", Icons.Filled.MoreTime, AccentAmber, AccentAmberLight, Screen.Lembur.route),
            MenuGridItem("Izin/Sakit", Icons.Filled.LocalHospital, AccentRed, AccentRedLight, Screen.Izin.route),
            MenuGridItem("Reimburse", Icons.Filled.Receipt, AccentIndigo, AccentIndigoLight, Screen.Reimbursement.route),
            MenuGridItem("Pengumuman", Icons.Filled.Campaign, AccentPink, AccentPinkLight, Screen.Pengumuman.route),
            MenuGridItem("Organisasi", Icons.Filled.AccountTree, AccentCyan, AccentCyanLight, Screen.StrukturOrg.route),
            MenuGridItem("Dokumen", Icons.Filled.Description, AccentBrown, AccentBrownLight, Screen.Dokumen.route),
            MenuGridItem("FAQ", Icons.Filled.HelpCenter, TextTertiary, SurfaceLight, Screen.FAQ.route),
        )

        // Grid layout: 4 rows x 3 columns
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            menuItems.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { item ->
                        MenuCard(
                            item = item,
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill remaining space if row has less than 3
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // ══════════════════════════════════════════
        // ANNOUNCEMENTS
        // ══════════════════════════════════════════
        SectionHeader(title = "Pengumuman Terbaru", actionText = "Lihat Semua", onAction = { onNavigate(Screen.Pengumuman.route) })

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sampleAnnouncements) { item ->
                AnnouncementCard(title = item.first, date = item.second, preview = item.third)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ═══════ Helper Data & Composables ═══════

private data class MenuGridItem(
    val label: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
    val route: String,
)

private val sampleAnnouncements = listOf(
    Triple("Libur Hari Raya Idul Fitri", "10 Apr 2026", "Diberitahukan bahwa kantor akan libur pada..."),
    Triple("Gathering Tahunan 2026", "05 Apr 2026", "Acara gathering tahun ini akan diadakan di..."),
    Triple("Update Kebijakan WFH", "01 Apr 2026", "Mulai bulan depan, kebijakan WFH diperbarui..."),
)

@Composable
private fun MenuCard(
    item: MenuGridItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HRCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.bgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, contentDescription = item.label, tint = item.iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AnnouncementCard(title: String, date: String, preview: String) {
    HRCard(
        modifier = Modifier.width(260.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusBadge(text = "Penting", type = BadgeType.ERROR)
                Text(text = date, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}
