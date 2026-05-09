package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.*
import org.json.JSONObject
import retrofit2.HttpException

class LeaveRepository {

    // ── Leave (Cuti) ──────────────────────────────────────────────────────

    suspend fun getLeaveApplications(page: Int = 1): Result<LaravelPaginated<LeaveApplicationDto>> = try {
        Result.success(ApiConfig.apiService.leaveApplications(page))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getLeaveBalances(): Result<List<LeaveBalanceDto>> = try {
        val envelope = ApiConfig.apiService.leaveBalances()
        Result.success(envelope.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun submitLeave(request: SubmitLeaveApiRequest): Result<LeaveApplicationDto> = try {
        val envelope = ApiConfig.apiService.submitLeave(request)
        val data = envelope.data ?: throw Exception("Respon server kosong")
        Result.success(data)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal mengajukan cuti")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cancelLeave(id: String): Result<Unit> = try {
        ApiConfig.apiService.cancelLeave(id)
        Result.success(Unit)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal membatalkan cuti")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Overtime (Lembur) ─────────────────────────────────────────────────

    suspend fun getOvertimeRequests(page: Int = 1): Result<LaravelPaginated<OvertimeRequestDto>> = try {
        Result.success(ApiConfig.apiService.overtimeRequests(page))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun submitOvertime(request: SubmitOvertimeApiRequest): Result<OvertimeRequestDto> = try {
        val envelope = ApiConfig.apiService.submitOvertime(request)
        val data = envelope.data ?: throw Exception("Respon server kosong")
        Result.success(data)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal mengajukan lembur")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun cancelOvertime(id: String): Result<Unit> = try {
        ApiConfig.apiService.cancelOvertime(id)
        Result.success(Unit)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal membatalkan lembur")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun parseHttpError(e: HttpException, fallback: String): String {
        return try {
            val body = e.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) JSONObject(body).optString("message", fallback)
            else "$fallback (HTTP ${e.code()})"
        } catch (_: Exception) { "$fallback (HTTP ${e.code()})" }
    }
}
























