package com.example.hroes.data.api


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.model.*
import retrofit2.http.*

/**
 * HROES REST API Service — all /api/v1/ endpoints.
 * Auth: Sanctum Bearer token (added by ApiConfig interceptor).
 */
interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("v1/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): LoginApiResponse

    @POST("v1/auth/logout")
    suspend fun logout()

    @GET("v1/auth/me")
    suspend fun me(): UserDto

    // ── Dashboard ─────────────────────────────────────────────────────────
    @GET("v1/me/summary")
    suspend fun mySummary(): ApiEnvelope<DashboardSummaryDto>

    // ── Employees ─────────────────────────────────────────────────────────
    @GET("v1/employees")
    suspend fun employees(@Query("page") page: Int = 1): LaravelPaginated<EmployeeDto>

    @GET("v1/employees/{id}")
    suspend fun employeeDetail(@Path("id") id: String): ApiEnvelope<EmployeeDetailDto>

    // ── Attendance ────────────────────────────────────────────────────────
    @GET("v1/attendance/logs")
    suspend fun attendanceLogs(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
    ): LaravelPaginated<AttendanceLogDto>

    @POST("v1/attendance/check-in")
    suspend fun checkIn(@Body request: CheckInRequest): ApiEnvelope<AttendanceLogDto>

    @POST("v1/attendance/check-out")
    suspend fun checkOut(@Body request: CheckOutRequest): ApiEnvelope<AttendanceLogDto>

    @POST("v1/attendance/corrections")
    suspend fun submitAttendanceCorrection(@Body request: AttendanceCorrectionRequest): ApiEnvelope<Unit>

    // ── Leave (Cuti) ──────────────────────────────────────────────────────
    @GET("v1/leave/applications")
    suspend fun leaveApplications(@Query("page") page: Int = 1): LaravelPaginated<LeaveApplicationDto>

    @GET("v1/leave/applications/{id}")
    suspend fun leaveApplicationDetail(@Path("id") id: String): ApiEnvelope<LeaveApplicationDto>

    @POST("v1/leave/applications")
    suspend fun submitLeave(@Body request: SubmitLeaveApiRequest): ApiEnvelope<LeaveApplicationDto>

    @POST("v1/leave/applications/{id}/cancel")
    suspend fun cancelLeave(@Path("id") id: String): ApiEnvelope<Unit>

    @GET("v1/leave/balances")
    suspend fun leaveBalances(): ApiEnvelope<List<LeaveBalanceDto>>

    // ── Overtime (Lembur) ─────────────────────────────────────────────────
    @GET("v1/overtime/requests")
    suspend fun overtimeRequests(@Query("page") page: Int = 1): LaravelPaginated<OvertimeRequestDto>

    @GET("v1/overtime/requests/{id}")
    suspend fun overtimeRequestDetail(@Path("id") id: String): ApiEnvelope<OvertimeRequestDto>

    @POST("v1/overtime/requests")
    suspend fun submitOvertime(@Body request: SubmitOvertimeApiRequest): ApiEnvelope<OvertimeRequestDto>

    @POST("v1/overtime/requests/{id}/cancel")
    suspend fun cancelOvertime(@Path("id") id: String): ApiEnvelope<Unit>

    // ── Reimbursement ─────────────────────────────────────────────────────
    @GET("v1/reimbursements")
    suspend fun reimbursements(@Query("page") page: Int = 1): LaravelPaginated<ReimbursementClaimDto>

    @GET("v1/reimbursements/{id}")
    suspend fun reimbursementDetail(@Path("id") id: String): ApiEnvelope<ReimbursementClaimDto>

    @POST("v1/reimbursements")
    suspend fun createReimbursement(@Body request: CreateReimbursementRequest): ApiEnvelope<ReimbursementClaimDto>

    @POST("v1/reimbursements/{id}/items")
    suspend fun addReimbursementItem(
        @Path("id") id: String,
        @Body request: ReimbursementItemRequest,
    ): ApiEnvelope<Unit>

    @POST("v1/reimbursements/{id}/submit")
    suspend fun submitReimbursement(@Path("id") id: String): ApiEnvelope<Unit>

    // ── Payroll ───────────────────────────────────────────────────────────
    @GET("v1/payroll/slips")
    suspend fun payrollSlips(@Query("page") page: Int = 1): LaravelPaginated<PayrollSlipDto>

    @GET("v1/payroll/slips/{id}")
    suspend fun payrollSlipDetail(@Path("id") id: String): ApiEnvelope<PayrollSlipDetailDto>

    // ── Companies ─────────────────────────────────────────────────────────
    @GET("v1/companies")
    suspend fun companies(): List<CompanyDto>

    // ── Calendar & Holidays ───────────────────────────────────────────────
    @GET("v1/company/holidays")
    suspend fun companyHolidays(@Query("year") year: Int? = null): ApiEnvelope<List<HolidayDto>>

    // ── Scheduling ────────────────────────────────────────────────────────
    @GET("v1/scheduling/my-assignments")
    suspend fun myShiftAssignments(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): ApiEnvelope<List<ShiftAssignmentDto>>

    // ── Approvals ─────────────────────────────────────────────────────────
    @GET("v1/approvals")
    suspend fun approvals(
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null,
    ): LaravelPaginated<ApprovalDto>

    @GET("v1/approvals/{id}")
    suspend fun approvalDetail(@Path("id") id: String): ApiEnvelope<ApprovalDetailDto>

    @POST("v1/approvals/{id}/approve")
    suspend fun approveRequest(
        @Path("id") id: String,
        @Body request: ApprovalActionRequest,
    ): ApiEnvelope<Unit>

    @POST("v1/approvals/{id}/reject")
    suspend fun rejectRequest(
        @Path("id") id: String,
        @Body request: ApprovalActionRequest,
    ): ApiEnvelope<Unit>

    // ── Performance ───────────────────────────────────────────────────────
    @GET("v1/performance/cycles")
    suspend fun performanceCycles(@Query("page") page: Int = 1): LaravelPaginated<PerformanceCycleDto>

    @GET("v1/performance/cycles/{id}")
    suspend fun performanceCycleDetail(@Path("id") id: String): ApiEnvelope<PerformanceCycleDetailDto>

    @POST("v1/performance/reviews")
    suspend fun submitPerformanceReview(@Body request: SubmitPerformanceReviewRequest): ApiEnvelope<Unit>

    // ── Recruitment ───────────────────────────────────────────────────────
    @GET("v1/recruitment/postings")
    suspend fun jobPostings(@Query("page") page: Int = 1): LaravelPaginated<JobPostingDto>

    // ── Notifications ─────────────────────────────────────────────────────
    @GET("v1/notifications")
    suspend fun notifications(@Query("page") page: Int = 1): LaravelPaginated<NotificationDto>

    @POST("v1/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): ApiEnvelope<Unit>

    @POST("v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): ApiEnvelope<Unit>

    // ── Announcements ─────────────────────────────────────────────────────
    @GET("v1/announcements")
    suspend fun announcements(@Query("page") page: Int = 1): LaravelPaginated<AnnouncementDto>

    // ── Change Password ───────────────────────────────────────────────────
    @POST("v1/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiEnvelope<Unit>

    // ── Org Structure ─────────────────────────────────────────────────────
    @GET("v1/org-structure")
    suspend fun orgStructure(): ApiEnvelope<List<OrgDepartmentDto>>

    // ── My Documents ──────────────────────────────────────────────────────
    @GET("v1/me/documents")
    suspend fun myDocuments(): ApiEnvelope<List<DocumentDto>>

    // ── My Assets ─────────────────────────────────────────────────────────
    @GET("v1/me/assets")
    suspend fun myAssets(
        @Query("page") page: Int = 1,
        @Query("status") status: String? = null,
    ): ApiEnvelope<LaravelPaginated<AssetAssignmentDto>>

    // ── FAQ ───────────────────────────────────────────────────────────────
    @GET("v1/faq")
    suspend fun faq(): ApiEnvelope<List<FaqItemDto>>

    // ── Tenant Resolve — public, no auth required ─────────────────────────
    @GET("v1/tenant/resolve")
    suspend fun resolveTenant(@Query("code") code: String): ApiEnvelope<TenantResolveDto>
}


    // @GET("user/profile")
    // suspend fun getProfile(): ApiResponse<UserProfile>

    // @PUT("user/profile")
    // suspend fun updateProfile(@Body profile: UserProfile): ApiResponse<UserProfile>

    // @POST("user/avatar")
    // suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): ApiResponse<String>  // returns photoUrl

    // ═══════════════════════════════════════════════════════════════════
    // ATTENDANCE
    // ═══════════════════════════════════════════════════════════════════

    // @POST("attendance/check-in")
    // suspend fun checkIn(@Body request: CheckInRequest): ApiResponse<AttendanceRecord>

    // @POST("attendance/check-out")
    // suspend fun checkOut(@Body request: CheckOutRequest): ApiResponse<AttendanceRecord>

    // @GET("attendance/today")
    // suspend fun getTodayAttendance(): ApiResponse<AttendanceRecord?>

    // @GET("attendance/history")
    // suspend fun getAttendanceHistory(
    //     @Query("month") month: Int,    // 1-12
    //     @Query("year") year: Int,
    //     @Query("page") page: Int = 1,
    //     @Query("pageSize") pageSize: Int = 31,
    // ): ApiResponse<PaginatedResponse<AttendanceRecord>>

    // ═══════════════════════════════════════════════════════════════════
    // LEAVE (CUTI)
    // ═══════════════════════════════════════════════════════════════════

    // @GET("leave/balance")
    // suspend fun getLeaveBalance(): ApiResponse<LeaveBalanceResponse>

    // @POST("leave/submit")
    // suspend fun submitLeave(@Body request: SubmitLeaveRequest): ApiResponse<LeaveRecord>

    // @GET("leave/history")
    // suspend fun getLeaveHistory(
    //     @Query("page") page: Int = 1,
    //     @Query("pageSize") pageSize: Int = 20,
    // ): ApiResponse<PaginatedResponse<LeaveRecord>>

    // @DELETE("leave/{id}")
    // suspend fun cancelLeave(@Path("id") id: String): ApiResponse<Unit>

    // ═══════════════════════════════════════════════════════════════════
    // OVERTIME (LEMBUR)
    // ═══════════════════════════════════════════════════════════════════

    // @POST("overtime/submit")
    // suspend fun submitOvertime(@Body request: SubmitOvertimeRequest): ApiResponse<OvertimeRecord>

    // @GET("overtime/history")
    // suspend fun getOvertimeHistory(
    //     @Query("month") month: Int,
    //     @Query("year") year: Int,
    // ): ApiResponse<PaginatedResponse<OvertimeRecord>>

    // ═══════════════════════════════════════════════════════════════════
    // PERMISSION / SICK (IZIN)
    // ═══════════════════════════════════════════════════════════════════

    // @POST("permission/submit")
    // suspend fun submitPermission(@Body request: SubmitPermissionRequest): ApiResponse<PermissionRecord>

    // @GET("permission/history")
    // suspend fun getPermissionHistory(
    //     @Query("type") type: String? = null,
    // ): ApiResponse<PaginatedResponse<PermissionRecord>>

    // @POST("permission/{id}/attachment")
    // suspend fun uploadPermissionAttachment(
    //     @Path("id") id: String,
    //     @Part file: MultipartBody.Part,
    // ): ApiResponse<String>  // returns fileUrl

    // ═══════════════════════════════════════════════════════════════════
    // REIMBURSEMENT
    // ═══════════════════════════════════════════════════════════════════

    // @POST("reimbursement/submit")
    // suspend fun submitReimbursement(@Body request: SubmitReimbursementRequest): ApiResponse<ReimbursementRecord>

    // @GET("reimbursement/history")
    // suspend fun getReimbursementHistory(): ApiResponse<PaginatedResponse<ReimbursementRecord>>

    // @POST("reimbursement/{id}/receipt")
    // suspend fun uploadReceipt(
    //     @Path("id") id: String,
    //     @Part file: MultipartBody.Part,
    // ): ApiResponse<String>

    // ═══════════════════════════════════════════════════════════════════
    // PAYSLIP (SLIP GAJI)
    // ═══════════════════════════════════════════════════════════════════

    // @GET("payslip")
    // suspend fun getPayslipList(): ApiResponse<List<PayslipSummary>>

    // @GET("payslip/{id}/download")
    // suspend fun downloadPayslip(@Path("id") id: String): ResponseBody

    // ═══════════════════════════════════════════════════════════════════
    // ANNOUNCEMENTS
    // ═══════════════════════════════════════════════════════════════════

    // @GET("announcements")
    // suspend fun getAnnouncements(
    //     @Query("page") page: Int = 1,
    // ): ApiResponse<PaginatedResponse<Announcement>>

    // ═══════════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════

    // @GET("notifications")
    // suspend fun getNotifications(): ApiResponse<List<NotificationItem>>

    // @PUT("notifications/{id}/read")
    // suspend fun markNotificationRead(@Path("id") id: String): ApiResponse<Unit>

    // @PUT("notifications/read-all")
    // suspend fun markAllNotificationsRead(): ApiResponse<Unit>























