package com.binahr.data.repository

import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.OrgDepartmentDto

class OrgStructureRepository {

    suspend fun getOrgStructure(): Result<List<OrgDepartmentDto>> = try {
        val envelope = ApiConfig.apiService.orgStructure()
        if (envelope.success) {
            Result.success(envelope.data ?: emptyList())
        } else {
            Result.failure(Exception(envelope.message ?: "Gagal memuat struktur organisasi"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
