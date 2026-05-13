package com.binahr.data.api


import com.binahr.BuildConfig
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
    private const val KEY_PERMISSIONS   = "permissions"

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
     * Comma-separated permission names from the server's /auth/me response.
     * Used for RBAC gating in the UI (no direct enforcement — server enforces).
     */
    var permissions: String?
        get() = prefs?.getString(KEY_PERMISSIONS, null)
        set(value) { prefs?.edit()?.putString(KEY_PERMISSIONS, value)?.apply() }

    fun hasPermission(permission: String): Boolean =
        permissions?.split(",")?.contains(permission) == true

    fun hasAnyPermission(vararg perms: String): Boolean =
        perms.any { hasPermission(it) }

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

    /**
     * Clear auth session only (token, user info).
     * Tenant domain is preserved so the user doesn't have to re-enter
     * company code on the next login.
     */
    fun clearAuth() {
        prefs?.edit()
            ?.remove(KEY_TOKEN)
            ?.remove(KEY_USER_NAME)
            ?.remove(KEY_USER_EMAIL)
            ?.remove(KEY_USER_ID)
            ?.remove(KEY_EMPLOYEE_ID)
            ?.remove(KEY_PERMISSIONS)
            ?.apply()
    }

    /** Full wipe including tenant domain — only call from "Change Company" flow. */
    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }

    /**
     * Callback invoked by the OkHttp Authenticator when a 401 is received.
     * Set by AppNavigation to navigate to the Login screen and clear the back-stack.
     * Must only be called on the main thread (Compose navigation).
     */
    var onUnauthorized: (() -> Unit)? = null

    /** @deprecated Use clearAuth() on logout; clearAll() only when changing company. */
    fun clear() = clearAuth()
}






















