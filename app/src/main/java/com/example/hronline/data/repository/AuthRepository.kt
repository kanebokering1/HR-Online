package com.example.hronline.data.repository

import com.example.hronline.data.api.ApiConfig
import com.example.hronline.data.api.TokenManager
import com.example.hronline.data.api.model.LoginRequest
import com.example.hronline.data.api.model.UserDto
import org.json.JSONObject
import retrofit2.HttpException
import java.net.UnknownHostException

class AuthRepository {

    suspend fun login(email: String, password: String): Result<UserDto> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email dan password wajib diisi"))
        }

        // ── LOCAL BYPASS (dev only) ───────────────────────────────────────
        if (email.trim() == "demo" && password == "demo") {
            val dummyUser = UserDto(id = "0", name = "Demo User", email = "demo@hroes.test", emailVerifiedAt = null)
            TokenManager.token     = "local-bypass-token"
            TokenManager.userName  = dummyUser.name
            TokenManager.userEmail = dummyUser.email
            TokenManager.userId    = dummyUser.id
            return Result.success(dummyUser)
        }
        // ─────────────────────────────────────────────────────────────────

        return try {
            val response = ApiConfig.apiService.login(LoginRequest(email = email, password = password))
            TokenManager.token     = response.token
            TokenManager.userName  = response.user.name
            TokenManager.userEmail = response.user.email
            TokenManager.userId    = response.user.id
            Result.success(response.user)
        } catch (e: HttpException) {
            // Parse Laravel JSON error body (e.g. {"message": "These credentials..."})
            val errorMsg = try {
                val body = e.response()?.errorBody()?.string()
                if (!body.isNullOrBlank()) JSONObject(body).optString("message", "Login gagal")
                else "Login gagal (HTTP ${e.code()})"
            } catch (_: Exception) {
                "Login gagal (HTTP ${e.code()})"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Tidak dapat terhubung ke server. Pastikan perangkat terhubung ke jaringan yang sama dengan server."))
        } catch (e: Exception) {
            Result.failure(Exception(parseError(e)))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            ApiConfig.apiService.logout()
            TokenManager.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            TokenManager.clear()  // clear token even if request fails
            Result.success(Unit)
        }
    }

    suspend fun me(): Result<UserDto> {
        return try {
            Result.success(ApiConfig.apiService.me())
        } catch (e: Exception) {
            Result.failure(Exception(parseError(e)))
        }
    }

    // TODO: Add change-password endpoint to backend
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return if (newPassword.length >= 8) Result.success(Unit)
        else Result.failure(Exception("Password minimal 8 karakter"))
    }

    private fun parseError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) ||
            msg.contains("Failed to connect", ignoreCase = true) ->
                "Tidak dapat terhubung ke server. Pastikan perangkat terhubung ke jaringan yang sama dengan server."
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("timed out", ignoreCase = true) ->
                "Koneksi timeout. Coba lagi."
            else -> msg.ifBlank { "Terjadi kesalahan" }
        }
    }
}

