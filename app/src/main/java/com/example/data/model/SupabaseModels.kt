package com.example.data.model

import java.util.UUID

/**
 * Enterprise User Profile mapped to Supabase 'users' table (PostgreSQL).
 */
data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String,
    val phone: String = "",
    val role: String = "client", // "client", "dealer", "contractor", "admin"
    val avatarUrl: String? = null,
    val companyName: String? = null,
    val address: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Order Status for Material Purchase Orders in Supabase 'orders' table.
 */
enum class OrderStatus(val displayNameKh: String, val displayNameEn: String) {
    PENDING("កំពុងរង់ចាំការបញ្ជាក់", "Pending Confirmation"),
    CONFIRMED("បានបញ្ជាក់", "Confirmed"),
    PROCESSING("កំពុងរៀបចំទំនិញ", "Processing"),
    SHIPPED("កំពុងដឹកជញ្ជូន", "Shipped"),
    DELIVERED("បានប្រគល់ជោគជ័យ", "Delivered"),
    CANCELLED("បានបោះបង់", "Cancelled")
}

/**
 * Individual item inside a Material Purchase Order.
 */
data class OrderItem(
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val imageUrl: String? = null
) {
    val subtotal: Double
        get() = unitPrice * quantity
}

/**
 * Material Purchase Order mapped to Supabase 'orders' table (PostgreSQL).
 */
data class MaterialOrder(
    val id: String = UUID.randomUUID().toString(),
    val orderNumber: String = "CMK-${System.currentTimeMillis() % 1000000}",
    val userId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val currency: String = "USD",
    val status: OrderStatus = OrderStatus.PENDING,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Supabase Service Connection Status.
 */
sealed class SupabaseConnectionStatus {
    object Idle : SupabaseConnectionStatus()
    object Connecting : SupabaseConnectionStatus()
    data class Connected(val latencyMs: Long, val projectRef: String) : SupabaseConnectionStatus()
    data class Error(val message: String) : SupabaseConnectionStatus()
}
