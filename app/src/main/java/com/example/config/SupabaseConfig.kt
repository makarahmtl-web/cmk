package com.example.config

import com.example.BuildConfig

/**
 * Supabase configuration for PostgreSQL Core Database.
 * Supports configurable endpoints, schema definitions, and table namespaces.
 */
object SupabaseConfig {
    // Official Supabase Project Configuration for CMK Materials
    var SUPABASE_URL: String = "https://snxkpfiotujooyczzifm.supabase.co"
    var SUPABASE_ANON_KEY: String = "sb_publishable_QX18r1fEQtuj_1DS_d54mw_ECBoCQaw"

    val REST_ENDPOINT: String
        get() = "$SUPABASE_URL/rest/v1"

    val STORAGE_ENDPOINT: String
        get() = "$SUPABASE_URL/storage/v1"

    // Tables
    const val TABLE_POSTS = "posts"
    const val TABLE_PRODUCTS = "products"
    const val TABLE_COMMENTS = "comments"
    const val TABLE_USERS = "users"
    const val TABLE_ORDERS = "orders"

    // Storage Buckets
    const val BUCKET_MEDIA = "media"
    const val BUCKET_POSTS = "posts"
    const val BUCKET_PRODUCTS = "products"

    fun getPublicStorageUrl(bucket: String, path: String): String {
        val cleanPath = path.trim().removePrefix("/")
        return "$SUPABASE_URL/storage/v1/object/public/$bucket/$cleanPath"
    }

    fun getTableName(table: String): String {
        return table // directly map to public tables
    }

    /**
     * Updates dynamic credentials from user configuration or secrets.
     */
    fun updateCredentials(url: String, anonKey: String) {
        if (url.isNotBlank()) SUPABASE_URL = url.trim().removeSuffix("/")
        if (anonKey.isNotBlank()) SUPABASE_ANON_KEY = anonKey.trim()
    }
}
