package com.example.data.service

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Centralized Service for Firebase Analytics telemetry and Firebase Crashlytics error monitoring.
 */
object AppAnalytics {
    private const val TAG = "AppAnalytics"
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var firebaseCrashlytics: FirebaseCrashlytics? = null

    fun init(context: Context) {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAnalytics = FirebaseAnalytics.getInstance(context)
                firebaseCrashlytics = FirebaseCrashlytics.getInstance()
                Log.d(TAG, "AppAnalytics and Firebase Crashlytics initialized successfully.")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Firebase Analytics/Crashlytics not active in current environment: ${t.message}")
        }
    }

    fun setUserId(userId: String?) {
        try {
            firebaseAnalytics?.setUserId(userId)
            if (userId != null) {
                firebaseCrashlytics?.setUserId(userId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting user ID: ${e.message}")
        }
    }

    fun setUserProperty(name: String, value: String) {
        try {
            firebaseAnalytics?.setUserProperty(name, value)
            firebaseCrashlytics?.setCustomKey(name, value)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting user property: ${e.message}")
        }
    }

    fun logScreenView(screenName: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            firebaseCrashlytics?.log("Screen viewed: $screenName")
            Log.d(TAG, "Screen viewed: $screenName")
        } catch (e: Exception) {
            Log.w(TAG, "Error logging screen view: ${e.message}")
        }
    }

    fun logEvent(name: String, params: Map<String, String>? = null) {
        try {
            val bundle = Bundle().apply {
                params?.forEach { (key, value) ->
                    putString(key, value)
                }
            }
            firebaseAnalytics?.logEvent(name, bundle)
            firebaseCrashlytics?.log("Event: $name params: $params")
            Log.d(TAG, "Event: $name params: $params")
        } catch (e: Exception) {
            Log.w(TAG, "Error logging event: ${e.message}")
        }
    }

    fun recordException(throwable: Throwable, message: String? = null) {
        try {
            if (message != null) {
                firebaseCrashlytics?.log("Context: $message")
                Log.e(TAG, "Exception recorded [$message]: ${throwable.message}", throwable)
            } else {
                Log.e(TAG, "Exception recorded: ${throwable.message}", throwable)
            }
            firebaseCrashlytics?.recordException(throwable)
        } catch (e: Exception) {
            Log.w(TAG, "Error recording exception: ${e.message}")
        }
    }

    fun setCustomKey(key: String, value: String) {
        try {
            firebaseCrashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(TAG, "Error setting custom key: ${e.message}")
        }
    }
}
