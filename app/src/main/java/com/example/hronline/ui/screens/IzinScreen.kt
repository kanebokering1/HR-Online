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

data class IzinItem(
    val id: Int,
    val type: String,
    val date: String,
    val days: Int,
    val reason: String,
    val attachment: String?,
    val status: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinScreen(onBack: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Izin", "Sakit")
    var showForm by remember { mutableStateOf(false) }
    var formType by remember { mutableStateOf("Izin") }
    var typeExpanded by remember { mutableStateOf(false) }
    var formDate by remember { mutableStateOf("") }
    var formDays by remember { mutableStateOf("1") }
    var formReason by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    var allItems by remember {
        mutableStateOf(
            listOf(
                IzinItem(1, "Sakit", "22 Mar 2026", 2, "Demam dan flu", "surat_dokter.pdf", "Disetujui"),
                IzinItem(2, "Izin", "15 Mar 2026", 1, "Acara keluarga", null, "Disetujui"),
                IzinItem(3, "Sakit", "08 Mar 2026", 1, "Sakit gigi", "surat_dokter.pdf", "Disetujui"),
                IzinItem(4, "Izin", "01 Apr 2026", 1, "Mengurus dokumen", null, "Menunggu"),
                IzinItem(5, "Sakit", "25 Feb 2026", 3, "Operasi kecil", "surat_dokter.pdf", "Disetujui"),
            )
        )
    }

    val filteredItems = if (selectedFilter == "Semua") allItems else allItems.filter { it.type == selectedFilter }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Izin / Sakit", onBack = onBack)

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(Icons.Filled.EventBusy, "${allItems.count { it.type == "Izin" }}", "Izin", AccentBlue, AccentBlueLight, Modifier.weight(1f))
            StatCard(Icons.Filled.LocalHospital, "${allItems.count { it.type == "Sakit" }}", "Sakit", AccentRed, AccentRedLight, Modifier.weight(1f))
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
            text = "Ajukan Izin / Sakit",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat")

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
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (item.type == "Sakit") Icons.Filled.LocalHospital else Icons.Filled.EventBusy,
                                    contentDescription = null,
                                    tint = if (item.type == "Sakit") AccentRed else AccentBlue,
                                    modifier = Modifier.size(20.dp),
                                )
                                Column {
                                    Text(item.type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${item.date} (${item.days} hari)", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                }
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
                        Text("Alasan: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        if (item.attachment != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(item.attachment, style = MaterialTheme.typography.labelSmall, color = AccentBlue)
                            }
                        }
                    }
                }
            }
        }

        // ── Form Dialog ─────────────────────────────────────────────
        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = {
                    Text(
                        "Ajukan Izin / Sakit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Jenis izin dropdown
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = formType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jenis") },
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
                                listOf("Izin", "Sakit").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontFamily = PlusJakartaSans) },
                                        onClick = { formType = type; typeExpanded = false },
                                    )
                                }
                            }
                        }
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
                        OutlinedTextField(
                            value = formDays,
                            onValueChange = { formDays = it.filter { c -> c.isDigit() }.ifBlank { "1" } },
                            label = { Text("Jumlah Hari") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = formReason,
                            onValueChange = { formReason = it; formError = false },
                            label = { Text("Keterangan") },
                            minLines = 2,
                            isError = formError && formReason.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (formType == "Sakit") {
                            Text(
                                "Lampiran surat dokter dapat diunggah setelah pengajuan disetujui.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary,
                            )
                        }
                        if (formError) {
                            Text(
                                "Tanggal dan keterangan wajib diisi",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formDate.isBlank() || formReason.isBlank()) {
                                formError = true
                            } else {
                                allItems = allItems + IzinItem(
                                    id = allItems.size + 1,
                                    type = formType,
                                    date = formDate,
                                    days = formDays.toIntOrNull() ?: 1,
                                    reason = formReason,
                                    attachment = null,
                                    status = "Menunggu",
                                )
                                formDate = ""; formDays = "1"; formReason = ""
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
