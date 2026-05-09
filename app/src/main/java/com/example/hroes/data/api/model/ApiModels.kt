package com.example.hroes.data.api.model


import com.example.hroes.BuildConfig
import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════════════
// SERVER RESPONSE ENVELOPE
// All API responses: { success, message, data, meta, errors }
// ═══════════════════════════════════════════════════════════════════

data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val meta: Any?,
    val errors: Any?,
)

/** Laravel paginate() wrapper — returned by list endpoints */
data class LaravelPaginated<T>(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<T>,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int,
)

// ═══════════════════════════════════════════════════════════════════
// AUTH
// ═══════════════════════════════════════════════════════════════════

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String = "hroes-android",
)

data class GoogleLoginRequest(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("device_name") val deviceName: String = "hroes-android-google",
)

data class LoginApiResponse(
    val token: String,
    val user: UserDto,
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerializedName("email_verified_at") val emailVerifiedAt: String?,
)

// ═══════════════════════════════════════════════════════════════════
// DASHBOARD
// ═══════════════════════════════════════════════════════════════════

data class DashboardSummaryDto(
    @SerializedName("attendance_today")  val attendanceToday: AttendanceTodayDto?,
    @SerializedName("leave_balance")     val leaveBalance: Int,
    @SerializedName("pending_approvals") val pendingApprovals: Int,
    @SerializedName("overtime_this_month") val overtimeThisMonth: Double,
    @SerializedName("unread_notifications") val unreadNotifications: Int,
)

data class AttendanceTodayDto(
    @SerializedName("clock_in_at")  val clockInAt: String?,
    @SerializedName("clock_out_at") val clockOutAt: String?,
    val status: String?,
)

// ═══════════════════════════════════════════════════════════════════
// EMPLOYEE
// ═══════════════════════════════════════════════════════════════════

data class EmployeeDto(
    val id: String,
    @SerializedName("company_id")       val companyId: String,
    @SerializedName("user_id")          val userId: String?,
    @SerializedName("employee_number")  val employeeNumber: String,
    @SerializedName("full_name")        val fullName: String,
    @SerializedName("birth_date")       val birthDate: String?,
    val gender: String?,
    @SerializedName("marital_status")   val maritalStatus: String?,
    @SerializedName("employment_status") val employmentStatus: String,
    @SerializedName("hire_date")        val hireDate: String?,
    @SerializedName("department_id")    val departmentId: String?,
    @SerializedName("position_id")      val positionId: String?,
)

data class EmployeeDetailDto(
    val id: String,
    @SerializedName("company_id")       val companyId: String,
    @SerializedName("user_id")          val userId: String?,
    @SerializedName("employee_number")  val employeeNumber: String,
    @SerializedName("full_name")        val fullName: String,
    @SerializedName("birth_date")       val birthDate: String?,
    val gender: String?,
    val religion: String?,
    @SerializedName("marital_status")   val maritalStatus: String?,
    @SerializedName("employment_status") val employmentStatus: String,
    @SerializedName("hire_date")        val hireDate: String?,
    val phone: String?,
    val address: String?,
    @SerializedName("department_name")  val departmentName: String?,
    @SerializedName("position_name")    val positionName: String?,
    @SerializedName("photo_url")        val photoUrl: String?,
    val npwp: String?,
    val bpjsKesehatan: String?,
    val bpjsKetenagakerjaan: String?,
)

data class CompanyDto(
    val id: String,
    val name: String,
    val code: String,
    val address: String?,
)

// ═══════════════════════════════════════════════════════════════════
// ATTENDANCE
// ═══════════════════════════════════════════════════════════════════

data class AttendanceLogDto(
    val id: String,
    @SerializedName("employee_id")  val employeeId: String,
    @SerializedName("clock_in_at")  val clockInAt: String?,
    @SerializedName("clock_out_at") val clockOutAt: String?,
    val status: String?,
    @SerializedName("minutes_late") val minutesLate: Int,
    val source: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("check_in_address")  val checkInAddress: String?,
    @SerializedName("check_out_address") val checkOutAddress: String?,
)

data class CheckInRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    @SerializedName("selfie_base64") val selfieBase64: String? = null,
)

data class CheckOutRequest(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val notes: String? = null,
)

data class AttendanceCorrectionRequest(
    val date: String,
    @SerializedName("correct_clock_in")  val correctClockIn: String?,
    @SerializedName("correct_clock_out") val correctClockOut: String?,
    val reason: String,
)

// ═══════════════════════════════════════════════════════════════════
// LEAVE (CUTI)
// ═══════════════════════════════════════════════════════════════════

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
    @SerializedName("created_at")    val createdAt: String?,
)

data class SubmitLeaveApiRequest(
    @SerializedName("leave_type") val leaveType: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date")   val endDate: String,
    val reason: String,
    @SerializedName("attachment_url") val attachmentUrl: String? = null,
)

