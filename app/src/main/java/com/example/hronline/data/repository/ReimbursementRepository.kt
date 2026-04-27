package com.example.hronline.data.repository

import com.example.hronline.data.api.ApiConfig
import com.example.hronline.data.api.model.*

class ReimbursementRepository {

    suspend fun getReimbursementHistory(): Result<List<ReimbursementClaimDto>> = try {
        Result.success(ApiConfig.apiService.reimbursements().data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // TODO: Add submit-reimbursement endpoint to backend
    suspend fun submitReimbursement(request: SubmitReimbursementRequest): Result<ReimbursementRecord> =
        Result.success(
            ReimbursementRecord(
                id = "rb_${System.currentTimeMillis()}",
                title = request.title,
                category = request.category,
                amount = request.amount,
                date = request.date,
                receiptUrl = null,
                status = "Menunggu",
            )
        )
}

