package com.binahr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.LemburViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjukanLemburScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: LemburViewModel = viewModel(),
) {
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()

    val datePickerState = rememberDatePickerState()
    val startTimeState = rememberTimePickerState(initialHour = 17, initialMinute = 0)
    val endTimeState = rememberTimePickerState(initialHour = 20, initialMinute = 0)
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
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

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            vm.clearSubmitResult()
            onSuccess()
        }
        submitResult?.onFailure {
            vm.clearSubmitResult()
        }
    }

    val dateMs = datePickerState.selectedDateMillis
    val startTime = timeToString(startTimeState.hour, startTimeState.minute)
    val endTime = timeToString(endTimeState.hour, endTimeState.minute)

    FormScaffold(
        title = "Ajukan Lembur",
        onBack = onBack,
        submitLabel = "Kirim Pengajuan",
        isSubmitting = isLoading,
        submitEnabled = dateMs != null && reason.isNotBlank(),
        onSubmit = {
            val date = epochToApiDate(dateMs)
            if (date.isBlank() || reason.isBlank()) {
                showError = true
            } else {
                showError = false
                vm.submit(date, startTime, endTime, reason)
            }
        },
    ) {
        error?.let {
            InfoCallout(message = it, type = CalloutType.ERROR)
            Spacer(Modifier.height(8.dp))
        }

        // Date
        OutlinedTextField(
            value = epochToDisplay(dateMs).ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Lembur") },
            trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = GreenPrimary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (showError && dateMs == null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (showError && dateMs == null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = GreenPrimary,
            ),
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
        )

        Spacer(Modifier.height(12.dp))

        // Time Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = startTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Jam Mulai") },
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
                    .weight(1f)
                    .clickable { showStartTimePicker = true },
            )
            OutlinedTextField(
                value = endTime,
                onValueChange = {},
                readOnly = true,
                label = { Text("Jam Selesai") },
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
                    .weight(1f)
                    .clickable { showEndTimePicker = true },
            )
        }

        Spacer(Modifier.height(12.dp))

        // Reason
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it; showError = false },
            label = { Text("Alasan Lembur") },
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

    if (showStartTimePicker) {
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Jam Mulai Lembur") },
            text = { TimePicker(state = startTimeState) },
            confirmButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Batal") }
            },
        )
    }

    if (showEndTimePicker) {
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("Jam Selesai Lembur") },
            text = { TimePicker(state = endTimeState) },
            confirmButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Batal") }
            },
        )
    }
}
