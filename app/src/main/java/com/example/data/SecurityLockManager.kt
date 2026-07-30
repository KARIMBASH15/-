package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SecurityLockManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("life_organizer_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOCK_ENABLED = "is_lock_enabled"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_USE_BIOMETRICS = "use_biometrics"
    }

    fun isLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_IS_LOCK_ENABLED, false)
    }

    fun setLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOCK_ENABLED, enabled).apply()
    }

    fun getPinCode(): String {
        return prefs.getString(KEY_PIN_CODE, "8090") ?: "8090"
    }

    fun setPinCode(pin: String) {
        prefs.edit().putString(KEY_PIN_CODE, pin).apply()
    }

    fun isBiometricsEnabled(): Boolean {
        return prefs.getBoolean(KEY_USE_BIOMETRICS, false)
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_BIOMETRICS, enabled).apply()
    }

    fun validatePin(inputPin: String): Boolean {
        val storedPin = getPinCode()
        return storedPin.isNotEmpty() && storedPin == inputPin
    }
}
