package com.example.hroes.ui.screens


import com.example.hroes.BuildConfig
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hroes.ui.components.*
import com.example.hroes.ui.theme.*
import com.example.hroes.ui.viewmodel.ProfileViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DataDiriScreen(onBack: () -> Unit, vm: ProfileViewModel = viewModel()) {
    val employee by vm.employee.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    val displayName = employee?.fullName ?: vm.cachedName ?: "Karyawan"

    val sections = listOf(
        "Informasi Pribadi" to listOf(
            "Nama Lengkap" to (employee?.fullName ?: "-"),
            "NIK Karyawan" to (employee?.employeeNumber ?: "-"),
            "NPWP" to (employee?.npwp ?: "-"),
            "BPJS Kesehatan" to (employee?.bpjsKesehatan ?: "-"),
            "BPJS TK" to (employee?.bpjsKetenagakerjaan ?: "-"),
        ),
        "Informasi Kontak" to listOf(
            "No. HP" to (employee?.phone ?: "-"),
            "Alamat" to (employee?.address ?: "-"),
        ),
        "Informasi Pekerjaan" to listOf(
            "Jabatan" to (employee?.positionName ?: "-"),
            "Departemen" to (employee?.departmentName ?: "-"),
            "Tgl Bergabung" to (employee?.hireDate ?: "-"),
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "Data Diri", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Profile header
            HRCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                AvatarImage(initials = displayName.take(2).uppercase(), size = 64.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(employee?.positionName ?: "-", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        StatusBadge(text = "Aktif", type = BadgeType.SUCCESS)
                    }
                }
            }

            sections.forEach { (title, items) ->
                SectionHeader(title = title)
                HRCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        items.forEachIndexed { index, (label, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextTertiary, modifier = Modifier.weight(1f))
                                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f))
                            }
                            if (index < items.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}






















