package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.model.*

class NotificationRepository {

    suspend fun getNotifications(page: Int = 1): Result<LaravelPaginated<NotificationDto>> = try {
        Result.success(ApiConfig.apiService.notifications(page))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markRead(id: String): Result<Unit> = try {
        ApiConfig.apiService.markNotificationRead(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markAllRead(): Result<Unit> = try {
        ApiConfig.apiService.markAllNotificationsRead()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}






















