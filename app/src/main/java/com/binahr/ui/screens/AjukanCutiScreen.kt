package com.binahr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.CutiViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjukanCutiScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: CutiViewModel = viewModel(),
) {
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()

    var leaveType by remember { mutableStateOf("Cuti Tahunan") }
    var typeExpanded by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
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

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            vm.clearSubmitResult()
            onSuccess()
        }
        submitResult?.onFailure {
            vm.clearSubmitResult()
        }
    }

    val startDateMs = startPickerState.selectedDateMillis
    val endDateMs = endPickerState.selectedDateMillis

    FormScaffold(
        title = "Ajukan Cuti",
        onBack = onBack,
        submitLabel = "Kirim Pengajuan",
        isSubmitting = isLoading,
        submitEnabled = startDateMs != null && reason.isNotBlank(),
        onSubmit = {
            val start = epochToApiDate(startDateMs)
            val end = epochToApiDate(endDateMs).ifBlank { start }
            if (start.isBlank() || reason.isBlank()) {
                showError = true
            } else {
                showError = false
                vm.submit(leaveType, start, end, reason)
            }
        },
    ) {
        error?.let {
            InfoCallout(message = it, type = CalloutType.ERROR)
            Spacer(Modifier.height(8.dp))
        }

        // Leave Type Dropdown
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it },
        ) {
            OutlinedTextField(
                value = leaveType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Jenis Cuti") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false },
            ) {
                listOf("Cuti Tahunan", "Cuti Sakit", "Cuti Besar", "Cuti Melahirkan", "Cuti Tidak Berbayar").forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t) },
                        onClick = { leaveType = t; typeExpanded = false },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Start Date
        OutlinedTextField(
            value = epochToDisplay(startDateMs).ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Mulai") },
            trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = GreenPrimary) },
            isError = showError && startDateMs == null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (showError && startDateMs == null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (showError && startDateMs == null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = GreenPrimary,
            ),
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartPicker = true },
        )

        Spacer(Modifier.height(12.dp))

        // End Date
        OutlinedTextField(
            value = epochToDisplay(endDateMs).ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Selesai (opsional, kosongkan jika 1 hari)") },
            trailingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = GreenPrimary) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = GreenPrimary,
            ),
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEndPicker = true },
        )

        Spacer(Modifier.height(12.dp))

        // Reason
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it; showError = false },
            label = { Text("Alasan / Keterangan") },
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
                "Tanggal mulai dan alasan wajib diisi",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = { TextButton(onClick = { showStartPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Batal") } },
        ) { DatePicker(state = startPickerState) }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = { TextButton(onClick = { showEndPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Batal") } },
        ) { DatePicker(state = endPickerState) }
    }
}
