package com.binahr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCorrectionScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: AttendanceViewModel = viewModel(),
) {
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val correctionResult by vm.correctionResult.collectAsStateWithLifecycle()
    val logsForDate by vm.logsForDate.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val datePickerState = rememberDatePickerState()
    val clockInState = rememberTimePickerState(initialHour = 8, initialMinute = 0)
    val clockOutState = rememberTimePickerState(initialHour = 17, initialMinute = 0)
    var showDatePicker by remember { mutableStateOf(false) }
    var showClockInPicker by remember { mutableStateOf(false) }
    var showClockOutPicker by remember { mutableStateOf(false) }
    var hasClockIn by remember { mutableStateOf(true) }
    var hasClockOut by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var dateLoaded by remember { mutableStateOf(false) }

    fun epochToApiDate(ms: Long?): String {
        if (ms == null) return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(ms))
    }

    fun epochToDisplay(ms: Long?): String {
        if (ms == null) return ""
        return SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(ms))
    }

    fun timeToString(h: Int, m: Int) = String.format(Locale.getDefault(), "%02d:%02d", h, m)

    // Combine date (yyyy-MM-dd) + time (HH:mm) into ISO datetime (yyyy-MM-dd HH:mm:ss)
    fun toDateTime(dateStr: String, h: Int, m: Int) = "$dateStr ${timeToString(h, m)}:00"

    val dateMs = datePickerState.selectedDateMillis

    // When date changes, load logs from server
    LaunchedEffect(dateMs) {
        if (dateMs != null) {
            val apiDate = epochToApiDate(dateMs)
            vm.loadLogsForDate(apiDate)
            dateLoaded = true
        }
    }

    // Pre-fill time pickers from existing log
    val existingLog = logsForDate.firstOrNull()
    LaunchedEffect(existingLog) {
        existingLog?.clockInAt?.let { raw ->
            // raw format: "yyyy-MM-dd HH:mm:ss" or ISO
            val parts = raw.split(" ", "T")
            val timePart = parts.getOrNull(1) ?: return@let
            val hm = timePart.split(":")
            val h = hm.getOrNull(0)?.toIntOrNull() ?: return@let
            val m = hm.getOrNull(1)?.toIntOrNull() ?: return@let
            clockInState.hour = h
            clockInState.minute = m
        }
        existingLog?.clockOutAt?.let { raw ->
            val parts = raw.split(" ", "T")
            val timePart = parts.getOrNull(1) ?: return@let
            val hm = timePart.split(":")
            val h = hm.getOrNull(0)?.toIntOrNull() ?: return@let
            val m = hm.getOrNull(1)?.toIntOrNull() ?: return@let
            clockOutState.hour = h
            clockOutState.minute = m
            hasClockOut = true
        }
    }

    LaunchedEffect(correctionResult) {
        correctionResult?.onSuccess {
            vm.clearCorrectionResult()
            onSuccess()
        }
        correctionResult?.onFailure {
            vm.clearCorrectionResult()
        }
    }

    FormScaffold(
        title = "Koreksi Kehadiran",
        onBack = onBack,
        submitLabel = "Kirim Koreksi",
        isSubmitting = isLoading,
        submitEnabled = dateMs != null && reason.isNotBlank() && existingLog != null && !isLoading,
        onSubmit = {
            val apiDate = epochToApiDate(dateMs)
            val logId = existingLog?.id
            if (apiDate.isBlank() || reason.isBlank() || logId == null) {
                showError = true
            } else {
                showError = false
                vm.submitCorrection(
                    logId = logId,
                    clockInAt = toDateTime(apiDate, clockInState.hour, clockInState.minute),
                    clockOutAt = if (hasClockOut) toDateTime(apiDate, clockOutState.hour, clockOutState.minute) else null,
                    reason = reason,
                )
            }
        },
    ) {
        error?.let {
            InfoCallout(message = it, type = CalloutType.ERROR)
            Spacer(Modifier.height(8.dp))
        }

        // ── Tanggal ────────────────────────────────────────────────────
        OutlinedTextField(
            value = epochToDisplay(dateMs).ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Kehadiran") },
            trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = OrangePrimary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (showError && dateMs == null)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (showError && dateMs == null)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = OrangePrimary,
            ),
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
        )

        Spacer(Modifier.height(12.dp))

        // ── Log yang ditemukan ─────────────────────────────────────────
        if (dateMs != null && dateLoaded) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangePrimary, modifier = Modifier.size(24.dp))
                    }
                }
                existingLog == null -> {
                    InfoCallout(
                        message = "Tidak ada data absensi untuk tanggal ini.",
                        type = CalloutType.WARNING,
                    )
                }
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangeSurface),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "Data absensi ditemukan",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrangePrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Masuk: ${existingLog.clockInAt?.take(16)?.replace("T", " ") ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                            Text(
                                "Keluar: ${existingLog.clockOutAt?.take(16)?.replace("T", " ") ?: "Belum absen keluar"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        // ── Jam Masuk ──────────────────────────────────────────────────
        if (existingLog != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = hasClockIn, onCheckedChange = { hasClockIn = it })
                Text(
                    "Koreksi Jam Masuk",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                if (hasClockIn) {
                    OutlinedTextField(
                        value = timeToString(clockInState.hour, clockInState.minute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jam Masuk") },
                        trailingIcon = { Icon(Icons.Filled.Schedule, null, tint = OrangePrimary) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTrailingIconColor = OrangePrimary,
                        ),
                        enabled = false,
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { showClockInPicker = true },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Jam Keluar ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = hasClockOut, onCheckedChange = { hasClockOut = it })
                Text(
                    "Koreksi Jam Keluar",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                if (hasClockOut) {
                    OutlinedTextField(
                        value = timeToString(clockOutState.hour, clockOutState.minute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jam Keluar") },
                        trailingIcon = { Icon(Icons.Filled.Schedule, null, tint = OrangePrimary) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTrailingIconColor = OrangePrimary,
                        ),
                        enabled = false,
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { showClockOutPicker = true },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // ── Alasan ─────────────────────────────────────────────────────
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it; showError = false },
            label = { Text("Alasan Koreksi") },
            minLines = 3,
            isError = showError && reason.isBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                focusedLabelColor = OrangePrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (showError) {
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    dateMs == null -> "Pilih tanggal terlebih dahulu"
                    existingLog == null -> "Tidak ada data absensi untuk tanggal ini"
                    reason.isBlank() -> "Alasan koreksi wajib diisi"
                    else -> "Periksa kembali data yang diisi"
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showClockInPicker) {
        AlertDialog(
            onDismissRequest = { showClockInPicker = false },
            title = { Text("Jam Masuk") },
            text = { TimePicker(state = clockInState) },
            confirmButton = { TextButton(onClick = { showClockInPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showClockInPicker = false }) { Text("Batal") } },
        )
    }

    if (showClockOutPicker) {
        AlertDialog(
            onDismissRequest = { showClockOutPicker = false },
            title = { Text("Jam Keluar") },
            text = { TimePicker(state = clockOutState) },
            confirmButton = { TextButton(onClick = { showClockOutPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showClockOutPicker = false }) { Text("Batal") } },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCorrectionScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: AttendanceViewModel = viewModel(),
) {
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val correctionResult by vm.correctionResult.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val datePickerState = rememberDatePickerState()
    val clockInState = rememberTimePickerState(initialHour = 8, initialMinute = 0)
    val clockOutState = rememberTimePickerState(initialHour = 17, initialMinute = 0)
    var showDatePicker by remember { mutableStateOf(false) }
    var showClockInPicker by remember { mutableStateOf(false) }
    var showClockOutPicker by remember { mutableStateOf(false) }
    var hasClockIn by remember { mutableStateOf(false) }
    var hasClockOut by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun epochToApiDate(ms: Long?): String {
        if (ms == null) return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(ms))
    }

    fun epochToDisplay(ms: Long?): String {
        if (ms == null) return ""
        return SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(ms))
    }

    fun timeToString(h: Int, m: Int) = String.format("%02d:%02d", h, m)

    LaunchedEffect(correctionResult) {
        correctionResult?.onSuccess {
            vm.clearCorrectionResult()
            onSuccess()
        }
        correctionResult?.onFailure {
            vm.clearCorrectionResult()
        }
    }

    val dateMs = datePickerState.selectedDateMillis

    FormScaffold(
        title = "Koreksi Kehadiran",
        onBack = onBack,
        submitLabel = "Kirim Koreksi",
        isSubmitting = isLoading,
        submitEnabled = dateMs != null && reason.isNotBlank(),
        onSubmit = {
            val date = epochToApiDate(dateMs)
            if (date.isBlank() || reason.isBlank()) {
                showError = true
            } else {
                showError = false
                vm.submitCorrection(
                    date = date,
                    clockIn = if (hasClockIn) timeToString(clockInState.hour, clockInState.minute) else null,
                    clockOut = if (hasClockOut) timeToString(clockOutState.hour, clockOutState.minute) else null,
                    reason = reason,
                )
            }
        },
    ) {
        error?.let {
            InfoCallout(message = it, type = CalloutType.ERROR)
            Spacer(Modifier.height(8.dp))
        }

        // Tanggal
        OutlinedTextField(
            value = epochToDisplay(dateMs).ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Kehadiran") },
            trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = GreenPrimary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (showError && dateMs == null)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (showError && dateMs == null)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = GreenPrimary,
            ),
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
        )

        Spacer(Modifier.height(12.dp))

        // Jam Masuk
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = hasClockIn, onCheckedChange = { hasClockIn = it })
            Text(
                "Koreksi Jam Masuk",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (hasClockIn) {
                OutlinedTextField(
                    value = timeToString(clockInState.hour, clockInState.minute),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jam Masuk") },
                    trailingIcon = { Icon(Icons.Filled.Schedule, null, tint = GreenPrimary) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTrailingIconColor = GreenPrimary,
                    ),
                    enabled = false,
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { showClockInPicker = true },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Jam Keluar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = hasClockOut, onCheckedChange = { hasClockOut = it })
            Text(
                "Koreksi Jam Keluar",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (hasClockOut) {
                OutlinedTextField(
                    value = timeToString(clockOutState.hour, clockOutState.minute),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jam Keluar") },
                    trailingIcon = { Icon(Icons.Filled.Schedule, null, tint = GreenPrimary) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTrailingIconColor = GreenPrimary,
                    ),
                    enabled = false,
                    modifier = Modifier
                        .width(130.dp)
                        .clickable { showClockOutPicker = true },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Alasan
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it; showError = false },
            label = { Text("Alasan Koreksi") },
            minLines = 3,
            isError = showError && reason.isBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                focusedLabelColor = GreenPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (showError) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Tanggal dan alasan wajib diisi",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showClockInPicker) {
        AlertDialog(
            onDismissRequest = { showClockInPicker = false },
            title = { Text("Jam Masuk") },
            text = { TimePicker(state = clockInState) },
            confirmButton = { TextButton(onClick = { showClockInPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showClockInPicker = false }) { Text("Batal") } },
        )
    }

    if (showClockOutPicker) {
        AlertDialog(
            onDismissRequest = { showClockOutPicker = false },
            title = { Text("Jam Keluar") },
            text = { TimePicker(state = clockOutState) },
            confirmButton = { TextButton(onClick = { showClockOutPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showClockOutPicker = false }) { Text("Batal") } },
        )
    }
}
