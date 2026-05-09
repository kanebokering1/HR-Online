package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.*

class AnnouncementRepository {

    suspend fun getAnnouncements(page: Int = 1): Result<LaravelPaginated<AnnouncementDto>> = try {
        Result.success(ApiConfig.apiService.announcements(page))
    } catch (e: Exception) {
        Result.failure(e)
    }
}






















