package com.example.hronline.ui.screens

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
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*

data class ReimbursementItem(
    val id: Int,
    val title: String,
    val date: String,
    val amount: Long,
    val category: String,
    val receipt: String?,
    val status: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReimbursementScreen(onBack: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Transportasi", "Makan", "Kesehatan", "Lainnya")
    var showForm by remember { mutableStateOf(false) }
    var formTitle by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("Transportasi") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var formAmount by remember { mutableStateOf("") }
    var formDate by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    var allItems by remember {
        mutableStateOf(
            listOf(
                ReimbursementItem(1, "Taksi ke klien PT ABC", "20 Mar 2026", 150_000, "Transportasi", "receipt_001.jpg", "Disetujui"),
                ReimbursementItem(2, "Makan siang meeting", "18 Mar 2026", 250_000, "Makan", "receipt_002.jpg", "Disetujui"),
                ReimbursementItem(3, "Medical check-up", "15 Mar 2026", 500_000, "Kesehatan", "receipt_003.jpg", "Menunggu"),
                ReimbursementItem(4, "Grab ke bandara", "10 Mar 2026", 200_000, "Transportasi", "receipt_004.jpg", "Ditolak"),
                ReimbursementItem(5, "Alat tulis kantor", "05 Mar 2026", 75_000, "Lainnya", null, "Disetujui"),
            )
        )
    }

    val filteredItems = if (selectedFilter == "Semua") allItems else allItems.filter { it.category == selectedFilter }
    val totalApproved = allItems.filter { it.status == "Disetujui" }.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Reimbursement", onBack = onBack)

        // Total card
        HRCard(
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Reimbursement Disetujui", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Text(
                    "Rp ${String.format("%,d", totalApproved)}",
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
                                Text(item.date, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            StatusBadge(
                                text = item.status,
                                type = when (item.status) {
                                    "Disetujui" -> BadgeType.SUCCESS
                                    "Ditolak" -> BadgeType.ERROR
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
                                StatusBadge(text = item.category, type = BadgeType.NEUTRAL)
                                if (item.receipt != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentBlue)
                                }
                            }
                            Text(
                                "Rp ${String.format("%,d", item.amount)}",
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
                                allItems = allItems + ReimbursementItem(
                                    id = allItems.size + 1,
                                    title = formTitle,
                                    date = formDate,
                                    amount = formAmount.toLongOrNull() ?: 0L,
                                    category = formCategory,
                                    receipt = null,
                                    status = "Menunggu",
                                )
                                formTitle = ""; formAmount = ""; formDate = ""
                                formError = false
                                showForm = false
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
