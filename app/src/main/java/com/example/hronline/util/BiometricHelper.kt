package com.example.hronline.util

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps androidx.biometric.BiometricPrompt and stores the user's opt-in preference
 * in SharedPreferences ("biometric_prefs"). The biometric layer here does NOT
 * actually re-authenticate against the server — it gates access to the local
 * Sanctum token already stored in TokenManager (similar to how banking apps
 * gate access to a saved session).
 */
object BiometricHelper {

    private const val PREFS = "biometric_prefs"
    private const val KEY_ENABLED = "enabled"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Returns true if the device has at least one enrolled biometric (fingerprint/face). */
    fun isAvailable(context: Context): Boolean {
        val mgr = BiometricManager.from(context)
        return mgr.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** User has opted in to use biometric for fast login. */
    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    /**
     * Show the system biometric prompt. Calls [onSuccess] only on confirmed auth.
     * Errors and cancellations call [onError] with an Indonesian message.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Login dengan Biometrik",
        subtitle: String = "Sentuh sensor sidik jari atau gunakan Face Unlock",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Do NOT report repeated transient failures as fatal; only terminal errors land here.
                onError(errString.toString())
            }
        })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Gunakan password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        prompt.authenticate(info)
    }
}
