package com.example.hronline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import com.example.hronline.data.AttendanceRecord
import com.example.hronline.data.AttendanceStorage
import com.example.hronline.data.AttendanceType
import com.example.hronline.ui.theme.SplashTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashTheme {
                HistoryScreen(
                    activity = this@HistoryActivity,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    activity: ComponentActivity,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFFFF6568)
    
    // Get current month and year
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Load attendance data for selected month/year - refresh when screen is resumed
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    
    // Refresh when month/year changes or when screen is first loaded
    LaunchedEffect(selectedMonth, selectedYear) {
        attendanceRecords = AttendanceStorage.getAttendanceByMonth(context, selectedMonth, selectedYear)
        android.util.Log.d("HistoryActivity", "Data refreshed: ${attendanceRecords.size} records for $selectedMonth/$selectedYear")
    }
    
    // Refresh when activity is resumed
    LaunchedEffect(Unit) {
        attendanceRecords = AttendanceStorage.getAttendanceByMonth(context, selectedMonth, selectedYear)
        android.util.Log.d("HistoryActivity", "Initial load: ${attendanceRecords.size} records")
    }
    
    // Group records by date
    val groupedRecords = remember(attendanceRecords) {
        AttendanceStorage.groupRecordsByDate(attendanceRecords)
    }
    
    // Calculate summary statistics
    val summary = remember(groupedRecords) {
        calculateSummary(groupedRecords)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "History Absensi",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
        ) {
            // Header Section
            HeaderSection(
                primaryColor = primaryColor,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onDateClick = { showDatePicker = true }
            )
            
            // Date Picker
            DatePickerSection(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onDateClick = { showDatePicker = true }
            )
            
            // Summary Cards
            SummaryCardsSection(summary = summary)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Attendance List
            AttendanceListSection(
                groupedRecords = groupedRecords,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear
            )
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            MonthYearPickerDialog(
                initialMonth = selectedMonth,
                initialYear = selectedYear,
                onDateSelected = { month, year ->
                    selectedMonth = month
                    selectedYear = year
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun HeaderSection(
    primaryColor: Color,
    selectedMonth: Int,
    selectedYear: Int,
    onDateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Silahkan pilih Bulan dan tahun untuk dapat melihat history",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun DatePickerSection(
    selectedMonth: Int,
    selectedYear: Int,
    onDateClick: () -> Unit
) {
    val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onDateClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${monthNames[selectedMonth - 1]} $selectedYear",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

data class SummaryData(
    val tidakAbsen: Int = 0,
    val telat: Int = 0,
    val pulangAwal: Int = 0,
    val izin: Int = 0,
    val cuti: Int = 0,
    val tidakAbsenPulang: Int = 0
)

fun calculateSummary(groupedRecords: Map<String, Pair<AttendanceRecord?, AttendanceRecord?>>): SummaryData {
    var tidakAbsen = 0
    var telat = 0
    var pulangAwal = 0
    var tidakAbsenPulang = 0
    
    groupedRecords.forEach { (_, records) ->
        val (checkIn, checkOut) = records
        
        if (checkIn == null && checkOut == null) {
            tidakAbsen++
        } else if (checkIn != null && checkOut == null) {
            tidakAbsenPulang++
        } else if (checkIn != null) {
            // Check if late (after 08:00)
            val checkInTime = checkIn.time.split(" ")[0] // Remove "WIB"
            val (hour, minute) = checkInTime.split(":").map { it.toIntOrNull() ?: 0 }
            if (hour > 8 || (hour == 8 && minute > 0)) {
                telat++
            }
            // Check if early leave (before 17:00)
            val checkOutTime = checkOut?.time?.split(" ")?.get(0) ?: ""
            if (checkOutTime.isNotEmpty()) {
                val (outHour, outMinute) = checkOutTime.split(":").map { it.toIntOrNull() ?: 0 }
                if (outHour < 17) {
                    pulangAwal++
                }
            }
        }
    }
    
    return SummaryData(
        tidakAbsen = tidakAbsen,
        telat = telat,
        pulangAwal = pulangAwal,
        tidakAbsenPulang = tidakAbsenPulang
    )
}

@Composable
fun SummaryCardsSection(summary: SummaryData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCardItem("Tidak Absen", summary.tidakAbsen)
                }
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCardItem("Telat", summary.telat)
                }
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCardItem("Pulang Awal", summary.pulangAwal)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCardItem("Izin", summary.izin)
                }
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCardItem("Cuti", summary.cuti)
                }
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCardItem("Tidak Absen Pulang", summary.tidakAbsenPulang)
                }
            }
        }
    }
}

