package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*
import org.json.JSONObject
import retrofit2.HttpException

class ReimbursementRepository {

    suspend fun getReimbursements(page: Int = 1): Result<LaravelPaginated<ReimbursementClaimDto>> = try {
        Result.success(ApiConfig.apiService.reimbursements(page))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getReimbursementDetail(id: String): Result<ReimbursementClaimDto> = try {
        val envelope = ApiConfig.apiService.reimbursementDetail(id)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Full reimbursement submission flow:
     * 1. POST /reimbursements  → create header
     * 2. POST /reimbursements/{id}/items  → add line item
     * 3. POST /reimbursements/{id}/submit → submit for approval
     */
    suspend fun submitReimbursement(
        title: String,
        item: ReimbursementItemRequest,
    ): Result<ReimbursementClaimDto> = try {
        val createEnvelope = ApiConfig.apiService.createReimbursement(CreateReimbursementRequest(title = title))
        val claim = createEnvelope.data ?: throw Exception("Gagal membuat klaim")
        ApiConfig.apiService.addReimbursementItem(claim.id, item)
        ApiConfig.apiService.submitReimbursement(claim.id)
        Result.success(claim)
    } catch (e: HttpException) {
        Result.failure(Exception(parseHttpError(e, "Gagal mengajukan reimbursement")))
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
