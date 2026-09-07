package com.example.data.model

import java.util.UUID

enum class MaterialCategory(val displayNameKh: String, val displayNameEn: String = "") {
    BRICKS_TILES("ក្បឿង និងការ៉ូ", "Bricks & Tiles"),
    BATHROOM_PLUMBING("បរិក្ខារបន្ទប់ទឹក", "Bathroom & Plumbing"),
    SAFETY_EQUIPMENT("គ្រឿងបង្ការសុវត្ថិភាព", "Safety Equipment"),
    PAINTS_COATINGS("ថ្នាំលាប និងសារធាតុបិទភ្ជាប់", "Paints & Coatings"),
    HARDWARE_ELECTRICAL("គ្រឿងដែក និងអគ្គិសនី", "Hardware & Electrical")
}

data class MaterialItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: MaterialCategory,
    val imageUrl: String,
    val specDetails: String,
    val bulkPrice: String,
    val availability: String, // "In Stock" / "Limited" / "Pre-Order"
    val deliveryInfo: String = "Delivery within 2-3 business days",
    val location: String = "រាជធានីភ្នំពេញ • ខណ្ឌដូនពេញ"
)

data class FeedPost(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userDisplayName: String,
    val userAvatarInitials: String,
    val timeAgo: String,
    val content: String,
    val imageUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val imageFilter: String? = null
)

data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val userName: String,
    val userAvatarInitials: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CallType {
    AUDIO, VIDEO
}

data class CallRecord(
    val id: String = UUID.randomUUID().toString(),
    val otherPartyName: String,
    val type: CallType,
    val timeString: String,
    val isIncoming: Boolean,
    val durationString: String = "00:00"
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val text: String,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val audioDurationSec: Int = 0,
    val isUnsent: Boolean = false,
    val reaction: String? = null,
    val threadId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
