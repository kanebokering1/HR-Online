package com.example.hronline.data.api

import android.content.Context
import android.content.SharedPreferences

/**
 * TokenManager — stores Sanctum Bearer token + basic user info in SharedPreferences.
 * Call [init] once from Application or MainActivity before any API call.
 */
object TokenManager {
    private const val PREFS_NAME = "hroes_secure_prefs"
    private const val KEY_TOKEN      = "auth_token"
    private const val KEY_USER_NAME  = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID    = "user_id"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
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

    fun isLoggedIn(): Boolean = !token.isNullOrBlank()

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
