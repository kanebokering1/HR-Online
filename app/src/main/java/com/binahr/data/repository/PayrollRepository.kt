package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*

class PayrollRepository {

    suspend fun getSlips(page: Int = 1): Result<LaravelPaginated<PayrollSlipDto>> = try {
        Result.success(ApiConfig.apiService.payrollSlips(page))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSlipDetail(id: String): Result<PayrollSlipDetailDto> = try {
        val envelope = ApiConfig.apiService.payrollSlipDetail(id)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
