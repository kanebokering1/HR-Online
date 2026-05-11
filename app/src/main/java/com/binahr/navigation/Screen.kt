package com.binahr.navigation


import com.binahr.BuildConfig
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object TenantSetup : Screen("tenant_setup")
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Attendance : Screen("attendance/{type}") {
        fun createRoute(type: String) = "attendance/$type"
    }
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object DataDiri : Screen("data_diri")
    data object Kalender : Screen("kalender")
    data object Cuti : Screen("cuti")
    data object SlipGaji : Screen("slip_gaji")
    data object Pengumuman : Screen("pengumuman")
    data object Lembur : Screen("lembur")
    data object Notifikasi : Screen("notifikasi")
    data object UbahPassword : Screen("ubah_password")
    data object Reimbursement : Screen("reimbursement")
    data object StrukturOrg : Screen("struktur_org")
    data object Dokumen : Screen("dokumen")
    data object FAQ : Screen("faq")
    data object Approvals : Screen("approvals")
    data object Performance : Screen("performance")
    data object JadwalShift : Screen("jadwal_shift")
    data object Asset : Screen("asset")
}
