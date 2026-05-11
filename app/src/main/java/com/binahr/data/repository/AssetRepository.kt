package com.binahr.data.repository

import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.AssetAssignmentDto

class AssetRepository {

    suspend fun getMyAssets(status: String? = null): Result<List<AssetAssignmentDto>> = try {
        val envelope = ApiConfig.apiService.myAssets(status = status)
        if (envelope.success) {
            Result.success(envelope.data?.data ?: emptyList())
        } else {
            Result.failure(Exception(envelope.message ?: "Gagal memuat aset"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