data class LeaveBalanceDto(
    val id: String,
    @SerializedName("leave_type") val leaveType: String,
    @SerializedName("allocated_days") val allocatedDays: Double,
    @SerializedName("used_days")      val usedDays: Double,
    @SerializedName("remaining_days") val remainingDays: Double,
    val year: Int,
)

// ═══════════════════════════════════════════════════════════════════
// OVERTIME (LEMBUR)
// ═══════════════════════════════════════════════════════════════════

data class OvertimeRequestDto(
    val id: String,
    @SerializedName("employee_id")   val employeeId: String,
    @SerializedName("overtime_date") val overtimeDate: String,
    @SerializedName("start_time")    val startTime: String?,
    @SerializedName("end_time")      val endTime: String?,
    val hours: Double,
    val reason: String?,
    @SerializedName("approval_state") val approvalState: String,
    @SerializedName("approved_at")   val approvedAt: String?,
    @SerializedName("created_at")    val createdAt: String?,
)

data class SubmitOvertimeApiRequest(
    @SerializedName("overtime_date") val overtimeDate: String,
    @SerializedName("start_time")    val startTime: String,
    @SerializedName("end_time")      val endTime: String,
    val reason: String,
)

// ═══════════════════════════════════════════════════════════════════
// REIMBURSEMENT
// ═══════════════════════════════════════════════════════════════════

data class ReimbursementClaimDto(
    val id: String,
    @SerializedName("employee_id")   val employeeId: String,
    val title: String,
    val amount: Double,
    val currency: String,
    @SerializedName("approval_state") val approvalState: String,
    @SerializedName("approved_at")   val approvedAt: String?,
    @SerializedName("paid_at")       val paidAt: String?,
    @SerializedName("submitted_at")  val submittedAt: String?,
    @SerializedName("created_at")    val createdAt: String?,
)

data class CreateReimbursementRequest(
    val title: String,
    val notes: String? = null,
)

data class ReimbursementItemRequest(
    val description: String,
    val amount: Long,
    val date: String,
    val category: String,
    @SerializedName("receipt_url") val receiptUrl: String? = null,
)

// ═══════════════════════════════════════════════════════════════════
// PAYROLL
// ═══════════════════════════════════════════════════════════════════

data class PayrollSlipDto(
    val id: String,
    @SerializedName("payroll_run_id") val payrollRunId: String,
    @SerializedName("employee_id")    val employeeId: String,
    @SerializedName("period_label")   val periodLabel: String?,
    @SerializedName("gross_salary")   val grossSalary: Double,
    @SerializedName("net_salary")     val netSalary: Double,
    @SerializedName("pph21_ter_amount")       val pph21TerAmount: Double,
    @SerializedName("bpjs_employee_amount")   val bpjsEmployeeAmount: Double,
    @SerializedName("bpjs_company_amount")    val bpjsCompanyAmount: Double,
    @SerializedName("created_at")     val createdAt: String?,
)

data class PayrollSlipDetailDto(
    val id: String,
    @SerializedName("payroll_run_id") val payrollRunId: String,
    @SerializedName("employee_id")    val employeeId: String,
    @SerializedName("period_label")   val periodLabel: String?,
    @SerializedName("gross_salary")   val grossSalary: Double,
    @SerializedName("net_salary")     val netSalary: Double,
    @SerializedName("pph21_ter_amount")       val pph21TerAmount: Double,
    @SerializedName("bpjs_employee_amount")   val bpjsEmployeeAmount: Double,
    @SerializedName("bpjs_company_amount")    val bpjsCompanyAmount: Double,
    val lines: List<PayrollLineDto>,
    @SerializedName("created_at")     val createdAt: String?,
)

data class PayrollLineDto(
    val label: String,
    val amount: Double,
    val type: String,   // "earning" | "deduction"
)

// ═══════════════════════════════════════════════════════════════════
// CALENDAR & HOLIDAYS
// ═══════════════════════════════════════════════════════════════════

data class HolidayDto(
    val id: String,
    val date: String,
    val name: String,
    @SerializedName("is_national") val isNational: Boolean,
)

// ═══════════════════════════════════════════════════════════════════
// SCHEDULING / SHIFT
// ═══════════════════════════════════════════════════════════════════

data class ShiftAssignmentDto(
    val id: String,
    val date: String,
    @SerializedName("shift_name")  val shiftName: String,
    @SerializedName("start_time")  val startTime: String,
    @SerializedName("end_time")    val endTime: String,
    val location: String?,
)

// ═══════════════════════════════════════════════════════════════════
// APPROVALS
// ═══════════════════════════════════════════════════════════════════

data class ApprovalDto(
    val id: String,
    @SerializedName("approvable_type")  val approvableType: String,
    @SerializedName("approvable_id")    val approvableId: String,
    val title: String?,
    val status: String,
    @SerializedName("current_step")     val currentStep: Int,
    @SerializedName("created_at")       val createdAt: String?,
    @SerializedName("requester_name")   val requesterName: String?,
)

