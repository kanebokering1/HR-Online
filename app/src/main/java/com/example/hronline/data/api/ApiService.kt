package com.example.hronline.data.api

import com.example.hronline.data.api.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * HROES REST API Service — maps to Laravel MobileApiController endpoints.
 * Base URL: http://{COMPANY_DOMAIN}/api/
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

    // ── Employees ─────────────────────────────────────────────────────────
    @GET("v1/employees")
    suspend fun employees(@Query("page") page: Int = 1): LaravelPaginated<EmployeeDto>

    // ── Attendance ────────────────────────────────────────────────────────
    @GET("v1/attendance/logs")
    suspend fun attendanceLogs(@Query("page") page: Int = 1): LaravelPaginated<AttendanceLogDto>

    // ── Leave ─────────────────────────────────────────────────────────────
    @GET("v1/leave/applications")
    suspend fun leaveApplications(@Query("page") page: Int = 1): LaravelPaginated<LeaveApplicationDto>

    // ── Overtime ──────────────────────────────────────────────────────────
    @GET("v1/overtime/requests")
    suspend fun overtimeRequests(@Query("page") page: Int = 1): LaravelPaginated<OvertimeRequestDto>

    // ── Reimbursement ─────────────────────────────────────────────────────
    @GET("v1/reimbursements")
    suspend fun reimbursements(@Query("page") page: Int = 1): LaravelPaginated<ReimbursementClaimDto>

    // ── Payroll ───────────────────────────────────────────────────────────
    @GET("v1/payroll/slips")
    suspend fun payrollSlips(@Query("page") page: Int = 1): LaravelPaginated<PayrollSlipDto>

    // ── Companies ─────────────────────────────────────────────────────────
    @GET("v1/companies")
    suspend fun companies(): List<CompanyDto>
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

