package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*

class AnnouncementRepository {

    suspend fun getAnnouncements(page: Int = 1): Result<LaravelPaginated<AnnouncementDto>> = try {
        Result.success(ApiConfig.apiService.announcements(page))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
