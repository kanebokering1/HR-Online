package com.binahr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.ReimbursementViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjukanReimbursementScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    vm: ReimbursementViewModel = viewModel(),
) {
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Transportasi") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
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

    val dateMs = datePickerState.selectedDateMillis
    val amountLong = amountText.toLongOrNull() ?: 0L
    val isFormValid = title.isNotBlank() && amountLong > 0L && dateMs != null

    FormScaffold(
        title = "Ajukan Reimbursement",
        onBack = onBack,
        submitLabel = "Kirim Pengajuan",
        isSubmitting = isLoading,
        submitEnabled = isFormValid,
        onSubmit = {
            if (!isFormValid) {
                showError = true
            } else {
                showError = false
                vm.submit(title, category, amountLong, epochToApiDate(dateMs))
            }
        },
    ) {
        error?.let {
            InfoCallout(message = it, type = CalloutType.ERROR)
            Spacer(Modifier.height(8.dp))
        }

        // Title / Description
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; showError = false },
            label = { Text("Keterangan Pengeluaran") },
            isError = showError && title.isBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                focusedLabelColor = GreenPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategori") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
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
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false },
            ) {
                listOf("Transportasi", "Makan", "Kesehatan", "Akomodasi", "Lainnya").forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = { category = c; categoryExpanded = false },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Amount
        OutlinedTextField(
            value = amountText,
            onValueChange = { v ->
                if (v.all { it.isDigit() }) { amountText = v; showError = false }
            },
            label = { Text("Jumlah (Rp)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && amountLong <= 0L,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                focusedLabelColor = GreenPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Date
        OutlinedTextField(
            value = epochToDisplay(dateMs).ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Tanggal Pengeluaran") },
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

        if (showError) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Semua field wajib diisi dan jumlah harus lebih dari 0",
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
}
