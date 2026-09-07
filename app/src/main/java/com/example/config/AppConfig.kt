package com.example.config

import com.example.BuildConfig

/**
 * AppConfig provides clean environment separation between Development and Production modes
 * for Google Play Store Production Release compliance.
 */
object AppConfig {
    /**
     * Master environment flag. Defaults to true for Production release.
     */
    val IS_PRODUCTION: Boolean = !BuildConfig.DEBUG

    val ENVIRONMENT_NAME: String = if (IS_PRODUCTION) "Production" else "Development"

    // API Base Endpoints
    const val PRODUCTION_API_URL = "https://api.cmkmaterials.com/v1/"
    const val DEV_API_URL = "https://dev-api.cmkmaterials.com/v1/"

    val API_BASE_URL: String
        get() = if (IS_PRODUCTION) PRODUCTION_API_URL else DEV_API_URL

    // Firebase Firestore Collection Namespacing for Environment Separation
    private const val PROD_PREFIX = "prod_"
    private const val DEV_PREFIX = "dev_"

    fun getCollectionName(collectionName: String): String {
        val prefix = if (IS_PRODUCTION) PROD_PREFIX else DEV_PREFIX
        return "$prefix$collectionName"
    }

    /**
     * Flag to control whether Developer Demo Bypass is enabled in Auth UI.
     * Always false for Production Releases to ensure strict Auth compliance.
     */
    val IS_DEMO_BYPASS_ENABLED: Boolean = !IS_PRODUCTION
}
