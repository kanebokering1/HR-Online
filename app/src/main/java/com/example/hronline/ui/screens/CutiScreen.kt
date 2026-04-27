package com.example.hronline.ui.screens

import androidx.compose.foundation.background
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
import com.example.hronline.ui.components.*
import com.example.hronline.ui.theme.*
data class CutiItem(
    val id: Int,
    val type: String,
    val startDate: String,
    val endDate: String,
    val days: Int,
    val reason: String,
    val status: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutiScreen(onBack: () -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var formType by remember { mutableStateOf("Cuti Tahunan") }
    var formStart by remember { mutableStateOf("") }
    var formEnd by remember { mutableStateOf("") }
    var formReason by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf(false) }

    var cutiList by remember {
        mutableStateOf(
            listOf(
                CutiItem(1, "Cuti Tahunan", "15 Mar 2026", "17 Mar 2026", 3, "Liburan keluarga", "Disetujui"),
                CutiItem(2, "Cuti Tahunan", "10 Jun 2026", "10 Jun 2026", 1, "Urusan pribadi", "Menunggu"),
                CutiItem(3, "Cuti Besar", "01 Des 2025", "10 Des 2025", 10, "Mudik lebaran", "Disetujui"),
                CutiItem(4, "Cuti Tahunan", "20 Jul 2026", "21 Jul 2026", 2, "Acara keluarga", "Ditolak"),
            )
        )
    }

    val sisaCuti = 9

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Pengajuan Cuti", onBack = onBack)

        // Sisa Cuti Card
        HRCard(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Sisa Cuti Tahunan", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$sisaCuti", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        Text(" / 12 hari", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    }
                }
                LinearProgressIndicator(
                    progress = { sisaCuti / 12f },
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp),
                    color = GreenPrimary,
                    trackColor = Green50,
    
                )
            }
        }

        // Action Button
        HRButton(
            text = "Ajukan Cuti Baru",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(title = "Riwayat Pengajuan")

        // Cuti List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(cutiList) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(item.type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextTertiary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${item.startDate} - ${item.endDate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(${item.days} hari)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = AccentBlue)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Alasan: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        }

        // ── Form Dialog ─────────────────────────────────────────
        if (showForm) {
            AlertDialog(
                onDismissRequest = { showForm = false },
                title = {
                    Text(
                        "Ajukan Cuti Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Jenis Cuti Dropdown
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = formType,
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
                                listOf("Cuti Tahunan", "Cuti Sakit", "Cuti Besar", "Cuti Melahirkan").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontFamily = PlusJakartaSans) },
                                        onClick = { formType = type; typeExpanded = false },
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = formStart,
                            onValueChange = { formStart = it; formError = false },
                            label = { Text("Tanggal Mulai (dd MMM yyyy)") },
                            isError = formError && formStart.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = formEnd,
                            onValueChange = { formEnd = it },
                            label = { Text("Tanggal Selesai (dd MMM yyyy)") },
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
                            label = { Text("Alasan / Keterangan") },
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
                                "Tanggal mulai dan alasan wajib diisi",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formStart.isBlank() || formReason.isBlank()) {
                                formError = true
                            } else {
                                cutiList = cutiList + CutiItem(
                                    id = cutiList.size + 1,
                                    type = formType,
                                    startDate = formStart,
                                    endDate = formEnd.ifBlank { formStart },
                                    days = 1,
                                    reason = formReason,
                                    status = "Menunggu",
                                )
                                formStart = ""; formEnd = ""; formReason = ""
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
