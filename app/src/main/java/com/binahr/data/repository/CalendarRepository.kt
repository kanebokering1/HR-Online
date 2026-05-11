package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*

class CalendarRepository {

    suspend fun getHolidays(year: Int? = null): Result<List<HolidayDto>> = try {
        val envelope = ApiConfig.apiService.companyHolidays(year)
        Result.success(envelope.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMyShiftAssignments(from: String? = null, to: String? = null): Result<List<ShiftAssignmentDto>> = try {
        val envelope = ApiConfig.apiService.myShiftAssignments(from, to)
        Result.success(envelope.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
