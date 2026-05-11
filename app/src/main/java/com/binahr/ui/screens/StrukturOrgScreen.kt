package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.StrukturOrgViewModel

@Composable
fun StrukturOrgScreen(
    onBack: () -> Unit,
    vm: StrukturOrgViewModel = viewModel(),
) {
    val departments by vm.departments.collectAsState()
    val isLoading   by vm.isLoading.collectAsState()
    val error       by vm.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Struktur Organisasi", onBack = onBack)

            error?.let { msg ->
                InfoCallout(message = msg, type = CalloutType.ERROR, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            departments.isEmpty() -> {
                EmptyState(
                    title = "Struktur organisasi kosong",
                    subtitle = "Belum ada data departemen dari server.",
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(departments) { dept ->
                        HRCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(AccentBlueLight, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Filled.Business, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(dept.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                            if (!dept.code.isNullOrBlank()) {
                                                Text(dept.code, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                            }
                                        }
                                    }
                                    StatusBadge(text = "${dept.employeeCount} karyawan", type = BadgeType.INFO)
                                }

                                if (dept.positions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = SurfaceLight)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Jabatan", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    dept.positions.forEach { pos ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .background(SurfaceLight, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(Icons.Filled.Person, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(pos.name, style = MaterialTheme.typography.bodySmall)
                                            if (!pos.code.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("(${pos.code})", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class OrgNode(
    val name: String,
    val position: String,
    val department: String,
    val level: Int,
    val initials: String,
)

@Composable
fun StrukturOrgScreenPreview() {
    // This is a placeholder for the preview or additional screen content
}






















