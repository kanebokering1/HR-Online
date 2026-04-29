package com.example.hronline.data.repository

import com.example.hronline.data.api.ApiConfig
import com.example.hronline.data.api.TokenManager
import com.example.hronline.data.api.model.GoogleLoginRequest
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

        // ── LOCAL DEMO BYPASS ─────────────────────────────────────────────
        // Login dummy untuk akses cepat tanpa server. Hapus saat production.
        if (email.trim().equals("demo", ignoreCase = true) && password == "demo") {
            val dummy = UserDto(
                id = "0",
                name = "Demo User",
                email = "demo@hroes.test",
                emailVerifiedAt = null,
            )
            persistSession("local-bypass-token", dummy)
            return Result.success(dummy)
        }
        // ─────────────────────────────────────────────────────────────────

        return try {
            val response = ApiConfig.apiService.login(LoginRequest(email = email, password = password))
            persistSession(response.token, response.user)
            Result.success(response.user)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e, "Login gagal")))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Tidak dapat terhubung ke server. Pastikan perangkat terhubung ke jaringan yang sama dengan server."))
        } catch (e: Exception) {
            Result.failure(Exception(parseError(e)))
        }
    }

    /**
     * Authenticate using a Google ID token already obtained from CredentialManager.
     * Backend looks up the user by Google email — account MUST already be registered
     * by HRD on the web admin panel; no auto-registration.
     */
    suspend fun loginWithGoogle(idToken: String): Result<UserDto> {
        if (idToken.isBlank()) return Result.failure(Exception("Google ID token kosong"))
        return try {
            val response = ApiConfig.apiService.loginWithGoogle(GoogleLoginRequest(idToken = idToken))
            persistSession(response.token, response.user)
            Result.success(response.user)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e, "Login Google gagal")))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        } catch (e: Exception) {
            Result.failure(Exception(parseError(e)))
        }
    }

    private fun persistSession(token: String, user: UserDto) {
        TokenManager.token     = token
        TokenManager.userName  = user.name
        TokenManager.userEmail = user.email
        TokenManager.userId    = user.id
    }

    private fun parseHttpError(e: HttpException, fallback: String): String {
        return try {
            val body = e.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) JSONObject(body).optString("message", fallback)
            else "$fallback (HTTP ${e.code()})"
        } catch (_: Exception) {
            "$fallback (HTTP ${e.code()})"
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

