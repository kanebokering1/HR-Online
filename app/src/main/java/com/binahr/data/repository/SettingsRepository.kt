package com.binahr.data.repository

import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.TenantSettingsDto

class SettingsRepository {

    suspend fun getTenantSettings(): Result<TenantSettingsDto> = try {
        val envelope = ApiConfig.apiService.tenantSettings()
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
