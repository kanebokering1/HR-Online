package com.example.hronline.ui.screens

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

data class LemburItem(
    val id: Int,
    val date: String,
    val startTime: String,
    val endTime: String,
    val hours: Double,
    val reason: String,
    val status: String,
)

@Composable
fun LemburScreen(onBack: () -> Unit) {
    var showForm by remember { mutableStateOf(false) }
    var formDate by remember { mutableStateOf("") }
    var formStart by remember { mutableStateOf("") }
    var formEnd by remember { mutableStateOf("") }
    var formReason by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf(false) }

    var items by remember {
        mutableStateOf(
            listOf(
                LemburItem(1, "25 Mar 2026", "17:00", "20:00", 3.0, "Deadline project client A", "Disetujui"),
                LemburItem(2, "20 Mar 2026", "17:00", "21:00", 4.0, "Perbaikan bug production", "Disetujui"),
                LemburItem(3, "15 Mar 2026", "17:00", "19:00", 2.0, "Migrasi database", "Menunggu"),
                LemburItem(4, "10 Mar 2026", "17:00", "22:00", 5.0, "Deploy sistem baru", "Ditolak"),
            )
        )
    }

    val totalHours = items.filter { it.status == "Disetujui" }.sumOf { it.hours }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Lembur", onBack = onBack)

        // Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(Icons.Filled.AccessTime, "${totalHours.toInt()}", "Jam Lembur", AccentOrange, AccentOrangeLight, Modifier.weight(1f))
            StatCard(Icons.Filled.PendingActions, "${items.count { it.status == "Menunggu" }}", "Pending", AccentAmber, AccentAmberLight, Modifier.weight(1f))
        }

        HRButton(
            text = "Ajukan Lembur",
            onClick = { showForm = true },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Riwayat Lembur")

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items) { item ->
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(item.date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${item.startTime} - ${item.endTime} (${item.hours.toInt()} jam)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                        Text("Alasan: ${item.reason}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }
        }

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
                        OutlinedTextField(
                            value = formDate,
                            onValueChange = { formDate = it; formError = false },
                            label = { Text("Tanggal Lembur (dd MMM yyyy)") },
                            isError = formError && formDate.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                focusedLabelColor = GreenPrimary,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formStart,
                                onValueChange = { formStart = it; formError = false },
                                label = { Text("Jam Mulai") },
                                placeholder = { Text("17:00") },
                                isError = formError && formStart.isBlank(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = formEnd,
                                onValueChange = { formEnd = it },
                                label = { Text("Jam Selesai") },
                                placeholder = { Text("20:00") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    focusedLabelColor = GreenPrimary,
                                ),
                                modifier = Modifier.weight(1f),
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
                            if (formDate.isBlank() || formStart.isBlank() || formReason.isBlank()) {
                                formError = true
                            } else {
                                val startH = formStart.split(":").getOrNull(0)?.toIntOrNull() ?: 0
                                val endH = formEnd.split(":").getOrNull(0)?.toIntOrNull() ?: (startH + 1)
                                val hours = (endH - startH).toDouble().coerceAtLeast(0.5)
                                items = items + LemburItem(
                                    id = items.size + 1,
                                    date = formDate,
                                    startTime = formStart,
                                    endTime = formEnd.ifBlank { "${startH + 1}:00" },
                                    hours = hours,
                                    reason = formReason,
                                    status = "Menunggu",
                                )
                                formDate = ""; formStart = ""; formEnd = ""; formReason = ""
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
