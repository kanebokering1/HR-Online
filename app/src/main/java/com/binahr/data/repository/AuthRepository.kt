package com.binahr.data.repository


import com.binahr.BuildConfig
import com.binahr.data.api.ApiConfig
import com.binahr.data.api.TokenManager
import com.binahr.data.api.model.ChangePasswordRequest
import com.binahr.data.api.model.GoogleLoginRequest
import com.binahr.data.api.model.LoginRequest
import com.binahr.data.api.model.UserDto
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
            val envelope = ApiConfig.apiService.login(LoginRequest(email = email, password = password))
            val data = envelope.data ?: throw Exception("Respons login tidak valid dari server")
            persistSession(data.token, data.user)
            Result.success(data.user)
        } catch (e: HttpException) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            val msg = when (e.code()) {
                401, 422 -> "Email atau kata sandi salah. Periksa kembali dan coba lagi."
                403 -> "Akun Anda tidak memiliki akses. Hubungi admin HR."
                429 -> "Terlalu banyak percobaan login. Tunggu beberapa menit dan coba lagi."
                503 -> "Server sedang dalam pemeliharaan. Coba lagi nanti."
                else -> parseHttpError(e, "Login gagal")
            }
            Result.failure(Exception(msg))
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
            val envelope = ApiConfig.apiService.loginWithGoogle(GoogleLoginRequest(idToken = idToken))
            val data = envelope.data ?: throw Exception("Respons login tidak valid dari server")
            persistSession(data.token, data.user)
            Result.success(data.user)
        } catch (e: HttpException) {
            if (!tenantDomain.isNullOrBlank()) TokenManager.tenantDomain = null
            val msg = when (e.code()) {
                401, 404 -> "Akun Google ini belum terdaftar di perusahaan Anda. Hubungi admin HR."
                422 -> "Verifikasi Google gagal. Coba lagi."
                403 -> "Akun Anda tidak memiliki akses. Hubungi admin HR."
                else -> parseHttpError(e, "Login dengan Google gagal")
            }
            Result.failure(Exception(msg))
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
            TokenManager.clearAuth()
            Result.success(Unit)
        } catch (e: Exception) {
            TokenManager.clearAuth()  // clear token even if request fails
            Result.success(Unit)
        }
    }

    suspend fun me(): Result<UserDto> {
        return try {
            val envelope = ApiConfig.apiService.me()
            val data = envelope.data ?: throw Exception("Profil tidak ditemukan")
            Result.success(data)
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
