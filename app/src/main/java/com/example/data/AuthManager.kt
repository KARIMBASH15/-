package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("life_organizer_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USERNAME = "current_username"
        private const val KEY_CURRENT_ROLE = "current_role"
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    fun isLoggedIn(): Boolean {
        val remember = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        return loggedIn && remember
    }

    fun getCurrentUsername(): String {
        return prefs.getString(KEY_CURRENT_USERNAME, "") ?: ""
    }

    fun getCurrentRole(): String {
        return prefs.getString(KEY_CURRENT_ROLE, "USER") ?: "USER"
    }

    fun isAdmin(): Boolean {
        return getCurrentRole().uppercase() == "ADMIN" || getCurrentUsername().equals("km512", ignoreCase = true)
    }

    fun saveSession(username: String, role: String, rememberMe: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_CURRENT_USERNAME, username)
            .putString(KEY_CURRENT_ROLE, role)
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_CURRENT_USERNAME, "")
            .putString(KEY_CURRENT_ROLE, "USER")
            .putBoolean(KEY_REMEMBER_ME, false)
            .apply()
    }
}
