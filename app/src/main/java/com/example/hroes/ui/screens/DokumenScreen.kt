package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
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
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.DokumenViewModel
import kotlinx.coroutines.launch

@Composable
fun DokumenScreen(
    onBack: () -> Unit,
    vm: DokumenViewModel = viewModel(),
) {
    var selectedCategory by remember { mutableStateOf("Semua") }
    val categories = listOf("Semua", "Kontrak", "Identitas", "SK", "Sertifikat", "Slip Gaji", "Lainnya")

    val documents  by vm.documents.collectAsState()
    val isLoading  by vm.isLoading.collectAsState()
    val error      by vm.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredDocs = if (selectedCategory == "Semua") documents
                       else documents.filter { it.category == selectedCategory }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GradientTopBar(title = "Dokumen Saya", onBack = onBack)

            error?.let { msg ->
                InfoCallout(message = msg, type = CalloutType.ERROR, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // Category filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontFamily = PlusJakartaSans) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (filteredDocs.isEmpty()) {
                EmptyState(
                    title = "Tidak ada dokumen",
                    subtitle = if (selectedCategory == "Semua") "Belum ada dokumen yang diunggah oleh HRD."
                               else "Tidak ada dokumen kategori $selectedCategory.",
                )
            } else {
                Text(
                    "${filteredDocs.size} dokumen",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredDocs) { doc ->
                        val (icon, iconColor, iconBg) = when (doc.category) {
                            "Kontrak"  -> Triple(Icons.Filled.Handshake, AccentBlue, AccentBlueLight)
                            "Sertifikat" -> Triple(Icons.Filled.WorkspacePremium, AccentAmber, AccentAmberLight)
                            "SK"       -> Triple(Icons.Filled.Gavel, AccentPurple, AccentPurpleLight)
                            "Slip Gaji" -> Triple(Icons.Filled.RequestPage, GreenPrimary, Green50)
                            "Identitas" -> Triple(Icons.Filled.Badge, AccentOrange, AccentOrangeLight)
                            else       -> Triple(Icons.Filled.Description, TextTertiary, SurfaceLight)
                        }
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
                                        .background(iconBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        doc.name ?: doc.documentType ?: "Dokumen",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "${doc.category ?: "Lainnya"} • ${doc.createdAt ?: ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Fitur unduh segera hadir")
                                    }
                                }) {
                                    Icon(Icons.Filled.Lock, contentDescription = "Unduh tidak tersedia", tint = TextTertiary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DokumenItem(
    val id: Int,
    val name: String,
    val type: String,
    val size: String,
    val date: String,
    val category: String,
)























