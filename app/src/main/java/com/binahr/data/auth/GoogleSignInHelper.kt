package com.binahr.data.auth


import com.binahr.R
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.binahr.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Wraps Android's Credential Manager API to obtain a Google ID token.
 *
 * Usage:
 *   val token = GoogleSignInHelper.signIn(activityContext)
 *   // POST token to backend `/v1/auth/google`
 *
 * Returns Result.failure with a user-friendly Indonesian error message if the
 * user cancels or no Google account is configured on the device.
 */
object GoogleSignInHelper {

    /** Returns true if the project has a Google Web Client ID configured at build time. */
    fun isConfigured(): Boolean = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    /**
     * Triggers the Google account picker and returns the resulting ID token (JWT)
     * which can be sent to the backend for verification.
     *
     * Must be called from a UI context (Activity).
     */
    suspend fun signIn(context: Context): Result<String> {
        if (!isConfigured()) {
            return Result.failure(IllegalStateException(
                "Google Sign-In belum dikonfigurasi. Tambahkan GOOGLE_WEB_CLIENT_ID di local.properties."
            ))
        }

        val credentialManager = CredentialManager.create(context)

        // Use a non-filtered request so the user can pick any Google account on the device,
        // including ones not previously used with this app.
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = credentialManager.getCredential(context = context, request = request)
            val credential = response.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleCred.idToken)
            } else {
                Result.failure(IllegalStateException("Tipe credential tidak dikenali"))
            }
        } catch (e: NoCredentialException) {
            Result.failure(Exception("Tidak ada akun Google di perangkat. Tambahkan akun Google di pengaturan."))
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(Exception("Gagal membaca token Google. Coba lagi."))
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Login Google dibatalkan atau gagal."))
        }
    }
}






















