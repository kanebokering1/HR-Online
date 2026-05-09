package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.*

class ProfileRepository {

    suspend fun getMe(): Result<UserDto> = try {
        Result.success(ApiConfig.apiService.me())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDashboardSummary(): Result<DashboardSummaryDto> = try {
        val envelope = ApiConfig.apiService.mySummary()
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEmployeeDetail(id: String): Result<EmployeeDetailDto> = try {
        val envelope = ApiConfig.apiService.employeeDetail(id)
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getOrgEmployees(page: Int = 1): Result<LaravelPaginated<EmployeeDto>> = try {
        Result.success(ApiConfig.apiService.employees(page))
    } catch (e: Exception) {
        Result.failure(e)
    }
}






















