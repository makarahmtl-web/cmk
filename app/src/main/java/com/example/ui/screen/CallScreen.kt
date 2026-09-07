package com.example.ui.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.CallState
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

data class UserProfileData(
    val userId: String,
    val name: String,
    val roleOrBio: String,
    val mutualFriendsInfo: String,
    val isOnline: Boolean = true,
    val location: String = "រាជធានីភ្នំពេញ • ប្រទេសកម្ពុជា",
    val isFriend: Boolean = false,
    val isPendingRequest: Boolean = false,
    val isFriendRequestReceived: Boolean = false
)

data class FriendRequestData(
    val id: String,
    val name: String,
    val mutualInfo: String,
    val avatarUrl: String? = null,
    val isOnline: Boolean = true
)

data class SuggestedFriendData(
    val id: String,
    val name: String,
    val roleInfo: String,
    val mutualCount: String
)

data class MyFriendData(
    val id: String,
    val name: String,
    val role: String,
    val isOnline: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val callState by viewModel.webRtcService.callState.collectAsState()
    val isVideo by viewModel.webRtcService.isVideoCall.collectAsState()
    val partnerName by viewModel.webRtcService.currentCallPartner.collectAsState()
    val durationSeconds by viewModel.webRtcService.callDurationSeconds.collectAsState()
    val micMuted by viewModel.webRtcService.isMuted.collectAsState()
    val speakerOn by viewModel.webRtcService.isSpeakerOn.collectAsState()
    val cameraEnabled by viewModel.webRtcService.isCameraOn.collectAsState()

    val durationString = "%02d:%02d".format(durationSeconds / 60, durationSeconds % 60)

    // UI States for Friends Screen
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, ONLINE, SUGGESTIONS, MY_FRIENDS
    var showAllRequests by remember { mutableStateOf(false) }

    // User Profile Dialog state
    var selectedUserProfile by remember { mutableStateOf<UserProfileData?>(null) }

    // Friend Requests state list
    val initialRequests = remember {
        mutableStateListOf(
            FriendRequestData("req_1", "សុខា ដេប៉ូដែក", "មិត្តភក្តិនិងគ្នា 3 • 1 ថ្ងៃ", null, true),
            FriendRequestData("req_2", "ក្រុមហ៊ុន សំណង់ ខេមបូឌា", "មិត្តភក្តិនិងគ្នា 4 • 1 សប្តាហ៍", null, true)
        )
    }

    var confirmedIds by remember { mutableStateOf(setOf<String>()) }
    var deletedIds by remember { mutableStateOf(setOf<String>()) }

    // Suggestions state list
    val initialSuggestions = remember {
        mutableStateListOf(
            SuggestedFriendData("sug_1", "វិស្វករ ចាន់ថន", "វិស្វករគម្រោងសំណង់", "មិត្តភក្តិនិងគ្នា 8 នាក់"),
            SuggestedFriendData("sug_2", "គឹមស៊ាង គ្រឿងសំណង់", "ដេប៉ូលក់ដុំគ្រឿងសំណង់", "មិត្តភក្តិនិងគ្នា 31 នាក់"),
            SuggestedFriendData("sug_3", "សុខ ម៉េងហួរ", "ជាងជំនាញតម្លើងប្រព័ន្ធភ្លើង & ទឹក", "មិត្តភក្តិនិងគ្នា 14 នាក់"),
            SuggestedFriendData("sug_4", "ចាន់ សុភាព", "អ្នករចនាប្លង់ស្ថាបត្យកម្ម", "មិត្តភក្តិនិងគ្នា 19 នាក់")
        )
    }
    var sentSuggestionIds by remember { mutableStateOf(setOf<String>()) }
    var removedSuggestionIds by remember { mutableStateOf(setOf<String>()) }

    // My Friends state list
    val myFriends = remember {
        listOf(
            MyFriendData("f_1", "ហេង ដែកទីប", "អ្នកផ្គត់ផ្គង់ដែកទីបអក្សរ H", true),
            MyFriendData("f_2", "ណារ៉ុង ស៊ីម៉ង់តិ៍", "តំណាងចែកចាយស៊ីម៉ង់តិ៍ K-Cement", true)
        )
    }

    val activeRequests = initialRequests.filter { !deletedIds.contains(it.id) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    shadowElevation = 2.dp,
                    color = CMKDeepBlue
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Main Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "មិត្តភក្តិ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { isSearchActive = !isSearchActive },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Search Friends",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                        .testTag("btn_friends_top_menu")
                                ) {
                                    FourStoryMenuIcon(tint = Color.White)
                                }
                            }
                        }

                        // Search Bar expansion
                        AnimatedVisibility(visible = isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("ស្វែងរកឈ្មោះមិត្តភក្តិ ឬសំណើ...", fontSize = 13.sp, color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CMKDeepBlue) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CMKGoldAccent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFFF8FAFC)
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {


                // Section: Suggestions ("ការណែនាំ")
                if (selectedFilter == "ALL" || selectedFilter == "SUGGESTIONS") {
                    item {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
                            Text(
                                text = "ការណែនាំ (People You May Know)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    val activeSuggestions = initialSuggestions.filter {
                        !removedSuggestionIds.contains(it.id) && (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
                    }

                    items(activeSuggestions, key = { it.id }) { sug ->
                        val isSent = sentSuggestionIds.contains(sug.id)
                        SuggestedFriendCard(
                            data = sug,
                            isSent = isSent,
                            onProfileClick = {
                                selectedUserProfile = UserProfileData(
                                    userId = sug.id,
                                    name = sug.name,
                                    roleOrBio = sug.roleInfo,
                                    mutualFriendsInfo = sug.mutualCount,
                                    isOnline = true,
                                    isPendingRequest = isSent
                                )
                            },
                            onAdd = {
                                sentSuggestionIds = sentSuggestionIds + sug.id
                                Toast.makeText(context, "បានផ្ញើសំណើជាមិត្តភក្តិទៅកាន់ ${sug.name}!", Toast.LENGTH_SHORT).show()
                            },
                            onRemove = {
                                removedSuggestionIds = removedSuggestionIds + sug.id
                            }
                        )
                    }
                }

                // Section: My Friends ("មិត្តភក្តិរបស់អ្នក")
                if (selectedFilter == "ALL" || selectedFilter == "MY_FRIENDS") {
                    item {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)) {
                            Text(
                                text = "មិត្តភក្តិរបស់អ្នក (${myFriends.size + confirmedIds.size} នាក់)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    val filteredFriends = myFriends.filter {
                        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                    }

                    items(filteredFriends, key = { it.id }) { friend ->
                        MyFriendItemCard(
                            data = friend,
                            onProfileClick = {
                                selectedUserProfile = UserProfileData(
                                    userId = friend.id,
                                    name = friend.name,
                                    roleOrBio = friend.role,
                                    mutualFriendsInfo = "មិត្តភក្តិជិតស្និទ្ធ",
                                    isOnline = friend.isOnline,
                                    isFriend = true
                                )
                            },
                            onMessageClick = {
                                Toast.makeText(context, "បើកប្រអប់សារជាមួយ ${friend.name}", Toast.LENGTH_SHORT).show()
                                viewModel.navigateTo(AppScreen.CHAT)
                            },
                            onCallClick = {
                                Toast.makeText(context, "កំពុងហៅចេញទៅកាន់ ${friend.name}...", Toast.LENGTH_SHORT).show()
                                viewModel.startCall(isVideo = false, partnerName = friend.name)
                            }
                        )
                    }
                }
            }

            // User Profile Dialog Modal
            selectedUserProfile?.let { profile ->
                UserProfileDialog(
                    profile = profile,
                    onDismiss = { selectedUserProfile = null },
                    onCallClick = {
                        val pName = profile.name
                        selectedUserProfile = null
                        Toast.makeText(context, "កំពុងហៅចេញទៅកាន់ $pName...", Toast.LENGTH_SHORT).show()
                        viewModel.startCall(isVideo = false, partnerName = pName)
                    },
                    onMessageClick = {
                        val pName = profile.name
                        selectedUserProfile = null
                        Toast.makeText(context, "បើកប្រអប់សារជាមួយ $pName", Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(AppScreen.CHAT)
                    },
                    onAddFriendClick = {
                        sentSuggestionIds = sentSuggestionIds + profile.userId
                        selectedUserProfile = profile.copy(isPendingRequest = true)
                        Toast.makeText(context, "បានផ្ញើសំណើជាមិត្តភក្តិទៅកាន់ ${profile.name}!", Toast.LENGTH_SHORT).show()
                    },
                    onConfirmRequestClick = {
                        confirmedIds = confirmedIds + profile.userId
                        selectedUserProfile = profile.copy(isFriendRequestReceived = false, isFriend = true)
                        Toast.makeText(context, "🎉 បានទទួលយកសំណើជាមិត្តភក្តិពី ${profile.name}!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteRequestClick = {
                        deletedIds = deletedIds + profile.userId
                        selectedUserProfile = null
                        Toast.makeText(context, "លុបសំណើជាមិត្តភក្តិបានជោគជ័យ", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // WebRTC Active Call HUD Overlay
            AnimatedVisibility(
                visible = callState != CallState.IDLE,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ActiveCallHUD(
                    partnerName = partnerName,
                    callState = callState,
                    isVideo = isVideo,
                    duration = durationString,
                    micMuted = micMuted,
                    cameraEnabled = cameraEnabled,
                    speakerOn = speakerOn,
                    onToggleMic = { viewModel.webRtcService.toggleMute() },
                    onToggleCamera = { viewModel.webRtcService.toggleCameraFlip() },
                    onToggleSpeaker = { viewModel.webRtcService.toggleSpeaker() },
                    onHangUp = { viewModel.endCall() }
                )
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    data: FriendRequestData,
    isConfirmed: Boolean,
    onProfileClick: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Clickable)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CMKDeepBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (data.name.isNotEmpty()) data.name.take(2).uppercase() else "FR",
                        color = CMKGoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                if (data.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .background(SafeGreen, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfileClick() }
                ) {
                    Text(
                        text = data.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.mutualInfo,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isConfirmed) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ បានទទួលយកសំណើជាមិត្តភក្តិ",
                            color = Color(0xFF15803D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Confirm button (Blue)
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CMKDeepBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("បញ្ជាក់", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Delete button (Gray)
                        Button(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE2E8F0),
                                contentColor = Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("លុប", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedFriendCard(
    data: SuggestedFriendData,
    isSent: Boolean,
    onProfileClick: () -> Unit,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.name.take(1),
                    color = CMKDeepBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.clickable { onProfileClick() }) {
                    Text(data.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(data.roleInfo, fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(data.mutualCount, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isSent) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ បានផ្ញើសំណើជាមិត្តភក្តិ",
                            color = CMKDeepBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAdd,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CMKDeepBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("បន្ថែមជាមិត្ត", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = onRemove,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("លុបចេញ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyFriendItemCard(
    data: MyFriendData,
    onProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CMKDeepBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = data.name.take(1),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                if (data.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .background(SafeGreen, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                Text(data.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                Text(data.role, fontSize = 11.sp, color = Color(0xFF64748B))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onMessageClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEFF6FF), CircleShape)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Message", tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEFF6FF), CircleShape)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun UserProfileDialog(
    profile: UserProfileData,
    onDismiss: () -> Unit,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onAddFriendClick: () -> Unit,
    onConfirmRequestClick: () -> Unit,
    onDeleteRequestClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var selectedTab by remember { mutableStateOf(0) } // 0 = Posts, 1 = Shop, 2 = About

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- COVER PHOTO & TOP NAVIGATION BAR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    // Cover Banner Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(CMKDeepBlue, Color(0xFF1E293B))
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = "CMK Member Social Profile",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Top Bar Overlay Buttons (Back, Search, More)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                            }
                        }
                    }

                    // Overlapping Circular Profile Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 20.dp)
                            .size(92.dp)
                            .border(3.5.dp, Color.White, CircleShape)
                            .background(CMKDeepBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (profile.name.isNotEmpty()) profile.name.take(2).uppercase() else "CM",
                            color = CMKGoldAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                        if (profile.isOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                                    .background(SafeGreen, CircleShape)
                                    .border(2.5.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }

                // --- PROFILE INFO & DETAILS ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = profile.roleOrBio,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(profile.location, fontSize = 12.sp, color = Color(0xFF64748B))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(profile.mutualFriendsInfo, fontSize = 12.sp, color = Color(0xFF64748B))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- ACTION BUTTONS (Message & Add Friend / Friends Status) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Friend Action Button
                        if (profile.isFriendRequestReceived) {
                            Button(
                                onClick = onConfirmRequestClick,
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("បញ្ជាក់សំណើ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else if (profile.isFriend) {
                            Button(
                                onClick = { },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("មិត្តភក្តិ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = onAddFriendClick,
                                enabled = !profile.isPendingRequest,
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (profile.isPendingRequest) Color(0xFFE2E8F0) else CMKDeepBlue,
                                    contentColor = if (profile.isPendingRequest) Color(0xFF64748B) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (profile.isPendingRequest) Icons.Default.Check else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (profile.isPendingRequest) "បានផ្ញើសំណើ" else "បន្ថែមជាមិត្ត",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Message Button
                        Button(
                            onClick = onMessageClick,
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEFF6FF),
                                contentColor = CMKDeepBlue
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ផ្ញើសារ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Options Button
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color(0xFF475569))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- TAB NAVIGATION (Posts, Shop, About) ---
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = CMKDeepBlue,
                        divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("ការផុស (Posts)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("ហាង (Shop)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("អំពី (About)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- TAB CONTENTS ---
                when (selectedTab) {
                    0 -> ProfilePostsTab(profile = profile)
                    1 -> ProfileShopTab(profile = profile)
                    2 -> ProfileAboutTab(profile = profile)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfilePostsTab(profile: UserProfileData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Post 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(CMKDeepBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Text("២ ម៉ោងមុន • 🌐 សាធារណៈ", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "រីករាយថ្ងៃនេះ! បានបន្តការងារត្រួតពិនិត្យគម្រោងសំណង់យ៉ាងរលូន។ អរគុណក្រុមការងារទាំងអស់ដែលបានប្រឹងប្រែង!",
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B),
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Post Banner Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("រូបភាពគម្រោង និងសកម្មភាពការងារ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ThumbUp, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ចូលចិត្ត 42", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Text("មតិ 12 • ចែករំលែក 5", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }

        // Post 2
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(CMKDeepBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(profile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Text("១ ថ្ងៃមុន • 🌐 សាធារណៈ", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "តើបងប្អូនណាខ្លះត្រូវការប្រឹក្សាយោបល់លើការរចនាប្លង់ ឬការជ្រើសរើសគ្រឿងសំណង់ដែលមានគុណភាព? អាចផ្ញើសារសាកសួរបានបាទ!",
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B),
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun ProfileShopTab(profile: UserProfileData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("ហាង និងផលិតផលរបស់ ${profile.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CMKDeepBlue)
                    Text("ទំនិញ និងសេវាកម្មដែលដាក់តាំងលក់ក្នុង App", fontSize = 11.sp, color = Color(0xFF475569))
                }
            }
        }

        // Product Item 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("សេវាកម្មពិនិត្យ និងរចនាប្លង់គម្រោង", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                    Text("តម្លៃចរចាបាន • ផ្ដល់ជូនការប្រឹក្សា", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$250.00 / គម្រោង", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = SafeGreen)
                }

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("សាកសួរ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileAboutTab(profile: UserProfileData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("អំពីអ្នកប្រើប្រាស់ (About)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("🌐 គណនីនេះបានកំណត់ជា៖ សាធារណៈ (Public)", fontSize = 12.sp, color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Work, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(profile.roleOrBio, fontSize = 12.sp, color = Color(0xFF475569))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HomeWork, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(profile.location, fontSize = 12.sp, color = Color(0xFF475569))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("បានចូលរួម៖ មករា 2025", fontSize = 12.sp, color = Color(0xFF475569))
            }
        }
    }
}

@Composable
fun TextTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = CMKDeepBlue,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun ActiveCallHUD(
    partnerName: String,
    callState: CallState,
    isVideo: Boolean,
    duration: String,
    micMuted: Boolean,
    cameraEnabled: Boolean,
    speakerOn: Boolean,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onHangUp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CMKDeepBlue, CMKSlateDark)
                )
            )
            .testTag("active_call_hud")
    ) {
        if (isVideo && cameraEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, CMKGoldAccent, RoundedCornerShape(32.dp))
                    .background(Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(CMKGoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (partnerName.isNotEmpty()) partnerName.first().toString() else "C",
                                color = CMKDeepBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Live Video Channel Active",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Secure Peer-to-Peer Link (WebRTC)",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .size(90.dp, 130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray)
                        Text("You", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Brush.sweepGradient(listOf(CMKDeepBlue, CMKGoldAccent, CMKDeepBlue)),
                            CircleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CMKSlateDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVideo) Icons.Default.VideocamOff else Icons.Default.Call,
                            contentDescription = null,
                            tint = CMKGoldAccent,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = partnerName.ifEmpty { "CMK Support" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                val statusLabel = when (callState) {
                    CallState.DIALING -> "Dialing Secure Link..."
                    CallState.CONNECTING -> "Connecting Channels..."
                    CallState.ACTIVE -> "Secure Session: $duration"
                    else -> "Disconnected"
                }

                Text(
                    text = statusLabel,
                    fontSize = 14.sp,
                    color = CMKGoldAccent,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleMic,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (micMuted) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (micMuted) Rose500 else Color.White
                    )
                }

                if (isVideo) {
                    IconButton(
                        onClick = onToggleCamera,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (!cameraEnabled) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (cameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Camera",
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = onToggleSpeaker,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (speakerOn) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (speakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onHangUp,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Rose500, CircleShape)
                        .testTag("hangup_button")
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "Hang Up",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

