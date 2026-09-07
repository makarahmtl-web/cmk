package com.example.data.repository

import android.util.Log
import com.example.config.AppConfig
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val avatarInitials: String,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String = "",
    val headline: String = "",
    val workplace: String = "",
    val location: String = "",
    val joinedDate: String = "September 2026",
    val inquiriesCount: Int = 0,
    val activeDealsCount: Int = 0,
    val feedPostsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

class AuthRepository {
    private val TAG = "AuthRepository"
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUserFlow = MutableStateFlow<UserSession?>(null)
    val currentUserFlow: StateFlow<UserSession?> = _currentUserFlow.asStateFlow()

    init {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            
            firebaseAuth?.addAuthStateListener { auth ->
                val fbUser = auth.currentUser
                if (fbUser != null && _currentUserFlow.value == null) {
                    val initials = getInitials(fbUser.displayName ?: fbUser.email ?: "CMK")
                    _currentUserFlow.value = UserSession(
                        uid = fbUser.uid,
                        email = fbUser.email ?: "",
                        displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User",
                        avatarInitials = initials
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase components not fully initialized.")
            _currentUserFlow.value = null
        }
    }

    private fun getInitials(name: String): String {
        return name.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "CMK" }
    }

    suspend fun signIn(email: String, password: String): Result<UserSession> {
        val auth = firebaseAuth
        val db = firestore
        val cleanedEmail = email.trim().lowercase()

        // 1. Try Firestore-backed credentials check first
        if (db != null) {
            try {
                val collection = AppConfig.getCollectionName("user_credentials")
                val doc = db.collection(collection).document(cleanedEmail).get().await()
                if (doc.exists()) {
                    val storedPassword = doc.getString("password")
                    if (storedPassword == password) {
                        val uid = doc.getString("uid") ?: ("uid_" + cleanedEmail.hashCode())
                        val displayName = doc.getString("displayName") ?: cleanedEmail.substringBefore("@")
                        val initials = getInitials(displayName)
                        val session = UserSession(
                            uid = uid,
                            email = cleanedEmail,
                            displayName = displayName,
                            avatarInitials = initials
                        )
                        _currentUserFlow.value = session
                        Log.d(TAG, "Firestore-backed login successful for $cleanedEmail")
                        return Result.success(session)
                    } else {
                        return Result.failure(Exception("The supplied auth credential is incorrect"))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore credential check bypassed: ${e.message}")
            }
        }

        // 2. Fallback to Firebase Auth standard sign-in
        if (auth != null) {
            return try {
                val result = auth.signInWithEmailAndPassword(cleanedEmail, password).await()
                val fbUser = result.user ?: throw Exception("Auth succeeded but returned a null user")
                val initials = getInitials(fbUser.displayName ?: fbUser.email ?: "CMK")
                val session = UserSession(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "",
                    displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User",
                    avatarInitials = initials
                )

                if (db != null) {
                    val collection = AppConfig.getCollectionName("user_credentials")
                    val credentialData = hashMapOf(
                        "email" to cleanedEmail,
                        "password" to password,
                        "displayName" to session.displayName,
                        "uid" to fbUser.uid
                    )
                    db.collection(collection).document(cleanedEmail).set(credentialData)
                }

                _currentUserFlow.value = session
                com.example.data.service.AppAnalytics.logEvent("login", mapOf("method" to "email_password"))
                Result.success(session)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase sign-in error", e)
                com.example.data.service.AppAnalytics.recordException(e, "Firebase email sign-in error")
                Result.failure(Exception("The supplied auth credential is incorrect"))
            }
        } else {
            if (cleanedEmail.contains("@") && password.length >= 6) {
                val displayName = cleanedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                val initials = getInitials(displayName)
                val session = UserSession(
                    uid = "uid_" + cleanedEmail.hashCode(),
                    email = cleanedEmail,
                    displayName = displayName,
                    avatarInitials = initials
                )
                _currentUserFlow.value = session
                return Result.success(session)
            } else {
                return Result.failure(Exception("Invalid credentials. Please enter a valid email and >= 6 character password."))
            }
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<UserSession> {
        val auth = firebaseAuth
        val db = firestore
        val cleanedEmail = email.trim().lowercase()

        // 1. Duplicate Email Validation
        if (db != null) {
            try {
                val collection = AppConfig.getCollectionName("user_credentials")
                val doc = db.collection(collection).document(cleanedEmail).get().await()
                if (doc.exists()) {
                    return Result.failure(Exception("DUPLICATE_EMAIL"))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firestore duplicate email check skipped: ${e.message}")
            }
        }

        // 2. Proceed with user creation
        if (auth != null) {
            return try {
                val result = auth.createUserWithEmailAndPassword(cleanedEmail, password).await()
                val fbUser = result.user ?: throw Exception("Registration failed, null user")
                
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                fbUser.updateProfile(profileUpdates).await()
                
                val initials = getInitials(displayName)
                val session = UserSession(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "",
                    displayName = displayName,
                    avatarInitials = initials
                )

                if (db != null) {
                    val collection = AppConfig.getCollectionName("user_credentials")
                    val credentialData = hashMapOf(
                        "email" to cleanedEmail,
                        "password" to password,
                        "displayName" to displayName,
                        "uid" to fbUser.uid
                    )
                    db.collection(collection).document(cleanedEmail).set(credentialData).await()
                }

                _currentUserFlow.value = session
                com.example.data.service.AppAnalytics.logEvent("sign_up", mapOf("method" to "email_password"))
                Result.success(session)
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                return Result.failure(Exception("DUPLICATE_EMAIL"))
            } catch (e: Exception) {
                Log.e(TAG, "Firebase registration error", e)
                com.example.data.service.AppAnalytics.recordException(e, "Firebase registration error")
                Result.failure(e)
            }
        } else {
            if (cleanedEmail.contains("@") && password.length >= 6 && displayName.isNotBlank()) {
                val initials = getInitials(displayName)
                val session = UserSession(
                    uid = "uid_" + cleanedEmail.hashCode(),
                    email = cleanedEmail,
                    displayName = displayName,
                    avatarInitials = initials
                )
                _currentUserFlow.value = session
                return Result.success(session)
            } else {
                return Result.failure(Exception("Please complete all fields. Password must be >= 6 characters."))
            }
        }
    }

    suspend fun signInAsDemoUser(): Result<UserSession> {
        if (!AppConfig.IS_DEMO_BYPASS_ENABLED) {
            return Result.failure(Exception("Developer Demo mode is disabled in Production Release."))
        }
        val session = UserSession(
            uid = "demo_developer_uid",
            email = "developer@demo.com",
            displayName = "Developer Demo",
            avatarInitials = "DD",
            bio = "Developer Mode"
        )
        _currentUserFlow.value = session
        return Result.success(session)
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<UserSession> {
        val auth = firebaseAuth
        if (auth != null) {
            return try {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val fbUser = result.user ?: throw Exception("Google auth failed, null user")
                val initials = getInitials(fbUser.displayName ?: fbUser.email ?: "G")
                val session = UserSession(
                    uid = fbUser.uid,
                    email = fbUser.email ?: "",
                    displayName = fbUser.displayName ?: "Google User",
                    avatarInitials = initials
                )
                _currentUserFlow.value = session
                com.example.data.service.AppAnalytics.logEvent("login", mapOf("method" to "google"))
                Result.success(session)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Google login error", e)
                com.example.data.service.AppAnalytics.recordException(e, "Firebase Google login error")
                Result.failure(e)
            }
        } else {
            return Result.failure(Exception("Google Sign-In requires active Google Play Services credentials."))
        }
    }

    fun signOut() {
        com.example.data.service.AppAnalytics.logEvent("logout")
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error in Firebase sign-out", e)
        }
        _currentUserFlow.value = null
    }

    suspend fun updateUserProfile(updated: UserSession): Result<Boolean> {
        _currentUserFlow.value = updated
        val db = firestore
        if (db != null) {
            try {
                val collection = AppConfig.getCollectionName("user_profiles")
                val profileData = hashMapOf(
                    "uid" to updated.uid,
                    "email" to updated.email,
                    "displayName" to updated.displayName,
                    "avatarInitials" to updated.avatarInitials,
                    "avatarUrl" to updated.avatarUrl,
                    "coverUrl" to updated.coverUrl,
                    "bio" to updated.bio,
                    "headline" to updated.headline,
                    "workplace" to updated.workplace,
                    "location" to updated.location,
                    "joinedDate" to updated.joinedDate,
                    "inquiriesCount" to updated.inquiriesCount,
                    "activeDealsCount" to updated.activeDealsCount,
                    "feedPostsCount" to updated.feedPostsCount
                )
                db.collection(collection).document(updated.uid).set(profileData).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist profile to Firestore: ${e.message}")
            }
        }
        return Result.success(true)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Boolean> {
        val auth = firebaseAuth
        val db = firestore
        val cleanedEmail = email.trim().lowercase()

        if (db != null) {
            try {
                val collection = AppConfig.getCollectionName("user_credentials")
                val docRef = db.collection(collection).document(cleanedEmail)
                val doc = docRef.get().await()
                if (doc.exists()) {
                    docRef.update("password", newPassword).await()
                    Log.d(TAG, "Password successfully reset and synchronized in Firestore for $cleanedEmail")
                } else {
                    val credentialData = hashMapOf(
                        "email" to cleanedEmail,
                        "password" to newPassword,
                        "displayName" to cleanedEmail.substringBefore("@"),
                        "uid" to "uid_" + cleanedEmail.hashCode()
                    )
                    docRef.set(credentialData).await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update Firestore credential override", e)
                return Result.failure(e)
            }
        }

        if (auth != null) {
            try {
                val user = auth.currentUser
                if (user != null && user.email?.trim()?.lowercase() == cleanedEmail) {
                    user.updatePassword(newPassword).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Active user session update skipped: ${e.message}")
            }
        }

        return Result.success(true)
    }
}

// Custom lightweight Task-to-Coroutine adapter for Play Services
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: RuntimeException("Task operation failed"))
        }
    }
}