data class ApprovalDetailDto(
    val id: String,
    @SerializedName("approvable_type")  val approvableType: String,
    @SerializedName("approvable_id")    val approvableId: String,
    val title: String?,
    val status: String,
    @SerializedName("current_step")     val currentStep: Int,
    val steps: List<ApprovalStepDto>,
    @SerializedName("created_at")       val createdAt: String?,
    @SerializedName("requester_name")   val requesterName: String?,
    val notes: String?,
)

data class ApprovalStepDto(
    val step: Int,
    @SerializedName("approver_name") val approverName: String?,
    val status: String,
    val notes: String?,
    @SerializedName("acted_at")      val actedAt: String?,
)

data class ApprovalActionRequest(
    val notes: String? = null,
)

// ═══════════════════════════════════════════════════════════════════
// PERFORMANCE
// ═══════════════════════════════════════════════════════════════════

data class PerformanceCycleDto(
    val id: String,
    val name: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date")   val endDate: String,
    val status: String,
    @SerializedName("my_review_status") val myReviewStatus: String?,
)

data class PerformanceCycleDetailDto(
    val id: String,
    val name: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date")   val endDate: String,
    val status: String,
    val goals: List<PerformanceGoalDto>,
    @SerializedName("my_review") val myReview: PerformanceReviewDto?,
)

data class PerformanceGoalDto(
    val id: String,
    val title: String,
    val description: String?,
    val weight: Double,
)

data class PerformanceReviewDto(
    val id: String,
    val score: Double?,
    val notes: String?,
    @SerializedName("submitted_at") val submittedAt: String?,
)

data class SubmitPerformanceReviewRequest(
    @SerializedName("cycle_id") val cycleId: String,
    val score: Double,
    val notes: String? = null,
)

// ═══════════════════════════════════════════════════════════════════
// RECRUITMENT
// ═══════════════════════════════════════════════════════════════════

data class JobPostingDto(
    val id: String,
    val title: String,
    @SerializedName("department_name") val departmentName: String?,
    val location: String?,
    val status: String,
    @SerializedName("closes_at") val closesAt: String?,
    @SerializedName("created_at") val createdAt: String?,
)

// ═══════════════════════════════════════════════════════════════════
// NOTIFICATIONS
// ═══════════════════════════════════════════════════════════════════

data class NotificationDto(
    val id: String,
    val title: String,
    val message: String?,
    val type: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("action_url") val actionUrl: String?,
)

// ═══════════════════════════════════════════════════════════════════
// ANNOUNCEMENTS
// ═══════════════════════════════════════════════════════════════════

data class AnnouncementDto(
    val id: String,
    val title: String,
    val content: String,
    val category: String?,
    @SerializedName("published_at") val publishedAt: String?,
    @SerializedName("is_pinned")    val isPinned: Boolean,
)

// ═══════════════════════════════════════════════════════════════════
// CHANGE PASSWORD
// ═══════════════════════════════════════════════════════════════════

data class ChangePasswordRequest(
    @SerializedName("current_password")          val currentPassword: String,
    @SerializedName("new_password")              val newPassword: String,
    @SerializedName("new_password_confirmation") val newPasswordConfirmation: String,
)

// ═══════════════════════════════════════════════════════════════════
// ORG STRUCTURE
// ═══════════════════════════════════════════════════════════════════

data class OrgPositionDto(
    val id: String,
    val name: String,
    val code: String?,
)

data class OrgDepartmentDto(
    val id: String,
    val name: String,
    val code: String?,
    @SerializedName("employee_count") val employeeCount: Int,
    val positions: List<OrgPositionDto>,
)

// ═══════════════════════════════════════════════════════════════════
// EMPLOYEE DOCUMENTS
// ═══════════════════════════════════════════════════════════════════

data class DocumentDto(
    val id: String,
    @SerializedName("document_type") val documentType: String?,
    val name: String?,
    val category: String?,
    @SerializedName("created_at") val createdAt: String?,
)

// ═══════════════════════════════════════════════════════════════════
// FAQ
// ═══════════════════════════════════════════════════════════════════

data class FaqItemDto(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String,
)

// ═══════════════════════════════════════════════════════════════════
// ASSETS
// ═══════════════════════════════════════════════════════════════════

data class AssetDto(
    val id: String,
    val name: String,
    @SerializedName("asset_tag")  val assetTag: String?,
    val category: String?,
    val brand: String?,
    val model: String?,
)

data class AssetAssignmentDto(
    val id: String,
    val asset: AssetDto?,
    @SerializedName("assigned_at")       val assignedAt: String?,
    @SerializedName("expected_return_at") val expectedReturnAt: String?,
    @SerializedName("returned_at")       val returnedAt: String?,
    val notes: String?,
)

// ── Tenant Resolve ────────────────────────────────────────────────────────────
data class TenantResolveDto(
    @SerializedName("tenant_id") val tenantId: String,
    val name: String?,
    val domain: String,
)
