@Composable
fun SummaryCardItem(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF5F5F5))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AttendanceListSection(
    groupedRecords: Map<String, Pair<AttendanceRecord?, AttendanceRecord?>>,
    selectedMonth: Int,
    selectedYear: Int
) {
    // Sort dates descending
    val sortedDates = groupedRecords.keys.sortedDescending()
    
    if (sortedDates.isEmpty()) {
        // Empty state
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tidak ada data absensi",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TANGGAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "MASUK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "PULANG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(24.dp)) // Space for chevron
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE0E0E0)
                )
                
                // Table Rows with single expand logic
                var expandedDate by remember { mutableStateOf<String?>(null) }
                sortedDates.forEachIndexed { index, date ->
                    val (checkIn, checkOut) = groupedRecords[date] ?: Pair(null, null)
                    AttendanceTableRow(
                        date = date,
                        checkIn = checkIn,
                        checkOut = checkOut,
                        isExpanded = expandedDate == date,
                        onExpandClick = { 
                            expandedDate = if (expandedDate == date) null else date
                        }
                    )
                    
                    if (index < sortedDates.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0xFFF5F5F5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceTableRow(
    date: String,
    checkIn: AttendanceRecord?,
    checkOut: AttendanceRecord?,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    val isHoliday = checkIn == null && checkOut == null && isWeekendOrHoliday(date)
    val checkInTime = checkIn?.time?.split(" ")?.get(0) ?: "-"
    val checkOutTime = checkOut?.time?.split(" ")?.get(0) ?: "-"
    val primaryColor = Color(0xFFFF6568)
    val hasData = checkIn != null || checkOut != null
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable(enabled = hasData, onClick = onExpandClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDateDisplay(date),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isHoliday) Color(0xFFD32F2F) else Color.Black
                )
                if (isHoliday) {
                    Text(
                        text = "Hari Libur",
                        fontSize = 10.sp,
                        color = Color(0xFFD32F2F)
                    )
                } else {
                    Text(
                        text = "Hari Kerja",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Text(
                text = checkInTime,
                fontSize = 13.sp,
                color = if (checkInTime == "-") Color.Gray else Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = checkOutTime,
                fontSize = 13.sp,
                color = if (checkOutTime == "-") Color.Gray else Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (hasData) Color.Gray else Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 4.dp)
            )
        }
        
        // Expanded Details
        if (isExpanded && hasData) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFF5F5F5)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Check In Details
                checkIn?.let { record ->
                    AttendanceDetailItem(
                        label = "Masuk",
                        time = record.time,
                        location = record.location,
                        faceVerified = record.faceVerified,
                        primaryColor = primaryColor
                    )
                }
                
                // Check Out Details
                checkOut?.let { record ->
                    if (checkIn != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    AttendanceDetailItem(
                        label = "Pulang",
                        time = record.time,
                        location = record.location,
                        faceVerified = record.faceVerified,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceDetailItem(
    label: String,
    time: String,
    location: String,
    faceVerified: Boolean,
    primaryColor: Color
) {
    val successColor = Color(0xFF4CAF50)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Time",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = time,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        
        // Location
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = primaryColor,
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location,
                    fontSize = 12.sp,
                    color = Color.Black,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.clickable {
                        // TODO: Share location
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShareLocation,
                        contentDescription = "Share Location",
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bagikan Lokasi",
                        fontSize = 11.sp,
                        color = primaryColor
                    )
                }
            }
        }
        
        // Face Verification
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Face Verification",
                tint = if (faceVerified) successColor else Color(0xFFD32F2F),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (faceVerified) "Verifikasi Wajah: Berhasil" else "Verifikasi Wajah: Gagal",
                fontSize = 12.sp,
                color = if (faceVerified) successColor else Color(0xFFD32F2F),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatDateDisplay(dateString: String): String {
    return try {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val date = dateFormat.parse(dateString)
        if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val monthNames = arrayOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
            val monthName = monthNames[calendar.get(Calendar.MONTH)]
            "$day $monthName"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

fun isWeekendOrHoliday(dateString: String): Boolean {
    return try {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val date = dateFormat.parse(dateString)
        if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

@Composable
fun MonthYearPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    onDateSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedYear by remember { mutableStateOf(initialYear) }
    
    val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Title with better styling
                Text(
                    text = "Pilih Bulan dan Tahun",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // Month Picker with better styling
                Text(
                    text = "Bulan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Month list - compact grid layout (2 columns)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    // Split into 2 columns - 6 rows
                    for (row in 0 until 6) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left column
                            val leftIndex = row
                            val leftMonthIndex = leftIndex + 1
                            val isLeftSelected = selectedMonth == leftMonthIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMonth = leftMonthIndex }
                                    .background(if (isLeftSelected) Color(0xFFFF6568).copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = monthNames[leftIndex],
                                    fontSize = 13.sp,
                                    fontWeight = if (isLeftSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isLeftSelected) Color(0xFFFF6568) else Color.Black
                                )
                            }
                            
                            // Divider between columns
                            if (row < 5) {
                                VerticalDivider(
                                    modifier = Modifier.height(40.dp),
                                    color = Color(0xFFE0E0E0),
                                    thickness = 0.5.dp
                                )
                            }
                            
                            // Right column
                            val rightIndex = row + 6
                            val rightMonthIndex = rightIndex + 1
                            val isRightSelected = selectedMonth == rightMonthIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMonth = rightMonthIndex }
                                    .background(if (isRightSelected) Color(0xFFFF6568).copy(alpha = 0.1f) else Color.Transparent)
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = monthNames[rightIndex],
                                    fontSize = 13.sp,
                                    fontWeight = if (isRightSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isRightSelected) Color(0xFFFF6568) else Color.Black
                                )
                            }
                        }
                        
                        // Horizontal divider between rows
                        if (row < 5) {
                            HorizontalDivider(
                                color = Color(0xFFE0E0E0),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Year Picker - Flexible with scroll
                Text(
                    text = "Tahun",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Year list - compact grid layout (3 columns, max 2026, more flexible)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    val maxYear = 2026
                    val minYear = 2020 // Show 7 years total (2020-2026) - more flexible
                    val yearsList = (maxYear downTo minYear).toList()
                    
                    // Split into rows of 3
                    for (row in 0 until (yearsList.size + 2) / 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                if (index < yearsList.size) {
                                    val year = yearsList[index]
                                    val isSelected = selectedYear == year
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { 
                                                if (year <= maxYear) { // Ensure cannot select > 2026
                                                    selectedYear = year
                                                }
                                            }
                                            .background(if (isSelected) Color(0xFFFF6568).copy(alpha = 0.1f) else Color.Transparent)
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = year.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFFFF6568) else Color.Black
                                        )
                                    }
                                    
                                    // Vertical divider between columns
                                    if (col < 2 && index < yearsList.size - 1) {
                                        VerticalDivider(
                                            modifier = Modifier.height(42.dp),
                                            color = Color(0xFFE0E0E0),
                                            thickness = 0.5.dp
                                        )
                                    }
                                } else {
                                    // Empty space for alignment
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        
                        // Horizontal divider between rows
                        if (row < (yearsList.size + 2) / 3 - 1) {
                            HorizontalDivider(
                                color = Color(0xFFE0E0E0),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Action Buttons with better styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text(
                            "Batal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onDateSelected(selectedMonth, selectedYear) },
                        modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6568)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Pilih",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
