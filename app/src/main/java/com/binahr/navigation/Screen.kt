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
    data object AttendanceMap : Screen("attendance_map/{type}") {
        fun createRoute(type: String) = "attendance_map/$type"
    }
    data object AttendanceFace : Screen("attendance_face/{type}/{lat}/{lon}/{address}") {
        fun createRoute(type: String, lat: Double, lon: Double, address: String) =
            "attendance_face/$type/$lat/$lon/${java.net.URLEncoder.encode(address, "UTF-8")}"
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

    // ── New screens ───────────────────────────────────────────────────────
    data object Pengajuan : Screen("pengajuan")
    data object AjukanCuti : Screen("ajukan_cuti")
    data object AjukanLembur : Screen("ajukan_lembur")
    data object AjukanReimbursement : Screen("ajukan_reimbursement")
    data object CutiDetail : Screen("cuti/{id}") {
        fun createRoute(id: String) = "cuti/$id"
    }
    data object LemburDetail : Screen("lembur/{id}") {
        fun createRoute(id: String) = "lembur/$id"
    }
    data object ReimbursementDetail : Screen("reimbursement/{id}") {
        fun createRoute(id: String) = "reimbursement/$id"
    }
    data object ApprovalDetail : Screen("approvals/{id}") {
        fun createRoute(id: String) = "approvals/$id"
    }
    data object AttendanceCorrection : Screen("attendance_correction")
    data object Recruitment : Screen("recruitment")
    data object PerformanceCycleDetail : Screen("performance/{id}") {
        fun createRoute(id: String) = "performance/$id"
    }
    data object DataDiriEdit : Screen("data_diri_edit")
    data object Success : Screen("success?message={message}&sub={sub}") {
        fun createRoute(message: String, sub: String = "") =
            "success?message=${java.net.URLEncoder.encode(message, "UTF-8")}&sub=${java.net.URLEncoder.encode(sub, "UTF-8")}"
    }
}
