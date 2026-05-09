package com.example.hroes.data.repository

import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.AssetAssignmentDto

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
