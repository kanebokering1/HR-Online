package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hroes.data.AttendanceStorage
import com.example.hroes.data.api.TokenManager
import com.example.hroes.navigation.Screen
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.HomeViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onNotificationClick: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshTick by remember { mutableStateOf(0) }
    val todayAttendance = remember(refreshTick) { AttendanceStorage.getTodayAttendance(context) }
    val hasCheckedIn = todayAttendance.any { it.type == com.example.hroes.data.AttendanceType.CHECK_IN }
    val hasCheckedOut = todayAttendance.any { it.type == com.example.hroes.data.AttendanceType.CHECK_OUT }
    val checkInTime = todayAttendance.firstOrNull { it.type == com.example.hroes.data.AttendanceType.CHECK_IN }?.time ?: "--:--"
    val checkOutTime = todayAttendance.firstOrNull { it.type == com.example.hroes.data.AttendanceType.CHECK_OUT }?.time ?: "--:--"

    val summary by vm.summary.collectAsStateWithLifecycle()
    val isRefreshingApi by vm.isRefreshing.collectAsStateWithLifecycle()

    // User identity from TokenManager (populated at login).
    val displayName = TokenManager.userName ?: "Karyawan"
    val displayPosition = TokenManager.userEmail ?: ""

    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullRefreshState(
        refreshing = isRefreshing || isRefreshingApi,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                kotlinx.coroutines.delay(300)
                refreshTick++
                vm.load()
                isRefreshing = false
            }
        },
    )

    val currentTime = remember { mutableStateOf("") }
    val currentSeconds = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }
    val greeting = remember { mutableStateOf("Selamat Pagi") }
    val greetingEmoji = remember { mutableStateOf("☀") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            val cal = Calendar.getInstance().apply { time = now }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val (g, e) = when (hour) {
                in 4..10  -> "Selamat Pagi"  to "\u2600"
                in 11..14 -> "Selamat Siang" to "\u2600"
                in 15..17 -> "Selamat Sore"  to "\u2600"
                else      -> "Selamat Malam" to "\u263D"
            }
            greeting.value = g
            greetingEmoji.value = e
            currentTime.value    = SimpleDateFormat("HH:mm",  Locale.getDefault()).format(now)
            currentSeconds.value = SimpleDateFormat(":ss",    Locale.getDefault()).format(now)
            currentDate.value    = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullState)
            .pointerInput(Unit) {
                var totalDx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalDx = 0f },
                    onDragEnd = {
                        // Swipe LEFT (negative dx) to go to History tab
                        if (totalDx < -120f) onNavigate(Screen.History.route)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDx += dragAmount
                    },
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // HEADER (gradient + decorative blobs)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Emerald700, Emerald500),
                    )
                )
        ) {
            // decorative circles (subtle, in corners)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = (-60).dp, y = (-40).dp)
                    .background(Color.White.copy(alpha = 0.06f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-20).dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Top row: avatar + greeting + notification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AvatarImage(initials = displayName.take(2).uppercase(), size = 48.dp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${greeting.value} ${greetingEmoji.value}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSans,
                            )
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSans,
                            )
                            Text(
                                text = displayPosition.ifBlank { "Karyawan" },
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontFamily = PlusJakartaSans,
                            )
                        }
                    }
                    // notif pill
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.18f), CircleShape)
                            .clickable { onNotificationClick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = AccentRed) {
                                    Text("3", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifikasi",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // ATTENDANCE CARD (floating, with timeline)
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        HRCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-32).dp),
            elevation = 8.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Date + Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentDate.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    StatusBadge(
                        text = when {
                            hasCheckedOut -> "Selesai"
                            hasCheckedIn -> "Bekerja"
                            else -> "Belum Absen"
                        },
                        type = when {
                            hasCheckedOut -> BadgeType.SUCCESS
                            hasCheckedIn -> BadgeType.INFO
                            else -> BadgeType.WARNING
                        },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live clock â€” big
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = currentTime.value,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                        color = Emerald700,
                    )
                    Text(
                        text = currentSeconds.value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSans,
                        color = Emerald400,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Kantor Pusat",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = PlusJakartaSans,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mini timeline check-in/check-out
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Emerald50, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TimelineEntry(
                        icon = Icons.AutoMirrored.Filled.Login,
                        label = "Check In",
                        time = checkInTime,
                        color = Emerald600,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Emerald200)
                    )
                    TimelineEntry(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        label = "Check Out",
                        time = checkOutTime,
                        color = AccentRed,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { onNavigate(Screen.Attendance.createRoute("CHECK_IN")) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600, disabledContainerColor = Emerald100),
                        enabled = !hasCheckedIn,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check In", fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSans)
                    }
                    Button(
                        onClick = { onNavigate(Screen.Attendance.createRoute("CHECK_OUT")) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed, disabledContainerColor = AccentRedLight),
                        enabled = hasCheckedIn && !hasCheckedOut,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check Out", fontWeight = FontWeight.SemiBold, fontFamily = PlusJakartaSans)
                    }
                }
            }
        }

        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // SUMMARY STATS
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-16).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Ringkasan Bulan Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
                )
                Text(
                    text = "April 2026",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontFamily = PlusJakartaSans,
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigate(Screen.History.route) }
                    .background(Emerald50)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Lihat Detail",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald700,
                    fontFamily = PlusJakartaSans,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Emerald700,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-8).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ImprovedStatCard(Modifier.weight(1f), Icons.Filled.CheckCircle, "18", "Hadir", Emerald600, Emerald50, trend = "+2", positive = true)
            ImprovedStatCard(Modifier.weight(1f), Icons.Outlined.AccessTime, "2", "Telat", AccentOrange, AccentOrangeLight, trend = "-1", positive = true)
            ImprovedStatCard(Modifier.weight(1f), Icons.Outlined.EventBusy, "1", "Izin", AccentBlue, AccentBlueLight, trend = "+1", positive = false)
            ImprovedStatCard(Modifier.weight(1f), Icons.Outlined.BeachAccess, "0", "Cuti", AccentPurple, AccentPurpleLight, trend = "0", positive = true)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // MENU GRID
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        SectionHeader(title = "Menu Akses Cepat")

        val menuItems = listOf(
            MenuGridItem("Data Diri", Icons.Outlined.Person, AccentBlue, AccentBlueLight, Screen.DataDiri.route),
            MenuGridItem("History", Icons.Outlined.History, TealAccent, Green50, Screen.History.route),
            MenuGridItem("Kalender", Icons.Outlined.CalendarMonth, AccentPurple, AccentPurpleLight, Screen.Kalender.route),
            MenuGridItem("Cuti", Icons.Outlined.BeachAccess, AccentOrange, AccentOrangeLight, Screen.Cuti.route),
            MenuGridItem("Slip Gaji", Icons.Outlined.AccountBalanceWallet, Emerald600, Emerald50, Screen.SlipGaji.route),
            MenuGridItem("Lembur", Icons.Outlined.MoreTime, AccentAmber, AccentAmberLight, Screen.Lembur.route),
            MenuGridItem("Izin/Sakit", Icons.Outlined.LocalHospital, AccentRed, AccentRedLight, Screen.Cuti.route),
            MenuGridItem("Reimburse", Icons.Outlined.Receipt, AccentIndigo, AccentIndigoLight, Screen.Reimbursement.route),
            MenuGridItem("Pengumuman", Icons.Outlined.Campaign, AccentPink, AccentPinkLight, Screen.Pengumuman.route),
            MenuGridItem("Organisasi", Icons.Outlined.AccountTree, AccentCyan, AccentCyanLight, Screen.StrukturOrg.route),
            MenuGridItem("Dokumen", Icons.Outlined.Description, AccentBrown, AccentBrownLight, Screen.Dokumen.route),
            MenuGridItem("FAQ", Icons.Outlined.HelpOutline, TextTertiary, SurfaceLight, Screen.FAQ.route),
            MenuGridItem("Persetujuan", Icons.Outlined.HowToReg, GreenPrimary, Green50, Screen.Approvals.route),
            MenuGridItem("Performa", Icons.Outlined.Leaderboard, AccentAmber, AccentAmberLight, Screen.Performance.route),
            MenuGridItem("Jadwal Shift", Icons.Outlined.Schedule, AccentBlue, AccentBlueLight, Screen.JadwalShift.route),
            MenuGridItem("Aset Saya", Icons.Outlined.Inventory2, AccentOrange, AccentOrangeLight, Screen.Asset.route),
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            menuItems.chunked(4).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { item ->
                        MenuCard(
                            item = item,
                            onClick = { onNavigate(item.route) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        // ANNOUNCEMENTS
        // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
        SectionHeader(title = "Pengumuman Terbaru", actionText = "Lihat Semua", onAction = { onNavigate(Screen.Pengumuman.route) })

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sampleAnnouncements) { item ->
                AnnouncementCard(title = item.first, date = item.second, preview = item.third, accentColor = item.fourth)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        }

        // Pull-to-refresh indicator overlay
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = Emerald600,
        )
    }
}

