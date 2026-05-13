package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binahr.ui.components.BinaTopBar
import com.binahr.ui.components.HRListItem
import com.binahr.ui.components.HRCard
import com.binahr.ui.theme.OrangePrimary

/**
 * Pengajuan hub — entry point for Cuti, Lembur, and Reimbursement list/form flows.
 */
@Composable
fun PengajuanScreen(
    onBack: () -> Unit = {},
    onNavigateCuti: () -> Unit = {},
    onNavigateLembur: () -> Unit = {},
    onNavigateReimbursement: () -> Unit = {},
) {
    Scaffold(
        topBar = { BinaTopBar(title = "Pengajuan", onBack = onBack) },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding),
        ) {
            item {
                HRCard(onClick = onNavigateCuti) {
                    HRListItem(
                        icon = Icons.Filled.BeachAccess,
                        title = "Cuti",
                        subtitle = "Lihat saldo & riwayat cuti, ajukan cuti baru",
                        onClick = onNavigateCuti,
                    )
                }
            }
            item {
                HRCard(onClick = onNavigateLembur) {
                    HRListItem(
                        icon = Icons.Filled.AccessTime,
                        title = "Lembur",
                        subtitle = "Lihat & ajukan permintaan lembur",
                        onClick = onNavigateLembur,
                    )
                }
            }
            item {
                HRCard(onClick = onNavigateReimbursement) {
                    HRListItem(
                        icon = Icons.Filled.Receipt,
                        title = "Reimbursement",
                        subtitle = "Ajukan klaim penggantian biaya",
                        onClick = onNavigateReimbursement,
                    )
                }
            }
        }
    }
}

