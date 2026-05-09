package com.example.hroes.data.repository

import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.DocumentDto

class DocumentRepository {

    suspend fun getMyDocuments(): Result<List<DocumentDto>> = try {
        val envelope = ApiConfig.apiService.myDocuments()
        if (envelope.success) {
            Result.success(envelope.data ?: emptyList())
        } else {
            Result.failure(Exception(envelope.message ?: "Gagal memuat dokumen"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
