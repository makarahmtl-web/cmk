package com.example

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.cloudinary.android.MediaManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // ១. Initialize Firebase
        try {
            Firebase.initialize(context = this)
            
            // ២. បើក App Check (យ៉ាងមានសុវត្ថិភាព កុំឱ្យ Crash លើទូរស័ព្ទពិតពេលគ្មាន Play Store)
            try {
                val providerFactory = DebugAppCheckProviderFactory.getInstance()
                Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
            } catch (t: Throwable) {
                // Ignore AppCheck provider failure in dev / sideloaded APK
            }
        } catch (e: Throwable) {
            // Handled safely in testing or restricted environments
        }

        // ៣. Initialize Cloudinary MediaManager
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "cmk-materials"
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Already initialized
        }

        // ៤. Initialize AppAnalytics (Firebase Analytics & Crashlytics)
        com.example.data.service.AppAnalytics.init(this)

        // ៥. Safely handle FCM auto-init to prevent hard failure exceptions when push is not configured
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().isAutoInitEnabled = false
        } catch (e: Exception) {
            // Ignored if FCM service is unavailable
        }
    }
}
