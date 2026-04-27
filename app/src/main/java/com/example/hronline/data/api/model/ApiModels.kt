package com.example.hronline.data.api.model

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════════
// HROES API Models
// ═══════════════════════════════════════════════════════════════════

// ── Auth ─────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String = "hroes-android",
)

/** Matches Laravel response: { token, user: {...} } */
data class LoginApiResponse(
    val token: String,
    val user: UserDto,
)

// ── User (from /auth/me) ──────────────────────────────────────────────

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("email_verified_at") val emailVerifiedAt: String?,
)

// ── Attendance ────────────────────────────────────────────────────────

data class CheckInRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val selfieBase64: String?,     // Optional face-verify image
)

data class CheckOutRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val notes: String? = null,
)

data class AttendanceRecord(
    val id: String,
    val date: String,              // yyyy-MM-dd
    val checkIn: String?,          // HH:mm
    val checkOut: String?,
    val status: String,            // "Hadir" | "Terlambat" | "Izin" | "Sakit" | "Alpha"
    val workHours: Double,
    val location: String,
)

// ── Leave (Cuti) ──────────────────────────────────────────────────────

data class SubmitLeaveRequest(
    val type: String,              // "Cuti Tahunan" | "Cuti Sakit" | "Cuti Besar" | "Cuti Melahirkan"
    val startDate: String,         // yyyy-MM-dd
    val endDate: String,
    val reason: String,
    val attachmentUrl: String? = null,
)

data class LeaveRecord(
    val id: String,
    val type: String,
    val startDate: String,
    val endDate: String,
    val days: Int,
    val reason: String,
    val status: String,            // "Menunggu" | "Disetujui" | "Ditolak"
    val approvedBy: String?,
    val approvedAt: String?,
)

data class LeaveBalanceResponse(
    val annual: Int,               // Sisa cuti tahunan
    val sick: Int,
    val total: Int,
)

// ── Overtime (Lembur) ─────────────────────────────────────────────────

data class SubmitOvertimeRequest(
    val date: String,              // yyyy-MM-dd
    val startTime: String,         // HH:mm
    val endTime: String,
    val reason: String,
)

data class OvertimeRecord(
    val id: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val hours: Double,
    val reason: String,
    val status: String,
)

// ── Permission / Sick (Izin) ──────────────────────────────────────────

data class SubmitPermissionRequest(
    val type: String,              // "Izin" | "Sakit"
    val date: String,
    val days: Int,
    val reason: String,
    val attachmentUrl: String? = null,
)

data class PermissionRecord(
    val id: String,
    val type: String,
    val date: String,
    val days: Int,
    val reason: String,
    val attachment: String?,
    val status: String,
)

// ── Reimbursement ─────────────────────────────────────────────────────

data class SubmitReimbursementRequest(
    val title: String,
    val category: String,          // "Transportasi" | "Makan" | "Kesehatan" | "Akomodasi" | "Lainnya"
    val amount: Long,
    val date: String,              // yyyy-MM-dd
    val receiptUrl: String? = null,
)

data class ReimbursementRecord(
    val id: String,
    val title: String,
    val category: String,
    val amount: Long,
    val date: String,
    val receiptUrl: String?,
    val status: String,
)

// ── Payslip (Slip Gaji) ───────────────────────────────────────────────

data class PayslipSummary(
    val id: String,
    val period: String,            // "Maret 2026"
    val basicSalary: Long,
    val totalAllowance: Long,
    val totalDeduction: Long,
    val netSalary: Long,
    val paidAt: String?,
    val downloadUrl: String?,
)

// ── Announcement ──────────────────────────────────────────────────────

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val publishedAt: String,
    val isRead: Boolean,
)

// ── Notification ──────────────────────────────────────────────────────

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: String,              // "approval" | "payslip" | "general"
    val isRead: Boolean,
    val createdAt: String,
)

// ── Generic Responses ─────────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
)

// ═══════════════════════════════════════════════════════════════════
// SERVER-SIDE DTOs — match Laravel JSON responses exactly
// ═══════════════════════════════════════════════════════════════════

/** Laravel paginate() wrapper */
data class LaravelPaginated<T>(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<T>,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int,
)

/** Employee record from /v1/employees */
data class EmployeeDto(
    val id: String,
    @SerializedName("company_id")    val companyId: String,
    @SerializedName("user_id")       val userId: String?,
    @SerializedName("employee_number") val employeeNumber: String,
    @SerializedName("full_name")     val fullName: String,
    @SerializedName("birth_date")    val birthDate: String?,
    val gender: String?,
    @SerializedName("marital_status") val maritalStatus: String?,
    @SerializedName("employment_status") val employmentStatus: String,
    @SerializedName("hire_date")     val hireDate: String?,
    @SerializedName("department_id") val departmentId: String?,
    @SerializedName("position_id")   val positionId: String?,
)

/** Company record from /v1/companies */
data class CompanyDto(
    val id: String,
    val name: String,
    val code: String,
    val address: String?,
)

/** Attendance log from /v1/attendance/logs */
data class AttendanceLogDto(
    val id: String,
    @SerializedName("employee_id")   val employeeId: String,
    @SerializedName("clock_in_at")   val clockInAt: String?,
    @SerializedName("clock_out_at")  val clockOutAt: String?,
    val status: String?,
    @SerializedName("minutes_late")  val minutesLate: Int,
    val source: String,
    val latitude: Double?,
    val longitude: Double?,
)

/** Leave application from /v1/leave/applications */
data class LeaveApplicationDto(
    val id: String,
    @SerializedName("employee_id")   val employeeId: String,
    @SerializedName("leave_type")    val leaveType: String,
    @SerializedName("start_date")    val startDate: String,
    @SerializedName("end_date")      val endDate: String,
    @SerializedName("total_days")    val totalDays: Double,
    @SerializedName("approval_state") val approvalState: String,
    val reason: String?,
    @SerializedName("approved_at")   val approvedAt: String?,
)

/** Overtime request from /v1/overtime/requests */
data class OvertimeRequestDto(
    val id: String,
    @SerializedName("employee_id")   val employeeId: String,
    @SerializedName("overtime_date") val overtimeDate: String,
    val hours: Double,
    val reason: String?,
    @SerializedName("approval_state") val approvalState: String,
    @SerializedName("approved_at")   val approvedAt: String?,
)

/** Reimbursement claim from /v1/reimbursements */
data class ReimbursementClaimDto(
    val id: String,
    @SerializedName("employee_id")   val employeeId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    @SerializedName("approval_state") val approvalState: String,
    @SerializedName("approved_at")   val approvedAt: String?,
    @SerializedName("paid_at")       val paidAt: String?,
)

/** Payroll slip from /v1/payroll/slips */
data class PayrollSlipDto(
    val id: String,
    @SerializedName("payroll_run_id") val payrollRunId: String,
    @SerializedName("employee_id")   val employeeId: String,
    val lines: List<Map<String, Any>>,
    @SerializedName("gross_salary")  val grossSalary: Double,
    @SerializedName("net_salary")    val netSalary: Double,
    @SerializedName("pph21_ter_amount") val pph21TerAmount: Double,
    @SerializedName("bpjs_employee_amount") val bpjsEmployeeAmount: Double,
    @SerializedName("bpjs_company_amount")  val bpjsCompanyAmount: Double,
)

