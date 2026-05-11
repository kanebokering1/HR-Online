package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.AssetViewModel

@Composable
fun AssetScreen(
    onBack: () -> Unit,
    vm: AssetViewModel = viewModel(),
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "active", "returned")
    val filterLabels = mapOf("Semua" to "Semua", "active" to "Aktif", "returned" to "Dikembalikan")

    val assignments by vm.assignments.collectAsState()
    val isLoading   by vm.isLoading.collectAsState()
    val error       by vm.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Aset Saya", onBack = onBack)

            error?.let { msg ->
                InfoCallout(message = msg, type = CalloutType.ERROR, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

        // Status filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filters) { f ->
                FilterChip(
                    selected = f == selectedFilter,
                    onClick = {
                        selectedFilter = f
                        vm.load(if (f == "Semua") null else f)
                    },
                    label = { Text(filterLabels[f] ?: f, fontFamily = PlusJakartaSans) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            assignments.isEmpty() -> {
                EmptyState(
                    title = "Tidak ada aset",
                    subtitle = when (selectedFilter) {
                        "active"   -> "Tidak ada aset yang sedang Anda pegang."
                        "returned" -> "Tidak ada aset yang sudah dikembalikan."
                        else       -> "Belum ada aset yang ditugaskan kepada Anda."
                    },
                )
            }
            else -> {
                Text(
                    "${assignments.size} aset",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(assignments) { a ->
                        val isReturned = a.returnedAt != null
                        HRCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isReturned) SurfaceLight else Green50,
                                            RoundedCornerShape(12.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Inventory2,
                                        contentDescription = null,
                                        tint = if (isReturned) TextTertiary else GreenPrimary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        a.asset?.name ?: "Aset",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    val meta = listOfNotNull(
                                        a.asset?.category,
                                        a.asset?.brand,
                                        a.asset?.assetTag?.let { "Tag: $it" },
                                    ).joinToString(" • ")
                                    if (meta.isNotBlank()) {
                                        Text(meta, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                    }
                                    Text(
                                        "Diterima: ${a.assignedAt?.take(10) ?: "-"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                    )
                                    if (isReturned) {
                                        Text(
                                            "Dikembalikan: ${a.returnedAt?.take(10)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextTertiary,
                                        )
                                    }
                                }
                                StatusBadge(
                                    text = if (isReturned) "Kembali" else "Aktif",
                                    type = if (isReturned) BadgeType.NEUTRAL else BadgeType.SUCCESS,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
