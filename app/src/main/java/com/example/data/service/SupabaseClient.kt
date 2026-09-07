package com.example.data.service

import android.util.Log
import com.example.config.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Standard HTTP PostgREST Client for Supabase Core Database.
 * Communicates directly with Supabase PostgreSQL instance using async coroutines.
 *
 * @param projectUrl The Supabase project base URL (e.g. "https://xyz.supabase.co")
 * @param apiKey The Supabase Anon/Public API Key (JWT)
 */
class SupabaseClient(
    val projectUrl: String = SupabaseConfig.SUPABASE_URL,
    val apiKey: String = SupabaseConfig.SUPABASE_ANON_KEY
) {
    private val TAG = "SupabaseClient"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val restEndpoint: String
        get() = "${projectUrl.trim().removeSuffix("/")}/rest/v1"

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun getBaseHeaders(): Headers {
        return Headers.Builder()
            .add("apikey", apiKey)
            .add("Authorization", "Bearer $apiKey")
            .add("Content-Type", "application/json")
            .add("Accept", "application/json")
            .build()
    }

    /**
     * Executes a SELECT query against a table.
     * @param table Table name (e.g., 'posts', 'products')
     * @param queryParams Map of PostgREST filters (e.g., "select" to "*", "order" to "created_at.desc")
     */
    suspend fun get(table: String, queryParams: Map<String, String> = emptyMap()): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "$restEndpoint/$table".toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid URL"))

            queryParams.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .headers(getBaseHeaders())
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: "[]"

            if (response.isSuccessful) {
                val jsonArray = if (bodyString.trim().startsWith("[")) {
                    JSONArray(bodyString)
                } else if (bodyString.trim().startsWith("{")) {
                    JSONArray().put(JSONObject(bodyString))
                } else {
                    JSONArray()
                }
                Result.success(jsonArray)
            } else {
                Log.w(TAG, "Supabase GET $table failed (${response.code}): $bodyString")
                Result.failure(IOException("HTTP ${response.code}: $bodyString"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase GET $table exception", e)
            Result.failure(e)
        }
    }

    /**
     * Inserts a record or list of records into a table.
     * @param table Table name
     * @param payload JSONObject or JSONArray to insert
     */
    suspend fun insert(table: String, payload: Any): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val headers = getBaseHeaders().newBuilder()
                .add("Prefer", "return=representation")
                .build()

            val request = Request.Builder()
                .url("$restEndpoint/$table")
                .headers(headers)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: "[]"

            if (response.isSuccessful) {
                val jsonArray = if (bodyString.trim().startsWith("[")) {
                    JSONArray(bodyString)
                } else if (bodyString.trim().startsWith("{")) {
                    JSONArray().put(JSONObject(bodyString))
                } else {
                    JSONArray()
                }
                Result.success(jsonArray)
            } else {
                Log.w(TAG, "Supabase INSERT $table failed (${response.code}): $bodyString")
                Result.failure(IOException("HTTP ${response.code}: $bodyString"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase INSERT $table exception", e)
            Result.failure(e)
        }
    }

    /**
     * Updates records matching filter query params.
     */
    suspend fun update(table: String, queryParams: Map<String, String>, payload: JSONObject): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "$restEndpoint/$table".toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid URL"))

            queryParams.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }

            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val headers = getBaseHeaders().newBuilder()
                .add("Prefer", "return=representation")
                .build()

            val request = Request.Builder()
                .url(urlBuilder.build())
                .headers(headers)
                .patch(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: "[]"

            if (response.isSuccessful) {
                val jsonArray = if (bodyString.trim().startsWith("[")) {
                    JSONArray(bodyString)
                } else if (bodyString.trim().startsWith("{")) {
                    JSONArray().put(JSONObject(bodyString))
                } else {
                    JSONArray()
                }
                Result.success(jsonArray)
            } else {
                Log.w(TAG, "Supabase UPDATE $table failed (${response.code}): $bodyString")
                Result.failure(IOException("HTTP ${response.code}: $bodyString"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase UPDATE $table exception", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes records matching filter query params.
     */
    suspend fun delete(table: String, queryParams: Map<String, String>): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = "$restEndpoint/$table".toHttpUrlOrNull()?.newBuilder()
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid URL"))

            queryParams.forEach { (key, value) ->
                urlBuilder.addQueryParameter(key, value)
            }

            val request = Request.Builder()
                .url(urlBuilder.build())
                .headers(getBaseHeaders())
                .delete()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val bodyString = response.body?.string() ?: ""
                Log.w(TAG, "Supabase DELETE $table failed (${response.code}): $bodyString")
                Result.failure(IOException("HTTP ${response.code}: $bodyString"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase DELETE $table exception", e)
            Result.failure(e)
        }
    }

    /**
     * Uploads binary file (e.g. Image from Gallery) to Supabase Storage Bucket.
     * @param bucket Name of storage bucket (e.g., "media", "posts", "products")
     * @param path File path inside bucket (e.g., "uploads/img_12345.jpg")
     * @param fileBytes Raw byte array of the file
     * @param mimeType MIME type (e.g., "image/jpeg", "image/png")
     * @return Result containing the public URL of the uploaded image
     */
    suspend fun uploadFile(
        bucket: String,
        path: String,
        fileBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanPath = path.trim().removePrefix("/")
            val storageUrl = "${projectUrl.trim().removeSuffix("/")}/storage/v1/object/$bucket/$cleanPath"
            val mediaType = mimeType.toMediaType()
            val requestBody = fileBytes.toRequestBody(mediaType)

            val headers = Headers.Builder()
                .add("apikey", apiKey)
                .add("Authorization", "Bearer $apiKey")
                .add("Content-Type", mimeType)
                .add("x-upsert", "true")
                .build()

            val request = Request.Builder()
                .url(storageUrl)
                .headers(headers)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (response.isSuccessful || response.code == 200 || response.code == 201) {
                val publicUrl = "${projectUrl.trim().removeSuffix("/")}/storage/v1/object/public/$bucket/$cleanPath"
                Log.i(TAG, "Supabase Storage Upload Success: $publicUrl")
                Result.success(publicUrl)
            } else {
                Log.w(TAG, "Supabase Storage Upload Failed (${response.code}): $bodyString")
                Result.failure(IOException("Upload Failed HTTP ${response.code}: $bodyString"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase Storage Upload Exception", e)
            Result.failure(e)
        }
    }

    /**
     * Pings the Supabase REST endpoint to verify connection & latency.
     */
    suspend fun ping(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("$restEndpoint/")
                .headers(getBaseHeaders())
                .head()
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            Pair(response.isSuccessful || response.code == 404 || response.code == 200, latency)
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Pair(false, latency)
        }
    }
}

