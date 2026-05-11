package com.binahr.ui.screens


import com.binahr.BuildConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.FaqViewModel

@Composable
fun FAQScreen(
    onBack: () -> Unit,
    vm: FaqViewModel = viewModel(),
) {
    var expandedId by remember { mutableIntStateOf(-1) }

    val faqItems  by vm.items.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "FAQ & Bantuan", onBack = onBack)

        // Contact card
        HRCard(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AccentBlueLight, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Butuh Bantuan?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Hubungi tim HR perusahaan Anda", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        SectionHeader(title = "Pertanyaan Umum")

        if (isLoading && faqItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(faqItems) { item ->
                    HRCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedId = if (expandedId == item.id) -1 else item.id },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    StatusBadge(text = item.category, type = BadgeType.NEUTRAL)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        item.question,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Icon(
                                    if (expandedId == item.id) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                )
                            }
                            AnimatedVisibility(visible = expandedId == item.id) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    HorizontalDivider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        item.answer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FAQItem(val id: Int, val question: String, val answer: String, val category: String)

@Composable
fun FAQScreen(onBack: () -> Unit) {
    var expandedId by remember { mutableIntStateOf(-1) }

    val faqItems = remember {
        listOf(
            FAQItem(1, "Bagaimana cara melakukan check in?", "Buka aplikasi → Tap tombol 'Check In' di dashboard → Izinkan akses lokasi → Verifikasi wajah → Konfirmasi → Selesai.", "Absensi"),
            FAQItem(2, "Apa yang harus dilakukan jika lupa check out?", "Hubungi HRD atau atasan langsung Anda untuk melakukan koreksi absensi. Koreksi harus dilakukan dalam waktu 1x24 jam.", "Absensi"),
            FAQItem(3, "Bagaimana cara mengajukan cuti?", "Buka menu 'Cuti' → Tap 'Ajukan Cuti Baru' → Pilih tanggal dan jenis cuti → Isi alasan → Submit. Pengajuan akan diterima oleh atasan.", "Cuti"),
            FAQItem(4, "Berapa kuota cuti tahunan?", "Setiap karyawan tetap mendapat 12 hari cuti tahunan. Cuti yang tidak digunakan akan hangus di akhir tahun.", "Cuti"),
            FAQItem(5, "Kapan slip gaji tersedia?", "Slip gaji tersedia setiap tanggal 25 setiap bulannya. Jika tanggal 25 jatuh pada hari libur, slip gaji akan tersedia pada hari kerja sebelumnya.", "Gaji"),
            FAQItem(6, "Bagaimana cara mengajukan reimbursement?", "Buka menu 'Reimburse' → Tap 'Ajukan Reimbursement' → Isi detail pengeluaran → Upload bukti/receipt → Submit.", "Keuangan"),
            FAQItem(7, "Bagaimana cara mengubah password?", "Buka Profil → Pengaturan → Ubah Password → Masukkan password lama dan baru → Simpan.", "Akun"),
            FAQItem(8, "Siapa yang harus dihubungi untuk masalah teknis?", "Hubungi tim IT Support melalui email it.support@xyz.co.id atau telepon ext. 1234.", "Umum"),
            FAQItem(9, "Bagaimana kebijakan lembur?", "Lembur harus diajukan terlebih dahulu dan disetujui atasan. Perhitungan upah lembur sesuai UU Ketenagakerjaan.", "Lembur"),
            FAQItem(10, "Dimana saya bisa melihat dokumen kerja saya?", "Buka menu 'Dokumen' untuk melihat kontrak kerja, sertifikat, SK, dan dokumen lainnya.", "Umum"),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GradientTopBar(title = "FAQ & Bantuan", onBack = onBack)

        // Contact card
        HRCard(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AccentBlueLight, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Butuh Bantuan?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Hubungi HRD: hr@xyz.co.id", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("IT Support: it.support@xyz.co.id", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        SectionHeader(title = "Pertanyaan Umum")

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(faqItems) { item ->
                HRCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedId = if (expandedId == item.id) -1 else item.id },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                StatusBadge(text = item.category, type = BadgeType.NEUTRAL)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    item.question,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Icon(
                                if (expandedId == item.id) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = TextTertiary,
                            )
                        }
                        AnimatedVisibility(visible = expandedId == item.id) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                HorizontalDivider(color = DividerColor)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.answer,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}






















