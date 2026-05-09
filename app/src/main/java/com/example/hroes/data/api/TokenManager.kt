package com.example.hroes.data.api


import com.example.hroes.BuildConfig
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * TokenManager — stores Sanctum Bearer token + basic user info using
 * [EncryptedSharedPreferences] (AES-256 GCM). Requires API 23+.
 * Call [init] once from Application.onCreate before any API call.
 *
 * Tenant domain is also persisted here so that after a cold start the app
 * can reconstruct the correct X-Tenant header without asking the user again.
 */
object TokenManager {
    private const val PREFS_NAME        = "bina_hr_secure_prefs"
    private const val KEY_TOKEN         = "auth_token"
    private const val KEY_USER_NAME     = "user_name"
    private const val KEY_USER_EMAIL    = "user_email"
    private const val KEY_USER_ID       = "user_id"
    private const val KEY_EMPLOYEE_ID   = "employee_id"
    private const val KEY_FCM_TOKEN     = "fcm_token"
    private const val KEY_TENANT_DOMAIN = "tenant_domain"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var token: String?
        get() = prefs?.getString(KEY_TOKEN, null)
        set(value) { prefs?.edit()?.putString(KEY_TOKEN, value)?.apply() }

    var userName: String?
        get() = prefs?.getString(KEY_USER_NAME, null)
        set(value) { prefs?.edit()?.putString(KEY_USER_NAME, value)?.apply() }

    var userEmail: String?
        get() = prefs?.getString(KEY_USER_EMAIL, null)
        set(value) { prefs?.edit()?.putString(KEY_USER_EMAIL, value)?.apply() }

    var userId: String?
        get() = prefs?.getString(KEY_USER_ID, null)
        set(value) { prefs?.edit()?.putString(KEY_USER_ID, value)?.apply() }

    /** UUID of the Employee record linked to this user (from /auth/me or /employees). */
    var employeeId: String?
        get() = prefs?.getString(KEY_EMPLOYEE_ID, null)
        set(value) { prefs?.edit()?.putString(KEY_EMPLOYEE_ID, value)?.apply() }

    /** FCM registration token, sent to backend on login for push notifications. */
    var fcmToken: String?
        get() = prefs?.getString(KEY_FCM_TOKEN, null)
        set(value) { prefs?.edit()?.putString(KEY_FCM_TOKEN, value)?.apply() }

    /**
     * Tenant domain used as X-Tenant header in every API request.
     * Falls back to BuildConfig.TENANT_DOMAIN if the user never set a custom domain
     * (single-tenant deployments / debug builds).
     *
     * Value examples: "sml.bina-hris.com", "acme.bina-hris.com"
     */
    var tenantDomain: String?
        get() = prefs?.getString(KEY_TENANT_DOMAIN, null)
        set(value) { prefs?.edit()?.putString(KEY_TENANT_DOMAIN, value)?.apply() }

    /** Effective tenant domain — stored value or BuildConfig fallback. */
    fun effectiveTenantDomain(): String =
        tenantDomain?.takeIf { it.isNotBlank() } ?: BuildConfig.TENANT_DOMAIN

    fun isLoggedIn(): Boolean = !token.isNullOrBlank()

    /** Full clear — called on logout. Wipes token AND tenant domain. */
    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}






















