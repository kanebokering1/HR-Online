package com.example.hronline.data.repository

import com.example.hronline.data.api.ApiConfig
import com.example.hronline.data.api.model.*

class LeaveRepository {

    // ── Leave (Cuti) ─────────────────────────────────────────────────────────

    suspend fun getLeaveHistory(): Result<List<LeaveApplicationDto>> = try {
        Result.success(ApiConfig.apiService.leaveApplications().data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // TODO: Add leave-balance endpoint to backend
    suspend fun getLeaveBalance(): Result<LeaveBalanceResponse> =
        Result.success(LeaveBalanceResponse(annual = 9, sick = 12, total = 21))

    // TODO: Add submit-leave endpoint to backend
    suspend fun submitLeave(request: SubmitLeaveRequest): Result<LeaveRecord> =
        Result.success(
            LeaveRecord(
                id = "lv_${System.currentTimeMillis()}",
                type = request.type,
                startDate = request.startDate,
                endDate = request.endDate,
                days = 1,
                reason = request.reason,
                status = "Menunggu",
                approvedBy = null,
                approvedAt = null,
            )
        )

    // ── Overtime (Lembur) ─────────────────────────────────────────────────

    suspend fun getOvertimeHistory(): Result<List<OvertimeRequestDto>> = try {
        Result.success(ApiConfig.apiService.overtimeRequests().data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // TODO: Add submit-overtime endpoint to backend
    suspend fun submitOvertime(request: SubmitOvertimeRequest): Result<OvertimeRecord> {
        val startH = request.startTime.split(":").getOrNull(0)?.toIntOrNull() ?: 17
        val endH   = request.endTime.split(":").getOrNull(0)?.toIntOrNull() ?: 20
        return Result.success(
            OvertimeRecord(
                id = "ot_${System.currentTimeMillis()}",
                date = request.date,
                startTime = request.startTime,
                endTime = request.endTime,
                hours = (endH - startH).toDouble().coerceAtLeast(0.0),
                reason = request.reason,
                status = "Menunggu",
            )
        )
    }

    // ── Permission / Izin ─────────────────────────────────────────────────

    // TODO: Add permission-specific endpoint to backend (currently shared with leave)
    suspend fun getPermissionHistory(): Result<List<PermissionRecord>> =
        Result.success(emptyList())

    suspend fun submitPermission(request: SubmitPermissionRequest): Result<PermissionRecord> =
        Result.success(
            PermissionRecord(
                id = "iz_${System.currentTimeMillis()}",
                type = request.type,
                date = request.date,
                days = request.days,
                reason = request.reason,
                attachment = request.attachmentUrl,
                status = "Menunggu",
            )
        )
}

