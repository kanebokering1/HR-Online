package com.binahr.data.repository

import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.JobPostingDto
import org.json.JSONObject
import retrofit2.HttpException

class RecruitmentRepository {
    suspend fun getJobPostings(page: Int = 1): Result<List<JobPostingDto>> = try {
        val paginated = ApiConfig.apiService.jobPostings(page)
        Result.success(paginated.data)
    } catch (e: HttpException) {
        val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
        val msg = if (!body.isNullOrBlank())
            JSONObject(body).optString("message", "Gagal memuat lowongan")
        else "Gagal memuat lowongan (HTTP ${e.code()})"
        Result.failure(Exception(msg))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
