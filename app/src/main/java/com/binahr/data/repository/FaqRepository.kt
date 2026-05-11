package com.binahr.data.repository

import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.FaqItemDto

class FaqRepository {

    suspend fun getFaq(): Result<List<FaqItemDto>> = try {
        val envelope = ApiConfig.apiService.faq()
        if (envelope.success) {
            Result.success(envelope.data ?: hardcodedFallback())
        } else {
            Result.failure(Exception(envelope.message ?: "Gagal memuat FAQ"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    companion object {
        fun hardcodedFallback(): List<FaqItemDto> = listOf(
            FaqItemDto(1, "Absensi", "Bagaimana cara melakukan check-in?", "Buka aplikasi → tap tombol Check-In di dashboard → izinkan akses lokasi → konfirmasi."),
            FaqItemDto(2, "Absensi", "Apa yang harus dilakukan jika lupa check-out?", "Ajukan koreksi absensi melalui menu Absensi → Koreksi dalam 1×24 jam."),
            FaqItemDto(3, "Cuti", "Bagaimana cara mengajukan cuti?", "Buka menu Cuti → Ajukan Cuti → pilih jenis dan tanggal → isi alasan → kirim."),
            FaqItemDto(4, "Cuti", "Berapa kuota cuti tahunan?", "Setiap karyawan tetap mendapat 12 hari cuti tahunan sesuai UU Ketenagakerjaan."),
            FaqItemDto(5, "Gaji", "Kapan slip gaji tersedia?", "Slip gaji tersedia setiap bulan setelah proses payroll disetujui oleh HRD."),
            FaqItemDto(6, "Keuangan", "Bagaimana cara mengajukan reimbursement?", "Buka menu Reimburse → Ajukan → isi detail → upload bukti → kirim."),
            FaqItemDto(7, "Lembur", "Bagaimana kebijakan lembur?", "Lembur harus diajukan dan disetujui atasan terlebih dahulu."),
            FaqItemDto(8, "Akun", "Bagaimana cara mengubah password?", "Buka Profil → Ubah Password → masukkan password lama dan baru → simpan."),
            FaqItemDto(9, "Dokumen", "Di mana saya bisa melihat dokumen kerja saya?", "Buka menu Dokumen untuk melihat kontrak, SK, dan dokumen lainnya."),
            FaqItemDto(10, "Umum", "Siapa yang harus dihubungi untuk masalah teknis?", "Hubungi tim HR atau IT Support perusahaan Anda."),
        )
    }
}
