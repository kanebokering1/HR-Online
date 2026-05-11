package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.LemburViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemburScreen(onBack: () -> Unit, vm: LemburViewModel = viewModel()) {
    var showForm by remember { mutableStateOf(false) }
    var formDate by remember { mutableStateOf("") }    // "yyyy-MM-dd"
    var formStart by remember { mutableStateOf("") }   // "HH:mm"
    var formEnd by remember { mutableStateOf("") }
    var formReason by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val startTimeState = rememberTimePickerState(initialHour = 17, initialMinute = 0)
    val endTimeState   = rememberTimePickerState(initialHour = 20, initialMinute = 0)

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

    val requests by vm.requests.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            showForm = false
            formDate = ""; formStart = ""; formEnd = ""; formReason = ""
            vm.clearSubmitResult()
        }
    }

    val approvedCount = requests.count { it.approvalState.equals("approved", ignoreCase = true) }
    val pendingCount = requests.count { it.approvalState.equals("pending", ignoreCase = true) || it.approvalState.equals("submitted", ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Lembur", onBack = onBack)

        // Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(Icons.Filled.AccessTime, "$approvedCount", "Disetujui", AccentOrange, AccentOrangeLight, Modifier.weight(1f))
            StatCard(Icons.Filled.PendingActions, "$pendingCount", "Pending", AccentAmber, AccentAmberLight, Modifier.weight(1f))
        }

        HRButton(
            text = "Ajukan Lembur",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Lembur")

        if (isLoading && requests.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (requests.isEmpty() && error != null) {
            EmptyState(
                title = "Gagal Memuat Data",
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
            items(requests) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(item.overtimeDate, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${item.startTime} - ${item.endTime}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            StatusBadge(
                                text = item.approvalState,
                                type = when (item.approvalState.lowercase()) {
                                    "approved" -> BadgeType.SUCCESS
                                    "rejected" -> BadgeType.ERROR
                                    else -> BadgeType.WARNING
                                },
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Alasan: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        }
        }  // end else (requests not empty / no error)

        // ── Form Dialog ────────────────────────────────────────────
        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = {
                    Text(
                        "Ajukan Lembur",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Tanggal Lembur — DatePicker
                        OutlinedTextField(
                            value = epochToDisplay(datePickerState.selectedDateMillis).ifBlank { formDate },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tanggal Lembur") },
                            trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = GreenPrimary) },
                            isError = formError && formDate.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            enabled = false,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Jam Mulai — TimePicker
                            OutlinedTextField(
                                value = timeToString(startTimeState.hour, startTimeState.minute),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jam Mulai") },
                                trailingIcon = { Icon(Icons.Filled.Schedule, null, tint = GreenPrimary) },
                                isError = formError && formStart.isBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                                modifier = Modifier.weight(1f).clickable { showStartTimePicker = true },
                                enabled = false,
                            )
                            // Jam Selesai — TimePicker
                            OutlinedTextField(
                                value = timeToString(endTimeState.hour, endTimeState.minute),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jam Selesai") },
                                trailingIcon = { Icon(Icons.Filled.Schedule, null, tint = GreenPrimary) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, focusedLabelColor = GreenPrimary),
                                modifier = Modifier.weight(1f).clickable { showEndTimePicker = true },
                                enabled = false,
                            )
                        }
                        OutlinedTextField(
                            value = formReason,
                            onValueChange = { formReason = it; formError = false },
                            label = { Text("Alasan Lembur") },
                            minLines = 2,
                            isError = formError && formReason.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (formError) {
                            Text(
                                "Tanggal, jam mulai, dan alasan wajib diisi",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val resolvedDate = epochToApiDate(datePickerState.selectedDateMillis).ifBlank { formDate }
                            val resolvedStart = timeToString(startTimeState.hour, startTimeState.minute)
                            val resolvedEnd   = timeToString(endTimeState.hour,   endTimeState.minute)
                            formDate = resolvedDate
                            formStart = resolvedStart
                            formEnd = resolvedEnd
                            if (resolvedDate.isBlank() || formReason.isBlank()) {
                                formError = true
                            } else {
                                vm.submit(resolvedDate, resolvedStart, resolvedEnd, formReason)
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Kirim", fontFamily = PlusJakartaSans)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForm = false; formError = false }) {
                        Text("Batal", color = TextSecondary, fontFamily = PlusJakartaSans)
                    }
                },
            )
        }

        // ── Date / Time Picker Dialogs ─────────────────────────────────────────
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } },
            ) { DatePicker(state = datePickerState) }
        }
        if (showStartTimePicker) {
            AlertDialog(
                onDismissRequest = { showStartTimePicker = false },
                title = { Text("Jam Mulai Lembur") },
                text = { TimePicker(state = startTimeState) },
                confirmButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("Batal") } },
            )
        }
        if (showEndTimePicker) {
            AlertDialog(
                onDismissRequest = { showEndTimePicker = false },
                title = { Text("Jam Selesai Lembur") },
                text = { TimePicker(state = endTimeState) },
                confirmButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("Batal") } },
            )
        }
    }
}






















