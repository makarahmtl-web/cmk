package com.example.data.repository

import android.util.Log
import com.example.config.AppConfig
import com.example.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreRepository {
    private val TAG = "FirestoreRepository"
    private var firestore: FirebaseFirestore? = null

    // រក្សាទុក Registration ដើម្បី remove() ការពារ Memory Leak (ចំណុច ញ)
    private var postsListenerRegistration: ListenerRegistration? = null

    // Variables សម្រាប់គ្រប់គ្រង Pagination (ចំណុច ដ)
    private var lastVisiblePostDocument: DocumentSnapshot? = null
    private var isLastPageReached = false
    private var isLoadingPagination = false
    private val PAGE_SIZE = 20L

    // Reactive states
    private val _postsState = MutableStateFlow<List<FeedPost>>(emptyList())
    val postsState: StateFlow<List<FeedPost>> = _postsState.asStateFlow()

    private val _chatMessagesState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessagesState: StateFlow<List<ChatMessage>> = _chatMessagesState.asStateFlow()

    private val _callRecordsState = MutableStateFlow<List<CallRecord>>(emptyList())
    val callRecordsState: StateFlow<List<CallRecord>> = _callRecordsState.asStateFlow()

    private val _materialCatalogueState = MutableStateFlow<List<MaterialItem>>(emptyList())
    val materialCatalogueState: StateFlow<List<MaterialItem>> = _materialCatalogueState.asStateFlow()

    val materialCatalogue: List<MaterialItem>
        get() = _materialCatalogueState.value

    fun addNewMaterialItem(item: MaterialItem) {
        _materialCatalogueState.value = _materialCatalogueState.value + item
    }

    private val initialCatalogue: List<MaterialItem> = emptyList()

    init {
        _materialCatalogueState.value = initialCatalogue
        try {
            firestore = FirebaseFirestore.getInstance()
            Log.d(TAG, "Firebase Firestore initialized.")
            
            // ចាប់ផ្ដើមទាញយកទំព័រដំបូង (Pagination)
            loadInitialPosts()
            // ស្តាប់ការសន្ទនា Real-time Messages
            listenToChatMessages()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore offline or missing configuration.", e)
        }
    }

    private var chatListenerRegistration: ListenerRegistration? = null

    /**
     * ស្តាប់សារសន្ទនា Real-time ពី Firestore (Chats Collection)
     */
    fun listenToChatMessages() {
        val db = firestore ?: return
        val collection = AppConfig.getCollectionName("chats")
        chatListenerRegistration?.remove()
        chatListenerRegistration = db.collection(collection)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore '$collection' chat access error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    _chatMessagesState.value = list
                }
            }
    }

    // =========================================================================
    // ចំណុច ដ៖ PAGINATION & REAL-TIME LISTENER
    // =========================================================================

    /**
     * ទាញយក Posts ទំព័រដំបូង (២០ posts ដំបូង) និងស្តាប់ការប្រែប្រួល Real-time
     */
    fun loadInitialPosts() {
        val db = firestore ?: return
        val collection = AppConfig.getCollectionName("posts")

        // លុប Listener ចាស់ចេញមុននឹងបង្កើតថ្មី (ចំណុច ញ)
        postsListenerRegistration?.remove()

        isLastPageReached = false
        lastVisiblePostDocument = null

        val query = db.collection(collection)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE)

        // រក្សាទុក registration ដើម្បីអាច clear បាននៅពេលក្រោយ (ចំណុច ញ)
        postsListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Firestore '$collection' access error: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FeedPost::class.java)?.copy(id = doc.id)
                }
                
                if (snapshot.documents.isNotEmpty()) {
                    lastVisiblePostDocument = snapshot.documents.last()
                }
                if (snapshot.documents.size < PAGE_SIZE) {
                    isLastPageReached = true
                }

                _postsState.value = list
            }
        }
    }

    /**
     * ទាញយកទំព័របន្ទាប់ទៀត (Next Page) នៅពេល User វ៉ៃ/អូសចុះក្រោម (Scroll to bottom)
     */
    suspend fun loadMorePosts() {
        val db = firestore ?: return
        val lastDoc = lastVisiblePostDocument
        if (isLoadingPagination || isLastPageReached || lastDoc == null) return

        isLoadingPagination = true
        val collection = AppConfig.getCollectionName("posts")

        try {
            val snapshot = db.collection(collection)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(PAGE_SIZE)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val newPosts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FeedPost::class.java)?.copy(id = doc.id)
                }
                lastVisiblePostDocument = snapshot.documents.last()
                
                // បញ្ចូល Post ថ្មីទៅក្នុងបញ្ជីចាស់
                _postsState.value = _postsState.value + newPosts

                if (snapshot.documents.size < PAGE_SIZE) {
                    isLastPageReached = true
                }
            } else {
                isLastPageReached = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load more posts", e)
        } finally {
            isLoadingPagination = false
        }
    }

    // =========================================================================
    // ចំណុច ឈ៖ ការលុប និង កែប្រែទិន្នន័យ (UPDATE / DELETE OPERATIONS)
    // =========================================================================

    /**
     * កែប្រែអត្ថបទ (Edit Post)
     */
    suspend fun updatePost(postId: String, newContent: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("posts")

        // Update local state ភ្លាមៗ (Optimistic UI)
        val currentPosts = _postsState.value.toMutableList()
        val index = currentPosts.indexOfFirst { it.id == postId }
        if (index != -1) {
            currentPosts[index] = currentPosts[index].copy(content = newContent)
            _postsState.value = currentPosts
        }

        if (db != null) {
            return try {
                db.collection(collection).document(postId)
                    .update("content", newContent)
                    .await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update post: $postId", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    /**
     * លុបអត្ថបទ (Delete Post)
     */
    suspend fun deletePost(postId: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("posts")

        // លុបចេញពី local state ភ្លាមៗ
        _postsState.value = _postsState.value.filterNot { it.id == postId }

        if (db != null) {
            return try {
                db.collection(collection).document(postId).delete().await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete post: $postId", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    /**
     * កែប្រែប្រអប់មតិយោបល់ (Edit Comment)
     */
    suspend fun updateComment(postId: String, commentId: String, newText: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("posts")

        if (db != null) {
            return try {
                db.collection(collection).document(postId)
                    .collection("comments").document(commentId)
                    .update("content", newText)
                    .await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update comment: $commentId", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    /**
     * លុបប្រអប់មតិយោបល់ (Delete Comment) និងបន្ថយចំនួន Comments Count
     */
    suspend fun deleteComment(postId: String, commentId: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("posts")

        // កាត់បន្ថយចំនួន comment count ក្នុង local state
        val posts = _postsState.value.toMutableList()
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = posts[index]
            posts[index] = post.copy(commentsCount = maxOf(0, post.commentsCount - 1))
            _postsState.value = posts
        }

        if (db != null) {
            return try {
                val postRef = db.collection(collection).document(postId)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentCount = snapshot.getLong("commentsCount") ?: 1
                    transaction.update(postRef, "commentsCount", maxOf(0, currentCount - 1))
                    transaction.delete(postRef.collection("comments").document(commentId))
                }.await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete comment: $commentId", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    /**
     * លុបសារសន្ទនា (Delete Chat Message)
     */
    suspend fun deleteChatMessage(messageId: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("chats")

        // លុបចេញពី local state
        _chatMessagesState.value = _chatMessagesState.value.filterNot { it.id == messageId }

        if (db != null) {
            return try {
                db.collection(collection).document(messageId).delete().await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete chat message: $messageId", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    // =========================================================================
    // ចំណុច ញ៖ CLEANUP FUNCTION (PREVENT MEMORY LEAKS)
    // =========================================================================

    /**
     * ត្រូវហៅ function នេះនៅពេល ViewModel ត្រូវកម្ទេចចោល (onCleared)
     * ដើម្បី Detach Real-time listeners ទាំងអស់ចេញពី Firestore
     */
    fun cleanup() {
        postsListenerRegistration?.remove()
        postsListenerRegistration = null
        Log.d(TAG, "Firestore listeners cleaned up successfully.")
    }

    // =========================================================================
    // EXISTING FUNCTIONS
    // =========================================================================

    suspend fun createPost(user: UserSession, content: String, imageUrls: List<String> = emptyList(), imageFilter: String? = null): Result<FeedPost> {
        val newPost = FeedPost(
            id = UUID.randomUUID().toString(),
            userId = user.uid,
            userDisplayName = user.displayName,
            userAvatarInitials = user.avatarInitials,
            timeAgo = "Just now",
            content = content,
            imageUrl = imageUrls.firstOrNull(),
            imageUrls = imageUrls,
            likesCount = 0,
            commentsCount = 0,
            isLikedByMe = false,
            timestamp = System.currentTimeMillis(),
            imageFilter = imageFilter
        )

        // Optimistic local update so UI reflects the new post immediately
        val currentList = _postsState.value.toMutableList()
        currentList.add(0, newPost)
        _postsState.value = currentList

        val db = firestore
        val collection = AppConfig.getCollectionName("posts")
        if (db != null) {
            try {
                db.collection(collection).document(newPost.id).set(newPost).await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore write warning ($collection): ${e.message}. Preserved in local & Supabase.")
            }
        }
        return Result.success(newPost)
    }

    suspend fun toggleLike(postId: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("posts")
        val currentList = _postsState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == postId }
        if (index == -1) return Result.failure(Exception("Post not found"))

        val post = currentList[index]
        val nextLiked = !post.isLikedByMe
        val nextLikesCount = if (nextLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
        val updatedPost = post.copy(isLikedByMe = nextLiked, likesCount = nextLikesCount)

        currentList[index] = updatedPost
        _postsState.value = currentList

        if (db != null) {
            return try {
                db.collection(collection).document(postId)
                    .update(
                        "likesCount", nextLikesCount,
                        "isLikedByMe", nextLiked
                    )
                Result.success(nextLiked)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update like in Firestore ($collection)", e)
                Result.failure(e)
            }
        }
        return Result.success(nextLiked)
    }

    fun getCommentsForPost(postId: String): Flow<List<PostComment>> = callbackFlow {
        val db = firestore
        val collection = AppConfig.getCollectionName("posts")
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
        } else {
            val listener = db.collection(collection).document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Comments listener error: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val comments = mutableListOf<PostComment>()
                    if (snapshot != null) {
                        for (doc in snapshot.documents) {
                            val c = doc.toObject(PostComment::class.java)
                            if (c != null) {
                                comments.add(c.copy(id = doc.id))
                            }
                        }
                    }
                    trySend(comments)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun addComment(postId: String, user: UserSession, text: String): Result<PostComment> {
        val comment = PostComment(
            id = UUID.randomUUID().toString(),
            postId = postId,
            userName = user.displayName,
            userAvatarInitials = user.avatarInitials,
            content = text,
            timestamp = System.currentTimeMillis()
        )

        val posts = _postsState.value.toMutableList()
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = posts[index]
            posts[index] = post.copy(commentsCount = post.commentsCount + 1)
            _postsState.value = posts
        }

        val db = firestore
        val collection = AppConfig.getCollectionName("posts")
        if (db != null) {
            return try {
                val postRef = db.collection(collection).document(postId)
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentCommentsCount = snapshot.getLong("commentsCount") ?: 0
                    transaction.update(postRef, "commentsCount", currentCommentsCount + 1)
                    transaction.set(postRef.collection("comments").document(comment.id), comment)
                }.await()
                Result.success(comment)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add comment to Firestore", e)
                Result.failure(e)
            }
        }
        return Result.success(comment)
    }

    suspend fun sendChatMessage(
        user: UserSession,
        text: String,
        imageUrl: String? = null,
        audioUrl: String? = null,
        audioDurationSec: Int = 0,
        threadId: String? = null
    ): Result<ChatMessage> {
        val newMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = user.uid,
            senderName = user.displayName,
            text = text,
            imageUrl = imageUrl,
            audioUrl = audioUrl,
            audioDurationSec = audioDurationSec,
            threadId = threadId,
            timestamp = System.currentTimeMillis()
        )

        val currentList = _chatMessagesState.value.toMutableList()
        currentList.add(newMsg)
        _chatMessagesState.value = currentList

        val db = firestore
        val collection = AppConfig.getCollectionName("chats")
        if (db != null) {
            return try {
                db.collection(collection).document(newMsg.id).set(newMsg).await()
                Result.success(newMsg)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message to Firestore ($collection)", e)
                Result.failure(e)
            }
        }
        return Result.success(newMsg)
    }

    fun unsendMessage(messageId: String) {
        val currentList = _chatMessagesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == messageId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                isUnsent = true,
                text = "សារត្រូវបានលុប (Message unsent)",
                imageUrl = null,
                audioUrl = null
            )
            _chatMessagesState.value = currentList
        }
        val db = firestore
        val collection = AppConfig.getCollectionName("chats")
        if (db != null) {
            db.collection(collection).document(messageId).update(
                mapOf(
                    "isUnsent" to true,
                    "text" to "សារត្រូវបានលុប (Message unsent)",
                    "imageUrl" to null,
                    "audioUrl" to null
                )
            )
        }
    }

    fun reactToMessage(messageId: String, reaction: String?) {
        val currentList = _chatMessagesState.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == messageId }
        if (index != -1) {
            val currentReaction = currentList[index].reaction
            val newReaction = if (currentReaction == reaction) null else reaction
            currentList[index] = currentList[index].copy(reaction = newReaction)
            _chatMessagesState.value = currentList
        }
        val db = firestore
        val collection = AppConfig.getCollectionName("chats")
        if (db != null) {
            db.collection(collection).document(messageId).update("reaction", reaction)
        }
    }

    fun addCallRecord(record: CallRecord) {
        val current = _callRecordsState.value.toMutableList()
        current.add(0, record)
        _callRecordsState.value = current

        val db = firestore
        val collection = AppConfig.getCollectionName("calls")
        if (db != null) {
            db.collection(collection).document(record.id).set(record)
                .addOnFailureListener { e -> Log.e(TAG, "Error adding call record", e) }
        }
    }

    suspend fun blockUserInFirestore(blockerId: String, blockedName: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("blockedUsers")
        if (db != null) {
            return try {
                val docId = "${blockerId}_${blockedName}"
                val data = mapOf(
                    "blockerId" to blockerId,
                    "blockedName" to blockedName,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection(collection).document(docId).set(data).await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to block user in Firestore ($collection)", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    suspend fun unblockUserInFirestore(blockerId: String, blockedName: String): Result<Boolean> {
        val db = firestore
        val collection = AppConfig.getCollectionName("blockedUsers")
        if (db != null) {
            return try {
                val docId = "${blockerId}_${blockedName}"
                db.collection(collection).document(docId).delete().await()
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unblock user in Firestore ($collection)", e)
                Result.failure(e)
            }
        }
        return Result.success(true)
    }

    fun listenToBlockedUsers(blockerId: String, onUpdate: (Set<String>) -> Unit) {
        val db = firestore ?: return
        val collection = AppConfig.getCollectionName("blockedUsers")
        db.collection(collection)
            .whereEqualTo("blockerId", blockerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to blocked users: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val names = snapshot.documents.mapNotNull { it.getString("blockedName") }.toSet()
                    onUpdate(names)
                }
            }
    }
}
