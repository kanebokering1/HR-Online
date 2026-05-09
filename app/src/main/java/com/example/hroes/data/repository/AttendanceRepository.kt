package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.*
import org.json.JSONObject
import retrofit2.HttpException

class AttendanceRepository {

    suspend fun getLogs(page: Int = 1): Result<LaravelPaginated<AttendanceLogDto>> = try {
        Result.success(ApiConfig.apiService.attendanceLogs(page = page, perPage = 30))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun checkIn(request: CheckInRequest): Result<AttendanceLogDto> = try {
        val envelope = ApiConfig.apiService.checkIn(request)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal check-in")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun checkOut(request: CheckOutRequest): Result<AttendanceLogDto> = try {
        val envelope = ApiConfig.apiService.checkOut(request)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal check-out")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun submitCorrection(request: AttendanceCorrectionRequest): Result<Unit> = try {
        ApiConfig.apiService.submitAttendanceCorrection(request)
        Result.success(Unit)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal mengajukan koreksi")))
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






















