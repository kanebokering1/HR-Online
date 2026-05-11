package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.model.*

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
