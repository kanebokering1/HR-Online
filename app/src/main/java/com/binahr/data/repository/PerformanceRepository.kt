package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*
import org.json.JSONObject
import retrofit2.HttpException

class PerformanceRepository {

    suspend fun getCycles(page: Int = 1): Result<LaravelPaginated<PerformanceCycleDto>> = try {
        Result.success(ApiConfig.apiService.performanceCycles(page))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCycleDetail(id: String): Result<PerformanceCycleDetailDto> = try {
        val envelope = ApiConfig.apiService.performanceCycleDetail(id)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun submitReview(request: SubmitPerformanceReviewRequest): Result<Unit> = try {
        ApiConfig.apiService.submitPerformanceReview(request)
        Result.success(Unit)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal menyimpan review")))
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
