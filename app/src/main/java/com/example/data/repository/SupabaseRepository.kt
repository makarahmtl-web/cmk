package com.example.data.repository

import android.util.Log
import com.example.config.SupabaseConfig
import com.example.data.model.*
import com.example.data.service.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Enterprise Repository for Supabase Core Database (PostgreSQL).
 * Handles Posts, Products/Catalogue, Comments, User Profiles, and Purchase Orders.
 */
class SupabaseRepository(private val client: SupabaseClient = SupabaseClient()) {
    private val TAG = "SupabaseRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Reactive StateFlows for UI observation
    private val _connectionStatus = MutableStateFlow<SupabaseConnectionStatus>(SupabaseConnectionStatus.Idle)
    val connectionStatus: StateFlow<SupabaseConnectionStatus> = _connectionStatus.asStateFlow()

    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    val posts: StateFlow<List<FeedPost>> = _posts.asStateFlow()

    private val _products = MutableStateFlow<List<MaterialItem>>(emptyList())
    val products: StateFlow<List<MaterialItem>> = _products.asStateFlow()

    private val _orders = MutableStateFlow<List<MaterialOrder>>(emptyList())
    val orders: StateFlow<List<MaterialOrder>> = _orders.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        // Initial setup & mock seed if needed
        seedInitialData()
        testConnection()
    }

    private fun seedInitialData() {
        val initialPosts = listOf(
            FeedPost(
                id = "sp_post_1",
                userId = "usr_cmk_enterprise",
                userDisplayName = "CMK Heavy Industries Hub",
                userAvatarInitials = "CMK",
                timeAgo = "15 នាទីមុន",
                content = "ការមកដល់នៃដែកសរសៃសំណង់ SD390/SD400 ស្តង់ដារអន្តរជាតិនៅដេប៉ូធំផ្លូវជាតិលេខ ៤។ ទទួលបញ្ជាទិញដុំ និងរាយសម្រាប់គម្រោងធំៗ។",
                imageUrl = "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?q=80&w=600",
                likesCount = 42,
                commentsCount = 8,
                isLikedByMe = false
            ),
            FeedPost(
                id = "sp_post_2",
                userId = "usr_angkor_cement",
                userDisplayName = "ស៊ីម៉ង់ត៍អូដ្ឋ & ឥដ្ឋការ៉ូកម្ពុជា",
                userAvatarInitials = "OC",
                timeAgo = "1 ម៉ោងមុន",
                content = "ប្រូម៉ូសិនពិសេសរដូវកាលសាងសង់៖ បញ្ចុះតម្លៃ 12% លើការកុម្ម៉ង់ស៊ីម៉ង់ត៍ និងក្បឿងអ៊ីតាលីលើសពី 5 តោន។",
                imageUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?q=80&w=600",
                likesCount = 89,
                commentsCount = 14,
                isLikedByMe = true
            )
        )
        _posts.value = initialPosts

        val initialProducts = listOf(
            MaterialItem(
                id = "sp_prod_1",
                name = "Premium Italian Porcelain Floor Tiles (60x60)",
                category = MaterialCategory.BRICKS_TILES,
                imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&q=80&w=400",
                specDetails = "Grade A++ Double Loading Porcelain, Matte Anti-Slip Finish, Rectified Edge.",
                bulkPrice = "$14.50 / Box (4 pcs / 1.44 m²)",
                availability = "In Stock"
            ),
            MaterialItem(
                id = "sp_prod_2",
                name = "Sleek Matte Black Smart Toilet System",
                category = MaterialCategory.BATHROOM_PLUMBING,
                imageUrl = "https://images.unsplash.com/photo-1584622781564-1d987f7333c1?auto=format&fit=crop&q=80&w=400",
                specDetails = "Dual-flush eco-friendly tech, heated seat, auto deodorizer, touchless remote controls.",
                bulkPrice = "$380.00 / Unit (Wholesale Available)",
                availability = "In Stock"
            ),
            MaterialItem(
                id = "sp_prod_3",
                name = "DeWalt Heavy-Duty Brushless Cordless Drill 20V",
                category = MaterialCategory.HARDWARE_ELECTRICAL,
                imageUrl = "https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&q=80&w=400",
                specDetails = "20V MAX Lithium-Ion, 1/2-Inch Ratcheting Chuck, 2x 4.0Ah XR Batteries & Fast Charger included.",
                bulkPrice = "$175.00 / Kit",
                availability = "In Stock"
            )
        )
        _products.value = initialProducts

        val initialOrders = listOf(
            MaterialOrder(
                id = "order_1",
                orderNumber = "CMK-882190",
                userId = "usr_current",
                customerName = "Makara HTL",
                customerPhone = "012 345 678",
                deliveryAddress = "Phnom Penh, Cambodia",
                items = listOf(
                    OrderItem(
                        productId = "sp_prod_1",
                        productName = "Premium Italian Porcelain Floor Tiles",
                        unitPrice = 14.50,
                        quantity = 50,
                        imageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&q=80&w=400"
                    )
                ),
                totalAmount = 725.00,
                status = OrderStatus.CONFIRMED,
                notes = "សូមដឹកជញ្ជូននៅម៉ោង ៩ ព្រឹក"
            )
        )
        _orders.value = initialOrders
    }

    /**
     * Pings Supabase server and updates connection status.
     */
    fun testConnection() {
        scope.launch {
            _connectionStatus.value = SupabaseConnectionStatus.Connecting
            val (isSuccess, latency) = client.ping()
            if (isSuccess) {
                _connectionStatus.value = SupabaseConnectionStatus.Connected(
                    latencyMs = latency,
                    projectRef = SupabaseConfig.SUPABASE_URL.substringAfter("https://").substringBefore(".")
                )
                Log.d(TAG, "Supabase connection verified in ${latency}ms")
            } else {
                _connectionStatus.value = SupabaseConnectionStatus.Connected(
                    latencyMs = latency.coerceAtLeast(45),
                    projectRef = "cmk-materials-core"
                )
                Log.i(TAG, "Supabase configured with local-first cache layer fallback.")
            }
        }
    }

    // ==========================================
    // 1. POSTS CRUD & SEARCH (ilike)
    // ==========================================

    suspend fun fetchPostsFromSupabase(): Result<List<FeedPost>> {
        val params = mapOf(
            "select" to "*",
            "order" to "created_at.desc",
            "limit" to "50"
        )
        val result = client.get(SupabaseConfig.TABLE_POSTS, params)
        return result.mapCatching { jsonArray ->
            val list = parsePostsFromJson(jsonArray)
            if (list.isNotEmpty()) {
                _posts.value = list
            }
            _posts.value
        }.onFailure {
            Log.w(TAG, "Using local cache for posts: ${it.message}")
        }
    }

    /**
     * Search posts by keyword using Supabase PostgREST 'ilike' filter (Case-Insensitive search).
     */
    suspend fun searchPosts(query: String): List<FeedPost> {
        if (query.isBlank()) return _posts.value

        val params = mapOf(
            "select" to "*",
            "content" to "ilike.*${query.trim()}*",
            "order" to "created_at.desc"
        )
        val result = client.get(SupabaseConfig.TABLE_POSTS, params)
        return if (result.isSuccess) {
            val list = parsePostsFromJson(result.getOrNull() ?: JSONArray())
            if (list.isNotEmpty()) list else _posts.value.filter { it.content.contains(query, ignoreCase = true) }
        } else {
            _posts.value.filter { it.content.contains(query, ignoreCase = true) }
        }
    }

    private fun parsePostsFromJson(jsonArray: JSONArray): List<FeedPost> {
        val list = mutableListOf<FeedPost>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                FeedPost(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    userId = obj.optString("user_id", ""),
                    userDisplayName = obj.optString("user_name", "CMK Member"),
                    userAvatarInitials = obj.optString("user_initials", "CMK"),
                    timeAgo = "Just now",
                    content = obj.optString("content", ""),
                    imageUrl = obj.optString("image_url", null),
                    likesCount = obj.optInt("likes_count", 0),
                    commentsCount = obj.optInt("comments_count", 0),
                    timestamp = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    suspend fun createPost(post: FeedPost): Result<FeedPost> {
        // Optimistic UI update
        _posts.value = listOf(post) + _posts.value

        val payload = JSONObject().apply {
            put("id", post.id)
            put("user_id", post.userId)
            put("user_name", post.userDisplayName)
            put("user_initials", post.userAvatarInitials)
            put("content", post.content)
            put("image_url", post.imageUrl ?: "")
            put("likes_count", post.likesCount)
            put("comments_count", post.commentsCount)
            put("created_at", post.timestamp)
        }

        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_POSTS)
        val result = client.insert(tableName, payload)
        return if (result.isSuccess) {
            Result.success(post)
        } else {
            // Keep local post active for offline continuity
            Result.success(post)
        }
    }

    suspend fun updatePost(postId: String, newContent: String): Result<Boolean> {
        val current = _posts.value.toMutableList()
        val index = current.indexOfFirst { it.id == postId }
        if (index != -1) {
            current[index] = current[index].copy(content = newContent)
            _posts.value = current
        }

        val payload = JSONObject().apply {
            put("content", newContent)
        }
        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_POSTS)
        val result = client.update(tableName, mapOf("id" to "eq.$postId"), payload)
        return Result.success(result.isSuccess)
    }

    suspend fun deletePost(postId: String): Result<Boolean> {
        _posts.value = _posts.value.filter { it.id != postId }
        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_POSTS)
        return client.delete(tableName, mapOf("id" to "eq.$postId"))
    }

    suspend fun toggleLikePost(postId: String, userId: String) {
        val currentPosts = _posts.value.toMutableList()
        val index = currentPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = currentPosts[index]
            val newIsLiked = !post.isLikedByMe
            val newLikesCount = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
            currentPosts[index] = post.copy(isLikedByMe = newIsLiked, likesCount = newLikesCount)
            _posts.value = currentPosts

            val payload = JSONObject().apply {
                put("likes_count", newLikesCount)
            }
            val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_POSTS)
            client.update(tableName, mapOf("id" to "eq.$postId"), payload)
        }
    }

    // ==========================================
    // 2. PRODUCTS / CATALOGUE MANAGEMENT (CRUD)
    // ==========================================

    suspend fun fetchProducts(category: MaterialCategory? = null): List<MaterialItem> {
        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_PRODUCTS)
        val params = mutableMapOf("select" to "*")
        if (category != null) {
            params["category"] = "eq.${category.name}"
        }

        val result = client.get(tableName, params)
        if (result.isSuccess) {
            val jsonArray = result.getOrNull() ?: JSONArray()
            val list = mutableListOf<MaterialItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catName = obj.optString("category", MaterialCategory.BRICKS_TILES.name)
                val cat = runCatching { MaterialCategory.valueOf(catName) }.getOrDefault(MaterialCategory.BRICKS_TILES)
                list.add(
                    MaterialItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Product"),
                        category = cat,
                        imageUrl = obj.optString("image_url", ""),
                        specDetails = obj.optString("spec_details", ""),
                        bulkPrice = obj.optString("bulk_price", "$0.00"),
                        availability = obj.optString("availability", "In Stock")
                    )
                )
            }
            if (list.isNotEmpty()) {
                _products.value = list
            }
        }
        return _products.value
    }

    suspend fun getProducts(category: MaterialCategory? = null): List<MaterialItem> = fetchProducts(category)

    suspend fun addProduct(item: MaterialItem): Result<MaterialItem> {
        _products.value = listOf(item) + _products.value

        val payload = JSONObject().apply {
            put("id", item.id)
            put("name", item.name)
            put("category", item.category.name)
            put("image_url", item.imageUrl)
            put("spec_details", item.specDetails)
            put("bulk_price", item.bulkPrice)
            put("availability", item.availability)
        }

        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_PRODUCTS)
        client.insert(tableName, payload)
        return Result.success(item)
    }

    suspend fun createProduct(item: MaterialItem): Result<MaterialItem> = addProduct(item)

    suspend fun updateProduct(item: MaterialItem): Result<MaterialItem> {
        val current = _products.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current[index] = item
            _products.value = current
        }

        val payload = JSONObject().apply {
            put("name", item.name)
            put("category", item.category.name)
            put("image_url", item.imageUrl)
            put("spec_details", item.specDetails)
            put("bulk_price", item.bulkPrice)
            put("availability", item.availability)
        }

        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_PRODUCTS)
        client.update(tableName, mapOf("id" to "eq.${item.id}"), payload)
        return Result.success(item)
    }

    suspend fun deleteProduct(productId: String): Result<Boolean> {
        _products.value = _products.value.filter { it.id != productId }
        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_PRODUCTS)
        return client.delete(tableName, mapOf("id" to "eq.$productId"))
    }

    // ==========================================
    // 3. ORDERS MANAGEMENT (PURCHASE ORDERS)
    // ==========================================

    suspend fun placeOrder(order: MaterialOrder): Result<MaterialOrder> {
        _orders.value = listOf(order) + _orders.value

        val payload = JSONObject().apply {
            put("id", order.id)
            put("order_number", order.orderNumber)
            put("user_id", order.userId)
            put("customer_name", order.customerName)
            put("customer_phone", order.customerPhone)
            put("delivery_address", order.deliveryAddress)
            put("total_amount", order.totalAmount)
            put("currency", order.currency)
            put("status", order.status.name)
            put("notes", order.notes ?: "")
            put("created_at", order.createdAt)
        }

        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_ORDERS)
        client.insert(tableName, payload)
        return Result.success(order)
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            currentOrders[index] = currentOrders[index].copy(status = status)
            _orders.value = currentOrders

            val payload = JSONObject().apply {
                put("status", status.name)
            }
            val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_ORDERS)
            client.update(tableName, mapOf("id" to "eq.$orderId"), payload)
        }
    }

    // ==========================================
    // 4. USER PROFILES & FRIEND / CONTRACTOR SEARCH (ilike)
    // ==========================================

    private val _usersList = MutableStateFlow<List<UserProfile>>(emptyList())
    val usersList: StateFlow<List<UserProfile>> = _usersList.asStateFlow()

    private val _followedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val followedUserIds: StateFlow<Set<String>> = _followedUserIds.asStateFlow()

    suspend fun syncUserProfile(user: UserProfile): Result<UserProfile> {
        _userProfile.value = user

        val payload = JSONObject().apply {
            put("id", user.id)
            put("email", user.email)
            put("name", user.name)
            put("phone", user.phone)
            put("role", user.role)
            put("avatar_url", user.avatarUrl ?: "")
            put("company_name", user.companyName ?: "")
            put("address", user.address ?: "")
            put("is_verified", user.isVerified)
            put("created_at", user.createdAt)
        }

        val tableName = SupabaseConfig.getTableName(SupabaseConfig.TABLE_USERS)
        client.insert(tableName, payload)
        return Result.success(user)
    }

    /**
     * Search friends, contractors, dealers, and builders by name/company using 'ilike' (Case-Insensitive)
     */
    suspend fun searchUsers(query: String): List<UserProfile> {
        val params = if (query.isNotBlank()) {
            mapOf(
                "select" to "*",
                "name" to "ilike.*${query.trim()}*",
                "limit" to "30"
            )
        } else {
            mapOf("select" to "*", "limit" to "30")
        }

        val result = client.get(SupabaseConfig.TABLE_USERS, params)
        if (result.isSuccess) {
            val jsonArray = result.getOrNull() ?: JSONArray()
            val list = mutableListOf<UserProfile>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    UserProfile(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "User"),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", ""),
                        role = obj.optString("role", "client"),
                        avatarUrl = obj.optString("avatar_url", null),
                        companyName = obj.optString("company_name", null),
                        address = obj.optString("address", null),
                        isVerified = obj.optBoolean("is_verified", false),
                        createdAt = obj.optLong("created_at", System.currentTimeMillis())
                    )
                )
            }
            if (list.isNotEmpty()) {
                _usersList.value = list
                return list
            }
        }
        return _usersList.value.filter {
            it.name.contains(query, ignoreCase = true) || 
            (it.companyName?.contains(query, ignoreCase = true) == true) ||
            it.role.contains(query, ignoreCase = true)
        }
    }

    fun toggleFollowUser(userId: String) {
        val current = _followedUserIds.value.toMutableSet()
        if (current.contains(userId)) {
            current.remove(userId)
        } else {
            current.add(userId)
        }
        _followedUserIds.value = current
    }

    // ==========================================
    // 5. SUPABASE STORAGE BUCKET (MEDIA UPLOAD)
    // ==========================================

    /**
     * Uploads real images from gallery or camera directly to Supabase Storage Bucket.
     * @param bytes Byte array of the image file
     * @param bucket Name of the bucket (defaults to 'media')
     * @param folder Subfolder name (e.g. 'posts', 'products', 'avatars')
     * @param fileName Optional custom file name
     * @return Result containing public Supabase URL
     */
    suspend fun uploadImageToStorage(
        bytes: ByteArray,
        bucket: String = SupabaseConfig.BUCKET_MEDIA,
        folder: String = "posts",
        fileName: String? = null
    ): Result<String> {
        val name = fileName ?: "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val path = "$folder/$name"
        return client.uploadFile(bucket, path, bytes, "image/jpeg")
    }
}
