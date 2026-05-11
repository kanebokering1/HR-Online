package com.binahr.ui.screens


import com.binahr.BuildConfig
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.ReimbursementViewModel
import com.binahr.util.toRupiah
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReimbursementScreen(onBack: () -> Unit, vm: ReimbursementViewModel = viewModel()) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Transportasi", "Makan", "Kesehatan", "Lainnya")
    var showForm by remember { mutableStateOf(false) }
    var formTitle by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("Transportasi") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var formAmount by remember { mutableStateOf("") }
    var formDate by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    val allItems by vm.claims.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            showForm = false
            formTitle = ""; formAmount = ""; formDate = ""
            vm.clearSubmitResult()
        }
    }

    val filteredItems = if (selectedFilter == "Semua") allItems
        else allItems.filter { it.title.contains(selectedFilter, ignoreCase = true) }
    val totalApproved = allItems.filter { it.approvalState.equals("approved", ignoreCase = true) }.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Reimbursement", onBack = onBack)

        // Total card
        HRCard(
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Reimbursement Disetujui", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Text(
                    totalApproved.toRupiah(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentIndigo,
                )
            }
        }

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = filter == selectedFilter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontFamily = PlusJakartaSans) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HRButton(
            text = "Ajukan Reimbursement",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Pengajuan")

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredItems) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(item.submittedAt ?: "", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = "Reimbursement", type = BadgeType.NEUTRAL)
                            }
                            Text(
                                item.amount.toLong().toRupiah(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentIndigo,
                            )
                        }
                    }
                }
            }
        }

        // ── Form Dialog ───────────────────────────────────────────────
        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = {
                    Text(
                        "Ajukan Reimbursement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formTitle,
                            onValueChange = { formTitle = it; formError = false },
                            label = { Text("Keterangan Pengeluaran") },
                            isError = formError && formTitle.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // Category dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = formCategory,
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
                                listOf("Transportasi", "Makan", "Kesehatan", "Akomodasi", "Lainnya").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontFamily = PlusJakartaSans) },
                                        onClick = { formCategory = cat; categoryExpanded = false },
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = formAmount,
                            onValueChange = { formAmount = it.filter { c -> c.isDigit() }; formError = false },
                            label = { Text("Jumlah (Rp)") },
                            prefix = { Text("Rp ") },
                            isError = formError && formAmount.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = formDate,
                            onValueChange = { formDate = it; formError = false },
                            label = { Text("Tanggal (dd MMM yyyy)") },
                            isError = formError && formDate.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Nota/kwitansi dapat dilampirkan setelah pengajuan disetujui.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                        )
                        if (formError) {
                            Text(
                                "Semua kolom wajib diisi",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formTitle.isBlank() || formAmount.isBlank() || formDate.isBlank()) {
                                formError = true
                            } else {
                                vm.submit(
                                    title = formTitle,
                                    category = formCategory,
                                    amount = formAmount.toLongOrNull() ?: 0L,
                                    date = formDate,
                                )
                            }
                        },
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
    }
}






















