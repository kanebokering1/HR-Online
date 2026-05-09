package com.example.hroes.data.repository


import com.example.hroes.BuildConfig
import com.example.hroes.data.api.ApiConfig
import com.example.hroes.data.api.TokenManager
import com.example.hroes.data.api.model.ChangePasswordRequest
import com.example.hroes.data.api.model.GoogleLoginRequest
import com.example.hroes.data.api.model.LoginRequest
import com.example.hroes.data.api.model.UserDto
import org.json.JSONObject
import retrofit2.HttpException
import java.net.UnknownHostException

class AuthRepository {

    /**
     * @param tenantDomain  e.g. "sml.bina-hris.com" — saved to TokenManager so the
     *                      ApiConfig interceptor sends the correct X-Tenant on all
     *                      subsequent requests. Pass null to keep the current value.
     */
    suspend fun login(email: String, password: String, tenantDomain: String? = null): Result<UserDto> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email dan password wajib diisi"))
        }

        // Set tenant domain BEFORE the API call so the interceptor uses it.
        if (!tenantDomain.isNullOrBlank()) {
            TokenManager.tenantDomain = tenantDomain.trim()
        }

        return try {
            val response = ApiConfig.apiService.login(LoginRequest(email = email, password = password))
            persistSession(response.token, response.user)
            Result.success(response.user)
        } catch (e: HttpException) {
            // On failure, roll back the tenant domain so a subsequent attempt
            // can re-enter a corrected domain.
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            Result.failure(Exception(parseHttpError(e, "Login gagal")))
        } catch (e: UnknownHostException) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            Result.failure(Exception("Tidak dapat terhubung ke server. Pastikan perangkat terhubung ke internet."))
        } catch (e: Exception) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            Result.failure(Exception(parseError(e)))
        }
    }

    /**
     * Authenticate using a Google ID token already obtained from CredentialManager.
     * Backend looks up the user by Google email — account MUST already be registered
     * by HRD on the web admin panel; no auto-registration.
     */
    suspend fun loginWithGoogle(idToken: String, tenantDomain: String? = null): Result<UserDto> {
        if (idToken.isBlank()) return Result.failure(Exception("Google ID token kosong"))
        if (!tenantDomain.isNullOrBlank()) {
            TokenManager.tenantDomain = tenantDomain.trim()
        }
        return try {
            val response = ApiConfig.apiService.loginWithGoogle(GoogleLoginRequest(idToken = idToken))
            persistSession(response.token, response.user)
            Result.success(response.user)
        } catch (e: HttpException) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            Result.failure(Exception(parseHttpError(e, "Login Google gagal")))
        } catch (e: UnknownHostException) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        } catch (e: Exception) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            Result.failure(Exception(parseError(e)))
        }
    }

    private fun persistSession(token: String, user: UserDto) {
        TokenManager.token     = token
        TokenManager.userName  = user.name
        TokenManager.userEmail = user.email
        TokenManager.userId    = user.id
        // employeeId resolved lazily when needed (via /v1/auth/me or /v1/employees)
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

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        if (newPassword.length < 8) return Result.failure(Exception("Password minimal 8 karakter"))
        return try {
            ApiConfig.apiService.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    newPasswordConfirmation = newPassword,
                )
            )
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e, "Gagal mengubah password")))
        } catch (e: UnknownHostException) {
            Result.failure(Exception("Tidak dapat terhubung ke server."))
        } catch (e: Exception) {
            Result.failure(Exception(parseError(e)))
        }
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