// â•â•â•â•â•â•â• Helper Data & Composables â•â•â•â•â•â•â•

private data class MenuGridItem(
    val label: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
    val route: String,
)

private data class Announcement(
    val first: String,
    val second: String,
    val third: String,
    val fourth: Color,
)

private val sampleAnnouncements = listOf(
    Announcement("Libur Hari Raya Idul Fitri", "10 Apr 2026", "Diberitahukan bahwa kantor akan libur pada...", Emerald600),
    Announcement("Gathering Tahunan 2026", "05 Apr 2026", "Acara gathering tahun ini akan diadakan di...", AccentPurple),
    Announcement("Update Kebijakan WFH", "01 Apr 2026", "Mulai bulan depan, kebijakan WFH diperbarui...", AccentOrange),
)

@Composable
private fun TimelineEntry(icon: ImageVector, label: String, time: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextTertiary, fontFamily = PlusJakartaSans)
            Text(
                time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = PlusJakartaSans,
            )
        }
    }
}

@Composable
private fun ImprovedStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    bgColor: Color,
    trend: String,
    positive: Boolean,
) {
    Surface(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top row: icon + trend chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(bgColor, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                if (trend != "0") {
                    val trendColor = if (positive) Emerald600 else AccentRed
                    Row(
                        modifier = Modifier
                            .background(trendColor.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            if (positive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(10.dp),
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            trend,
                            fontSize = 9.sp,
                            color = trendColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PlusJakartaSans,
                        )
                    }
                }
            }
            // Bottom: value + label
            Column {
                Text(
                    value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp,
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun MenuCard(
    item: MenuGridItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(item.bgColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, contentDescription = item.label, tint = item.iconColor, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = PlusJakartaSans,
            maxLines = 1,
        )
    }
}

@Composable
private fun AnnouncementCard(title: String, date: String, preview: String, accentColor: Color) {
    Surface(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row {
            // colored side bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            "Penting",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            fontFamily = PlusJakartaSans,
                        )
                    }
                    Text(text = date, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
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
}























