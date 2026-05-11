package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*

class ProfileRepository {

    suspend fun getMyProfile(): Result<EmployeeDetailDto?> = try {
        val envelope = ApiConfig.apiService.myProfile()
        Result.success(envelope.data) // data can be null if no employee linked
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMe(): Result<UserDto> = try {
        val envelope = ApiConfig.apiService.me()
        Result.success(envelope.data ?: throw Exception("Respon server kosong"))
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

    suspend fun updateProfile(phone: String?, address: String?): Result<EmployeeDetailDto> = try {
        val envelope = ApiConfig.apiService.updateMyProfile(UpdateProfileRequest(phone = phone, address = address))
        if (envelope.success) {
            Result.success(envelope.data ?: throw Exception("Respon server kosong"))
        } else {
            Result.failure(Exception(envelope.message ?: "Gagal memperbarui profil"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
