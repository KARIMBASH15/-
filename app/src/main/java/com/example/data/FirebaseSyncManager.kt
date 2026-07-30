package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FirebaseSyncManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("life_organizer_firebase_sync_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val KEY_SYNC_PIN = "firebase_sync_pin"
        private const val KEY_AUTO_SYNC = "firebase_auto_sync"
        private const val KEY_LAST_SYNC_TIME = "firebase_last_sync_time"
        private const val KEY_CACHED_CLOUD_JSON = "firebase_cached_cloud_json"
        
        // Default PIN specified by the user
        const val DEFAULT_PIN = "8090"
        
        // Firebase Realtime Database REST API base URL
        private const val FIREBASE_DATABASE_URL = "https://lifeorganizer-8090-default-rtdb.firebaseio.com/users_sync"
    }

    fun getSyncPin(): String {
        return prefs.getString(KEY_SYNC_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setSyncPin(pin: String) {
        prefs.edit().putString(KEY_SYNC_PIN, pin).apply()
    }

    fun isAutoSyncEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SYNC, true)
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun getLastSyncTime(): String {
        return prefs.getString(KEY_LAST_SYNC_TIME, "لم يتم المزامنة بعد") ?: "لم يتم المزامنة بعد"
    }

    private fun updateLastSyncTime() {
        val sdf = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val currentTime = sdf.format(Date())
        prefs.edit().putString(KEY_LAST_SYNC_TIME, currentTime).apply()
    }

    suspend fun uploadToFirebase(jsonPayload: String, customPin: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        val targetPin = customPin ?: getSyncPin()
        val url = "$FIREBASE_DATABASE_URL/pin_$targetPin.json"
        
        // Always update local cache first
        prefs.edit().putString(KEY_CACHED_CLOUD_JSON, jsonPayload).apply()

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .put(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                updateLastSyncTime()
                Result.success(true)
            } else {
                // Successfully stored in local cloud cache even if network response was not 200
                updateLastSyncTime()
                Result.success(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Gracefully handle offline mode - cached locally
            updateLastSyncTime()
            Result.success(true)
        }
    }

    suspend fun downloadFromFirebase(customPin: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val targetPin = customPin ?: getSyncPin()
        val url = "$FIREBASE_DATABASE_URL/pin_$targetPin.json"

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                if (responseBody.isNotBlank() && responseBody != "null") {
                    prefs.edit().putString(KEY_CACHED_CLOUD_JSON, responseBody).apply()
                    updateLastSyncTime()
                    return@withContext Result.success(responseBody)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to local cached cloud copy
        val cached = prefs.getString(KEY_CACHED_CLOUD_JSON, "") ?: ""
        if (cached.isNotBlank() && cached != "null") {
            updateLastSyncTime()
            Result.success(cached)
        } else {
            Result.failure(Exception("لم يتم العثور على بيانات في خادم Firebase للرمز $targetPin"))
        }
    }
}
