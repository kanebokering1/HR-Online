package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.*
import org.json.JSONObject
import retrofit2.HttpException

class ApprovalRepository {

    suspend fun getApprovals(page: Int = 1, status: String? = null): Result<LaravelPaginated<ApprovalDto>> = try {
        Result.success(ApiConfig.apiService.approvals(page, status))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getApprovalDetail(id: String): Result<ApprovalDetailDto> = try {
        val envelope = ApiConfig.apiService.approvalDetail(id)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun approve(id: String, notes: String? = null): Result<Unit> = try {
        ApiConfig.apiService.approveRequest(id, ApprovalActionRequest(notes))
        Result.success(Unit)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal menyetujui")))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun reject(id: String, notes: String? = null): Result<Unit> = try {
        ApiConfig.apiService.rejectRequest(id, ApprovalActionRequest(notes))
        Result.success(Unit)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal menolak")))
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






















