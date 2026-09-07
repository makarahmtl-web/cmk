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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.CMKDeepBlue
import com.example.ui.theme.CMKGoldAccent
import com.example.ui.theme.FourStoryMenuIcon
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Model for Active Online Users
data class OnlineUser(
    val id: String,
    val name: String,
    val shortName: String,
    val initials: String,
    val avatarBgColor: Color,
    val isSelf: Boolean = false,
    val isLive: Boolean = false
)

// Model for X-style Chat Conversation Thread
data class ChatThread(
    val id: String,
    val partnerName: String,
    val handle: String,
    val isVerified: Boolean = true,
    val isGroup: Boolean = false,
    val avatarBgColor: Color,
    val initials: String,
    val lastMessage: String,
    val timeAgo: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = true
)

// Model for App Contacts for New Message Screen
data class ChatContact(
    val id: String,
    val name: String,
    val handle: String,
    val role: String,
    val isVerified: Boolean = true,
    val initials: String,
    val avatarBgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsState()
    val user by viewModel.currentUserSession.collectAsState()
    
    // Search query & active category filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Unread, Direct, Groups, Requests
    var showDropdownFilter by remember { mutableStateOf(false) }

    // Dialog & Screen navigation state
    var showNewMessageSheet by remember { mutableStateOf(false) }
    var activeThread by remember { mutableStateOf<ChatThread?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showChatSettingsSheet by remember { mutableStateOf(false) }

    // Sample contacts from OUR APP (strictly no external photos or external user data)
    val appContacts = remember {
        listOf(
            ChatContact("c1", "Socheata (CMK Sales)", "@socheata_cmk", "CMK Official Sales Manager", true, "SC", Color(0xFF1E40AF)),
            ChatContact("c2", "Phalla (Contractor Admin)", "@phalla_construction", "Senior Civil Contractor", true, "PC", Color(0xFF047857)),
            ChatContact("c3", "Meng (Steel Supplier)", "@meng_steel_kh", "Deformed Steel Distributor", true, "MS", Color(0xFFB91C1C)),
            ChatContact("c4", "Mr. MAKAR CH", "@CHHUOYMAKARA64", "CMK VIP Client", true, "MC", Color(0xFFD97706)),
            ChatContact("c5", "Kirirom Steel Depot", "@kirirom_steel", "Building Materials Store", true, "KS", Color(0xFF4338CA)),
            ChatContact("c6", "Heng Cement Supplier", "@heng_cement", "Portland Cement Wholesale", true, "HC", Color(0xFF0369A1)),
            ChatContact("c7", "Borey Grand Project Team", "@borey_grand_team", "Infrastructure Engineers", true, "BG", Color(0xFF6D28D9))
        )
    }

    // List of Active / Online Friends (Facebook Messenger Active Now Style)
    val onlineUsers = remember(user) {
        listOf(
            OnlineUser(
                id = user?.uid ?: "self",
                name = user?.displayName ?: "Your Status",
                shortName = "អ្នក",
                initials = user?.avatarInitials ?: "ME",
                avatarBgColor = CMKDeepBlue,
                isSelf = true
            ),
            OnlineUser(
                id = "on_1",
                name = "Socheata (CMK Sales)",
                shortName = "Socheata",
                initials = "SC",
                avatarBgColor = Color(0xFF1E40AF)
            ),
            OnlineUser(
                id = "on_2",
                name = "Phalla (Contractor Admin)",
                shortName = "Phalla",
                initials = "PC",
                avatarBgColor = Color(0xFF047857)
            ),
            OnlineUser(
                id = "on_3",
                name = "Mr. MAKAR CH",
                shortName = "MAKAR",
                initials = "MC",
                avatarBgColor = Color(0xFFD97706)
            ),
            OnlineUser(
                id = "on_4",
                name = "វិស្វករ ចាន់ថន",
                shortName = "ចាន់ថន",
                initials = "CT",
                avatarBgColor = Color(0xFF0284C7)
            ),
            OnlineUser(
                id = "on_5",
                name = "ហេង ដែកទីប",
                shortName = "ហេង",
                initials = "HD",
                avatarBgColor = Color(0xFF4338CA)
            ),
            OnlineUser(
                id = "on_6",
                name = "ណារ៉ុង ស៊ីម៉ង់តិ៍",
                shortName = "ណារ៉ុង",
                initials = "NS",
                avatarBgColor = Color(0xFF0D9488)
            ),
            OnlineUser(
                id = "on_7",
                name = "គឹមស៊ាង គ្រឿងសំណង់",
                shortName = "គឹមស៊ាង",
                initials = "KS",
                avatarBgColor = Color(0xFFE11D48)
            )
        )
    }

    // Default chat threads list matching X social DMs layout
    var chatThreads by remember {
        mutableStateOf(
            listOf(
                ChatThread("t1", "Socheata (CMK Sales)", "@socheata_cmk", true, false, Color(0xFF1E40AF), "SC", "Hello! Thanks for reaching out to CMK Construction Materials...", "10m", 1, true),
                ChatThread("t2", "Phalla (Contractor Admin)", "@phalla_construction", true, false, Color(0xFF047857), "PC", "Can you send the rebar quotation for Borey Grand project?", "2h", 2, true),
                ChatThread("t3", "Meng (Steel Supplier)", "@meng_steel_kh", true, false, Color(0xFFB91C1C), "MS", "We have 50 tons of Deformed Steel Bars ready for dispatch.", "1d", 0, false),
                ChatThread("t4", "Mr. MAKAR CH", "@CHHUOYMAKARA64", true, false, Color(0xFFD97706), "MC", "ជំរាបសួរ! តើអាចពិនិត្យតម្លៃស៊ីម៉ង់ត៍ CMK Bulk បានទេ?", "3d", 0, true),
                ChatThread("t5", "Borey Grand Project Team", "@borey_grand_team", true, true, Color(0xFF6D28D9), "BG", "វិស្វករបានត្រួតពិនិត្យគ្រឹះបេតុងរួចរាល់ហើយ", "1w", 0, false)
            )
        )
    }

    // Filter threads based on search and category filter
    val filteredThreads = remember(chatThreads, searchQuery, selectedFilter) {
        chatThreads.filter { thread ->
            val matchesQuery = thread.partnerName.contains(searchQuery, ignoreCase = true) ||
                    thread.handle.contains(searchQuery, ignoreCase = true) ||
                    thread.lastMessage.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedFilter) {
                "Unread" -> thread.unreadCount > 0
                "Direct" -> !thread.isGroup
                "Groups" -> thread.isGroup
                "Requests" -> false // Sample requests empty
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            if (activeThread == null) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { showDropdownFilter = !showDropdownFilter }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Chat",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 19.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (selectedFilter) {
                                                    "Unread" -> "Unread"
                                                    "Direct" -> "Direct"
                                                    "Groups" -> "Groups"
                                                    "Requests" -> "Requests"
                                                    else -> "All"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CMKGoldAccent
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Filter Menu",
                                                tint = CMKGoldAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "CMK Direct Messages & Social Network",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            // X-Style Dropdown Filter Popup (Matches Screenshot 3)
                            DropdownMenu(
                                expanded = showDropdownFilter,
                                onDismissRequest = { showDropdownFilter = false },
                                modifier = Modifier
                                    .background(Color(0xFF0F172A))
                                    .width(200.dp)
                            ) {
                                val filterOptions = listOf(
                                    "All" to Pair("All Messages", Icons.Default.ChatBubbleOutline),
                                    "Unread" to Pair("Unread", Icons.Default.MarkUnreadChatAlt),
                                    "Direct" to Pair("Direct", Icons.Default.Person),
                                    "Groups" to Pair("Groups", Icons.Default.Group),
                                    "Requests" to Pair("Requests", Icons.Default.Inbox),
                                    "Settings" to Pair("Settings", Icons.Default.Settings),
                                    "MarkAll" to Pair("Mark all as read", Icons.Default.DoneAll)
                                )

                                filterOptions.forEach { (key, pair) ->
                                    val (label, icon) = pair
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = if (selectedFilter == key) CMKGoldAccent else Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = label,
                                                        color = if (selectedFilter == key) CMKGoldAccent else Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (selectedFilter == key) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                                if (selectedFilter == key) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = CMKGoldAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            showDropdownFilter = false
                                            if (key == "Settings") {
                                                showChatSettingsSheet = true
                                            } else if (key == "MarkAll") {
                                                chatThreads = chatThreads.map { it.copy(unreadCount = 0) }
                                                Toast.makeText(context, "Marked all messages as read", Toast.LENGTH_SHORT).show()
                                            } else {
                                                selectedFilter = key
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("btn_chat_top_menu")
                        ) {
                            FourStoryMenuIcon(tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CMKDeepBlue)
                )
            }
        },
        floatingActionButton = {
            if (activeThread == null) {
                FloatingActionButton(
                    onClick = { showNewMessageSheet = true },
                    containerColor = CMKGoldAccent,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkUnreadChatAlt,
                        contentDescription = "New Message",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (activeThread == null) {
                // --- MAIN CONVERSATIONS LIST (X DM STYLE) ---
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Direct Messages...", fontSize = 13.sp, color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CMKDeepBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )

                    // Active Filter Chip Display
                    if (selectedFilter != "All") {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtered by: ",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            AssistChip(
                                onClick = { selectedFilter = "All" },
                                label = { Text(selectedFilter, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Filter",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(containerColor = CMKGoldAccent.copy(alpha = 0.2f))
                            )
                        }
                    }

                    // --- ACTIVE / ONLINE NOW HORIZONTAL BAR (MESSENGER STYLE) ---
                    if (searchQuery.isEmpty() && (selectedFilter == "All" || selectedFilter == "Direct")) {
                        ActiveNowOnlineBar(
                            onlineUsers = onlineUsers,
                            onUserClick = { clickedUser ->
                                if (clickedUser.isSelf) {
                                    Toast.makeText(context, "ស្ថានភាពរបស់អ្នក៖ កំពុង Online 🟢", Toast.LENGTH_SHORT).show()
                                } else {
                                    val existing = chatThreads.find {
                                        it.partnerName.contains(clickedUser.shortName, ignoreCase = true) ||
                                                it.partnerName.contains(clickedUser.name, ignoreCase = true)
                                    }
                                    if (existing != null) {
                                        activeThread = existing
                                    } else {
                                        val newThread = ChatThread(
                                            id = UUID.randomUUID().toString(),
                                            partnerName = clickedUser.name,
                                            handle = "@${clickedUser.shortName.lowercase().replace(" ", "_")}",
                                            isVerified = true,
                                            isGroup = false,
                                            avatarBgColor = clickedUser.avatarBgColor,
                                            initials = clickedUser.initials,
                                            lastMessage = "បានចាប់ផ្តើមការសន្ទនាផ្ទាល់",
                                            timeAgo = "អម្បាញ់មិញ",
                                            unreadCount = 0,
                                            isOnline = true
                                        )
                                        chatThreads = listOf(newThread) + chatThreads
                                        activeThread = newThread
                                    }
                                    Toast.makeText(context, "ជជែកជាមួយ ${clickedUser.shortName}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    }

                    // Conversations List
                    if (filteredThreads.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No direct messages found",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Start a new conversation with suppliers, contractors, or sales reps",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showNewMessageSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)
                                ) {
                                    Text("New Message", color = Color.White)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredThreads) { thread ->
                                XChatItemRow(
                                    thread = thread,
                                    onClick = {
                                        // Clear unread count on click
                                        chatThreads = chatThreads.map {
                                            if (it.id == thread.id) it.copy(unreadCount = 0) else it
                                        }
                                        activeThread = thread
                                    }
                                )
                                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                            }
                        }
                    }
                }
            } else {
                // --- ACTIVE DIRECT MESSAGE CONVERSATION THREAD ---
                XConversationThreadView(
                    thread = activeThread!!,
                    viewModel = viewModel,
                    onBack = { activeThread = null }
                )
            }

            // --- NEW MESSAGE SHEET / DIALOG (MATCHES SCREENSHOT 2) ---
            if (showNewMessageSheet) {
                XNewMessageSheet(
                    contacts = appContacts,
                    onDismiss = { showNewMessageSheet = false },
                    onCreateGroup = {
                        showNewMessageSheet = false
                        showCreateGroupDialog = true
                    },
                    onSelectContact = { contact ->
                        showNewMessageSheet = false
                        // Find or create active thread
                        val existing = chatThreads.find { it.partnerName == contact.name }
                        if (existing != null) {
                            activeThread = existing
                        } else {
                            val newT = ChatThread(
                                id = UUID.randomUUID().toString(),
                                partnerName = contact.name,
                                handle = contact.handle,
                                isVerified = contact.isVerified,
                                isGroup = false,
                                avatarBgColor = contact.avatarBgColor,
                                initials = contact.initials,
                                lastMessage = "Started direct message conversation",
                                timeAgo = "Just now",
                                unreadCount = 0,
                                isOnline = true
                            )
                            chatThreads = listOf(newT) + chatThreads
                            activeThread = newT
                        }
                    }
                )
            }

            // --- CREATE GROUP DIALOG ---
            if (showCreateGroupDialog) {
                var groupName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showCreateGroupDialog = false },
                    title = {
                        Text("Create a Group", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    },
                    text = {
                        Column {
                            Text("Name your construction group or project channel:", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = groupName,
                                onValueChange = { groupName = it },
                                placeholder = { Text("e.g. Borey Grand Engineering Team") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (groupName.isNotBlank()) {
                                    val newGroup = ChatThread(
                                        id = UUID.randomUUID().toString(),
                                        partnerName = groupName,
                                        handle = "@${groupName.lowercase().replace(" ", "_")}",
                                        isVerified = true,
                                        isGroup = true,
                                        avatarBgColor = Color(0xFF6D28D9),
                                        initials = groupName.take(2).uppercase(),
                                        lastMessage = "Group created successfully!",
                                        timeAgo = "Just now",
                                        unreadCount = 0,
                                        isOnline = true
                                    )
                                    chatThreads = listOf(newGroup) + chatThreads
                                    showCreateGroupDialog = false
                                    activeThread = newGroup
                                    Toast.makeText(context, "Group created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateGroupDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }

            // --- CHAT SETTINGS & PREFERENCES SHEET ---
            if (showChatSettingsSheet) {
                XChatSettingsSheet(
                    onDismiss = { showChatSettingsSheet = false }
                )
            }
        }
    }
}

// Composable for X Chat Item Row
@Composable
fun XChatItemRow(
    thread: ChatThread,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Circle with Online Dot
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(thread.avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = thread.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            if (thread.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name, Handle, Message Preview & Time
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = thread.partnerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (thread.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Partner",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (thread.isGroup) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group",
                            tint = Color.Gray,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = thread.timeAgo,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            Text(
                text = thread.handle,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = thread.lastMessage,
                    fontSize = 12.sp,
                    color = if (thread.unreadCount > 0) Color(0xFF0F172A) else Color(0xFF64748B),
                    fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (thread.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(CMKGoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = thread.unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 3-Dot Dropdown Menu for Chat item options
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Chat Options",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (thread.unreadCount > 0) "Mark as Read" else "Mark as Unread") },
                    leadingIcon = { Icon(Icons.Default.MarkEmailRead, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        Toast.makeText(context, "Marked as ${if (thread.unreadCount > 0) "Read" else "Unread"}", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Pin Chat") },
                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        Toast.makeText(context, "Chat Pinned to top", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mute Notifications") },
                    leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        Toast.makeText(context, "Notifications muted for this chat", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Archive Chat") },
                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        Toast.makeText(context, "Chat moved to Archive", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Chat", color = Color(0xFFDC2626)) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
                    onClick = {
                        showMenu = false
                        Toast.makeText(context, "Chat conversation deleted", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Block User", color = Color(0xFFDC2626)) },
                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626)) },
                    onClick = {
                        showMenu = false
                        Toast.makeText(context, "User blocked successfully", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// Composable for New Message Modal (Matches Screenshot 2)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XNewMessageSheet(
    contacts: List<ChatContact>,
    onDismiss: () -> Unit,
    onCreateGroup: () -> Unit,
    onSelectContact: (ChatContact) -> Unit
) {
    var searchContactQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(contacts, searchContactQuery) {
        contacts.filter {
            it.name.contains(searchContactQuery, ignoreCase = true) ||
                    it.handle.contains(searchContactQuery, ignoreCase = true) ||
                    it.role.contains(searchContactQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
                Text(
                    text = "New message",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchContactQuery,
                onValueChange = { searchContactQuery = it },
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF334155),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Create a group button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateGroup() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "Create Group",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Create a group",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))

            // List of Contacts
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                items(filteredContacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectContact(contact) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(contact.avatarBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (contact.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${contact.handle} • ${contact.role}",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// Composable for Direct Conversation Thread View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XConversationThreadView(
    thread: ChatThread,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val globalMessages by viewModel.chatMessages.collectAsState()
    val threadMessages = remember(globalMessages, thread.id) {
        globalMessages.filter { it.threadId == null || it.threadId == thread.id }
    }
    var inputMessage by remember { mutableStateOf("") }
    var selectedChatImageUri by remember { mutableStateOf<String?>(null) }
    var zoomedImageUrl by remember { mutableStateOf<String?>(null) }
    var isSendingImage by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedChatImageUri = uri.toString()
        }
    }

    LaunchedEffect(threadMessages.size) {
        if (threadMessages.isNotEmpty()) {
            listState.animateScrollToItem(threadMessages.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(thread.avatarBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thread.initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = thread.partnerName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                if (thread.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = CMKGoldAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (thread.isOnline) "● Active Now" else thread.handle,
                                fontSize = 10.sp,
                                color = if (thread.isOnline) Color(0xFF4ADE80) else Color.LightGray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startCall(false, thread.partnerName) }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Audio Call", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.startCall(true, thread.partnerName) }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CMKDeepBlue)
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Conversation Body
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First initial welcome bubble for this thread
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(thread.avatarBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thread.initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = thread.partnerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "${thread.handle} • CMK Direct Partner",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                    }
                }

                // Display partner initial msg
                item {
                    ChatBubble(
                        message = ChatMessage(
                            id = "partner_init",
                            senderId = "partner",
                            senderName = thread.partnerName,
                            text = thread.lastMessage,
                            timestamp = System.currentTimeMillis() - 3600000
                        ),
                        isMe = false
                    )
                }

                // Render dynamic messages sent in this thread
                items(threadMessages) { msg ->
                    ChatBubble(
                        message = msg,
                        isMe = true,
                        onImageClick = { url -> zoomedImageUrl = url }
                    )
                }
            }

            // Photo Attachment Preview Thumbnail
            if (selectedChatImageUri != null) {
                Surface(
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                            ) {
                                coil.compose.AsyncImage(
                                    model = selectedChatImageUri,
                                    contentDescription = "Attachment preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "រូបភាពភ្ជាប់រួចរាល់ (Photo attached)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                        }
                        IconButton(
                            onClick = { selectedChatImageUri = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Input Row
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = "Attach Photo", tint = CMKDeepBlue)
                    }

                    IconButton(onClick = {
                        Toast.makeText(context, "Voice memo recording ready", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Memo", tint = CMKDeepBlue)
                    }

                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Start a message...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CMKDeepBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val canSend = (inputMessage.isNotBlank() || selectedChatImageUri != null) && !isSendingImage
                    IconButton(
                        onClick = {
                            if (canSend) {
                                val textToSend = inputMessage.trim()
                                val rawImgUri = selectedChatImageUri
                                inputMessage = ""
                                selectedChatImageUri = null
                                
                                coroutineScope.launch {
                                    isSendingImage = true
                                    val processedImage = if (rawImgUri != null) {
                                        com.example.data.service.MediaUploadService.prepareImageForPublishing(context, rawImgUri)
                                    } else null
                                    viewModel.sendChatMessage(
                                        text = textToSend,
                                        imageUrl = processedImage,
                                        threadId = thread.id
                                    )
                                    isSendingImage = false
                                }
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier.background(
                            if (canSend) CMKDeepBlue else Color(0xFFCBD5E1),
                            shape = CircleShape
                        )
                    ) {
                        if (isSendingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (canSend) Color.White else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Zoom Dialog for Chat Images
    if (zoomedImageUrl != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { zoomedImageUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { zoomedImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                coil.compose.AsyncImage(
                    model = zoomedImageUrl,
                    contentDescription = "Zoomed image",
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                IconButton(
                    onClick = { zoomedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean,
    onImageClick: ((String) -> Unit)? = null
) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { formatter.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isMe) CMKDeepBlue else Color.White,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                if (!message.imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick?.invoke(message.imageUrl) }
                    ) {
                        coil.compose.AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Chat photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    if (message.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = if (isMe) Color.White else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeStr,
                    fontSize = 9.sp,
                    color = if (isMe) Color.LightGray else Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

// Composable for X Direct Message Settings Sheet with 5 Sections & 26 Items
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XChatSettingsSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Preference States for toggles
    var activeStatusOn by remember { mutableStateOf(true) }
    var soundOn by remember { mutableStateOf(true) }
    var chatHeadsOn by remember { mutableStateOf(true) }
    var autoSaveMedia by remember { mutableStateOf(true) }
    var zeroDataMode by remember { mutableStateOf(true) }
    var autoUpdateOn by remember { mutableStateOf(true) }
    var betaTesterOn by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("Light") }
    var selectedLang by remember { mutableStateOf("Khmer") }
    var fontSizeSp by remember { mutableStateOf(14f) }

    // Dialog state handlers
    var activeDialog by remember { mutableStateOf<String?>(null) }
    var bugDescription by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) },
        modifier = Modifier.fillMaxHeight(0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CMKGoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chat Settings & Preferences",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "ការកំណត់ និងមុខងារគ្រប់គ្រងសារ",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            HorizontalDivider(color = Color(0xFF334155))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==================== ផ្នែកទី ១៖ Navigation & Interaction Menu ====================
                item {
                    SettingsSectionHeader(
                        titleKh = "ផ្នែកទី ១៖ Navigation & Interaction Menu",
                        titleEn = "Navigation & Interaction",
                        icon = Icons.Default.Explore
                    )
                }

                item {
                    SettingsCard {
                        // 1. Communities
                        SettingsClickableRow(
                            title = "Communities (ក្រុម/សហគមន៍)",
                            description = "ប្រើសម្រាប់ចូលមើល ឬគ្រប់គ្រងក្រុមដែលប្រមូលផ្តុំមនុស្សដែលមានចំណូលចិត្តដូចគ្នា",
                            icon = Icons.Default.Groups,
                            onClick = { activeDialog = "Communities" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 2. Support
                        SettingsClickableRow(
                            title = "Support (ជំនួយ)",
                            description = "ប្រើសម្រាប់សួរសំណួរ ស្វែងរកការដោះស្រាយបញ្ហា ឬទាក់ទងមកកាន់ក្រុមការងារ App",
                            icon = Icons.Default.HeadsetMic,
                            onClick = { activeDialog = "Support" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 3. Message requests
                        SettingsClickableRow(
                            title = "Message requests (សំណើសារ)",
                            description = "កន្លែងផ្ទុកសារដែលផ្ញើមកពីគណនីដែលមិនទាន់បានរាប់អានជាមិត្ត (Friend/Follower)",
                            icon = Icons.Default.Inbox,
                            badgeCount = "2",
                            onClick = { activeDialog = "Requests" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 4. Archive
                        SettingsClickableRow(
                            title = "Archive (ប្រអប់ផ្ទុកសារចាស់ៗ)",
                            description = "ប្រើសម្រាប់លាក់ប្រអប់ឆាត (Chat) ពីទំព័រដើមដោយមិនបាច់លុបវាចោល",
                            icon = Icons.Default.Archive,
                            onClick = { activeDialog = "Archive" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 5. Friend requests
                        SettingsClickableRow(
                            title = "Friend requests (សំណើរសុំរាប់អានជាមិត្ត)",
                            description = "បង្ហាញបញ្ជីឈ្មោះអ្នកដែលបានផ្ញើសំណើសុំរាប់អានជាមិត្ត",
                            icon = Icons.Default.PersonAdd,
                            badgeCount = "3",
                            onClick = { activeDialog = "FriendRequests" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 6. Channel invites
                        SettingsClickableRow(
                            title = "Channel invites (ការអញ្ជើញចូល Channel)",
                            description = "ផ្ទុកការអញ្ជើញឲ្យចូលរួមក្នុង Broadcast Channel ឬព័ត៌មានផ្សេងៗ",
                            icon = Icons.Default.Campaign,
                            onClick = { activeDialog = "ChannelInvites" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 7. Moments
                        SettingsClickableRow(
                            title = "Moments (ការចែករំលែកទិដ្ឋភាព/រូបភាព)",
                            description = "ប្រើសម្រាប់បង្ហោះ ឬមើលរូបភាព/វីដេអូខ្លីៗប្រចាំថ្ងៃ",
                            icon = Icons.Default.PhotoCamera,
                            onClick = { activeDialog = "Moments" }
                        )
                    }
                }

                // ==================== ផ្នែកទី ២៖ Accounts & Profile ====================
                item {
                    SettingsSectionHeader(
                        titleKh = "ផ្នែកទី ២៖ Accounts & Profile",
                        titleEn = "Accounts & Profile",
                        icon = Icons.Default.AccountCircle
                    )
                }

                item {
                    SettingsCard {
                        // 8. Switch profile
                        SettingsClickableRow(
                            title = "Switch profile (Accounts)",
                            description = "ផ្លាស់ប្តូរគណនី - ប្រើសម្រាប់ប្តូរទៅកាន់គណនីផ្សេងទៀត (ឧទាហរណ៍៖ គណនីផ្ទាល់ខ្លួន ទៅ គណនីការងារ)",
                            icon = Icons.Default.SwitchAccount,
                            valueText = "@CHHUOYMAKARA64",
                            onClick = { activeDialog = "SwitchProfile" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 9. Dark mode
                        SettingsClickableRow(
                            title = "Dark mode (Profile)",
                            description = "របៀបពន្លឺងងឹត - សម្រាប់កំណត់ផ្ទៃ App ឲ្យទៅជាពណ៌ខ្មៅ (Light / Dark / System) ដើម្បីការពារភ្នែក និងសន្សំសំចៃថ្ម",
                            icon = Icons.Default.Brightness4,
                            valueText = selectedTheme,
                            onClick = {
                                selectedTheme = when (selectedTheme) {
                                    "Light" -> "Dark"
                                    "Dark" -> "System"
                                    else -> "Light"
                                }
                                Toast.makeText(context, "Theme set to $selectedTheme", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 10. Active status
                        SettingsToggleRow(
                            title = "Active status (ស្ថានភាពសកម្ម)",
                            description = "បង្ហាញ ឬលាក់ភ្លើងពណ៌បៃតង ដើម្បីឲ្យគេដឹងថាអ្នកកំពុង Online ឬ Offline",
                            icon = Icons.Default.FiberManualRecord,
                            iconTint = if (activeStatusOn) Color(0xFF22C55E) else Color.Gray,
                            checked = activeStatusOn,
                            onCheckedChange = {
                                activeStatusOn = it
                                Toast.makeText(context, if (it) "Active status visible" else "Active status hidden", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 11. Username
                        SettingsClickableRow(
                            title = "Username (ឈ្មោះសម្គាល់គណនី)",
                            description = "បង្ហាញឈ្មោះអត្តសញ្ញាណប្លែកពីគេ (@CHHUOYMAKARA64) សម្រាប់ឲ្យគេងាយស្រួល search រក ឬ Copy link",
                            icon = Icons.Default.AlternateEmail,
                            valueText = "Copy Link",
                            onClick = {
                                Toast.makeText(context, "Copied profile link: https://cmk.app/@CHHUOYMAKARA64", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }

                // ==================== ផ្នែកទី ៣៖ For families ====================
                item {
                    SettingsSectionHeader(
                        titleKh = "ផ្នែកទី ៣៖ For families",
                        titleEn = "For Families & Safety",
                        icon = Icons.Default.FamilyRestroom
                    )
                }

                item {
                    SettingsCard {
                        // 12. Family Center
                        SettingsClickableRow(
                            title = "Family Center (មជ្ឈមណ្ឌលគ្រួសារ)",
                            description = "ប្រើសម្រាប់ឪពុកម្តាយ/អាណាព្យាបាលភ្ជាប់គណនីជាមួយកូនៗ ដើម្បីគ្រប់គ្រងសុវត្ថិភាព ការប្រើប្រាស់ និងពេលវេលានៃការលេង App",
                            icon = Icons.Default.SupervisorAccount,
                            onClick = { activeDialog = "FamilyCenter" }
                        )
                    }
                }

                // ==================== ផ្នែកទី ៤៖ Preferences ====================
                item {
                    SettingsSectionHeader(
                        titleKh = "ផ្នែកទី ៤៖ Preferences",
                        titleEn = "App Preferences & Features",
                        icon = Icons.Default.Tune
                    )
                }

                item {
                    SettingsCard {
                        // 13. Avatar
                        SettingsClickableRow(
                            title = "Avatar (រូបតំណាង 3D)",
                            description = "សម្រាប់បង្កើត ឬកែសម្រួលរូបតុក្កតា 3D តំណាងឲ្យខ្លួនឯង",
                            icon = Icons.Default.Face,
                            onClick = { activeDialog = "Avatar" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 14. App icon
                        SettingsClickableRow(
                            title = "App icon (រូបតំណាងកម្មវិធី)",
                            description = "អនុញ្ញាតឲ្យអ្នកប្រើប្រាស់ផ្លាស់ប្តូរ Logo/Icon របស់ App នៅលើអេក្រង់ទូរស័ព្ទ",
                            icon = Icons.Default.Apps,
                            onClick = { activeDialog = "AppIcon" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 15. Notifications & sounds
                        SettingsToggleRow(
                            title = "Notifications & sounds (ការជូនដំណឹង និងសំឡេង)",
                            description = "កំណត់ការបិទ/បើកសំឡេងសារ រក្សាសំឡេងរោទ៍ ឬការលោត Notification",
                            icon = Icons.Default.NotificationsActive,
                            checked = soundOn,
                            onCheckedChange = {
                                soundOn = it
                                Toast.makeText(context, if (it) "Chat sounds enabled" else "Muted chat sounds", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 16. Chat heads
                        SettingsToggleRow(
                            title = "Chat heads (ក្បាលឆាតលោតលើអេក្រង់)",
                            description = "បើក/បិទ រូបរង្វង់ឆាតតូចៗដែលលោតនៅលើអេក្រង់ទូរស័ព្ទ (សម្រាប់ Android)",
                            icon = Icons.Default.BubbleChart,
                            checked = chatHeadsOn,
                            onCheckedChange = {
                                chatHeadsOn = it
                                Toast.makeText(context, if (it) "Floating chat heads enabled" else "Chat heads disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 17. Accessibility
                        SettingsClickableRow(
                            title = "Accessibility (ភាពងាយស្រួលក្នុងការប្រើប្រាស់)",
                            description = "ការកំណត់ទំហំអក្សរ ពណ៌ ឬមុខងារជំនួយសម្រាប់អ្នកដែលមានបញ្ហាភ្នែក/ត្រចៀក",
                            icon = Icons.Default.AccessibilityNew,
                            valueText = "${fontSizeSp.toInt()} sp",
                            onClick = {
                                fontSizeSp = if (fontSizeSp >= 18f) 12f else fontSizeSp + 2f
                                Toast.makeText(context, "Font size updated to ${fontSizeSp.toInt()} sp", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 18. Privacy & safety
                        SettingsClickableRow(
                            title = "Privacy & safety (ឯកជនភាព និងសុវត្ថិភាព)",
                            description = "គ្រប់គ្រងការ Block គណនី, ការកំណត់ថាអ្នកណាខ្លះអាចឃើញព័ត៌មានយើង ឬផ្ញើសារមកយើងបាន",
                            icon = Icons.Default.Security,
                            onClick = {
                                Toast.makeText(context, "Privacy & Safety settings active. Blocked users: 0", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 19. Language identification
                        SettingsClickableRow(
                            title = "Language identification (ការកំណត់/ស្គាល់ភាសា)",
                            description = "អនុញ្ញាតឲ្យ App ស្គាល់ភាសាស្វ័យប្រវត្តិ ឬជ្រើសរើសភាសាផ្លូវការក្នុង App",
                            icon = Icons.Default.Language,
                            valueText = selectedLang,
                            onClick = {
                                selectedLang = if (selectedLang == "Khmer") "English" else "Khmer"
                                Toast.makeText(context, "Language set to $selectedLang", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 20. Photos & media
                        SettingsToggleRow(
                            title = "Photos & media (រូបភាព និងមេឌៀ)",
                            description = "កំណត់ការ Save រូបភាព/វីដេអូចូលទូរស័ព្ទស្វ័យប្រវត្តិ ឬកម្រិតគុណភាពនៃការផ្ញើរូប (HD)",
                            icon = Icons.Default.PermMedia,
                            checked = autoSaveMedia,
                            onCheckedChange = {
                                autoSaveMedia = it
                                Toast.makeText(context, if (it) "Auto-save media enabled" else "Auto-save disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 21. Metfone program / Network program
                        SettingsToggleRow(
                            title = "Metfone / Network Zero Data Program",
                            description = "កម្មវិធីដៃគូបណ្តាញសេវា - មុខងារពិសេសសម្រាប់អតិថិជនប្រើប្រាស់ស៊ីមដៃគូ (លេង App ដោយមិនអស់សេវា)",
                            icon = Icons.Default.SignalCellular4Bar,
                            iconTint = CMKGoldAccent,
                            checked = zeroDataMode,
                            onCheckedChange = {
                                zeroDataMode = it
                                Toast.makeText(context, if (it) "Zero Data Mode active (Metfone Free Access)" else "Standard data active", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 22. App updates
                        SettingsToggleRow(
                            title = "App updates (ការធ្វើបច្ចុប្បន្នភាព App)",
                            description = "ពិនិត្យមើល ឬកំណត់ឲ្យ App Update ទៅកាន់ Version ថ្មីដោយស្វ័យប្រវត្តិ",
                            icon = Icons.Default.SystemUpdate,
                            checked = autoUpdateOn,
                            onCheckedChange = {
                                autoUpdateOn = it
                                Toast.makeText(context, if (it) "Auto-updates enabled" else "Auto-updates disabled", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 23. Early access to features
                        SettingsToggleRow(
                            title = "Early access to features (សាកល្បងមុខងារថ្មីៗ)",
                            description = "អនុញ្ញាតឲ្យអ្នកប្រើប្រាស់ចុះឈ្មោះលេងមុខងារ Beta/មុខងារថ្មីៗដែល App ទើបតែបង្កើត",
                            icon = Icons.Default.Science,
                            iconTint = Color(0xFFA855F7),
                            checked = betaTesterOn,
                            onCheckedChange = {
                                betaTesterOn = it
                                Toast.makeText(context, if (it) "Joined Beta Tester Program!" else "Opted out of Beta", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // ==================== ផ្នែកទី ៥៖ Safety & Help ====================
                item {
                    SettingsSectionHeader(
                        titleKh = "ផ្នែកទី ៥៖ Safety & Help",
                        titleEn = "Safety, Help & Legal",
                        icon = Icons.Default.HelpOutline
                    )
                }

                item {
                    SettingsCard {
                        // 24. Report technical problem
                        SettingsClickableRow(
                            title = "Report technical problem (រាយការណ៍បញ្ហាបច្ចេកទេស)",
                            description = "សម្រាប់ឲ្យ User ផ្ញើសាររាយការណ៍ពេល App មាន Error, Bug ឬគាំង",
                            icon = Icons.Default.BugReport,
                            onClick = { activeDialog = "BugReport" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 25. Help
                        SettingsClickableRow(
                            title = "Help (ជំនួយ/ការណែនាំ)",
                            description = "កន្លែងអានឯកសារណែនាំពីរបៀបប្រើប្រាស់ App (FAQ/User Guide)",
                            icon = Icons.Default.Help,
                            onClick = { activeDialog = "Help" }
                        )
                        HorizontalDivider(color = Color(0xFF334155))

                        // 26. Legal & policies
                        SettingsClickableRow(
                            title = "Legal & policies (លក្ខខណ្ឌច្បាប់ និងគោលការណ៍)",
                            description = "បង្ហាញពីលក្ខខណ្ឌនៃការប្រើប្រាស់ (Terms of Service) និងគោលការណ៍រក្សាការសម្ងាត់ (Privacy Policy)",
                            icon = Icons.Default.Gavel,
                            onClick = { activeDialog = "Legal" }
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CMK Construction Social Network v3.2.0",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Protected by 256-bit Encryption & Data Privacy",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        // --- SUB-DIALOGS FOR SETTINGS ITEMS ---
        if (activeDialog != null) {
            when (activeDialog) {
                "Communities" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Communities (ក្រុម/សហគមន៍)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("ក្រុមសហគមន៍សំណង់ និងសម្ភារៈសាងសង់សកម្ម៖", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(10.dp))
                                CommunityItem("🏗️ CMK Concrete Builders Club", "1,240 Members")
                                CommunityItem("🔩 Cambodia Steel Dealers Association", "850 Members")
                                CommunityItem("🚜 Phnom Penh Heavy Machinery Operators", "620 Members")
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Done", color = Color.White)
                            }
                        }
                    )
                }
                "Support" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Support (ជំនួយ)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("ទាក់ទងមកកាន់ក្រុមការងារគាំទ្រ CMK 24/7:", fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("📞 Hotline: 023 888 999 / 012 345 678", fontWeight = FontWeight.Bold, color = CMKGoldAccent)
                                Text("📧 Email: support@cmk-construction.com", fontSize = 12.sp, color = Color.Gray)
                                Text("💬 Live Support: Available in Direct Chat", fontSize = 12.sp, color = Color.Gray)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Close", color = Color.White)
                            }
                        }
                    )
                }
                "Requests" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Message requests (សំណើសារ)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("សំណើសារពីគណនីដែលមិនទាន់បានរាប់អានជាមិត្ត៖", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Eric Rodriguez (@eric_contractor)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Hi, do you deliver rebar to Kampot?", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Button(onClick = {
                                        Toast.makeText(context, "Accepted message request from Eric", Toast.LENGTH_SHORT).show()
                                        activeDialog = null
                                    }, colors = ButtonDefaults.buttonColors(containerColor = CMKGoldAccent)) {
                                        Text("Accept", fontSize = 11.sp, color = Color.Black)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { activeDialog = null }) { Text("Close", color = Color.Gray) }
                        }
                    )
                }
                "Archive" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Archive (ប្រអប់ផ្ទុកសារចាស់ៗ)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Text("គ្មានសារចាស់ៗដែលបានលាក់ទុកក្នុង Archive ទេ (0 Archived Chats).", fontSize = 13.sp, color = Color.Gray)
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("OK", color = Color.White)
                            }
                        }
                    )
                }
                "FriendRequests" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Friend Requests (សំណើរសុំរាប់អាន)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("សំណើរសុំរាប់អានជាមិត្តថ្មី (3):", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("1. Voleak Steel Mart (@voleak_steel)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("2. Somnang Construction (@somnang_build)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("3. Kirirom Hardware Depot (@kirirom_depot)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                Toast.makeText(context, "Accepted all friend requests", Toast.LENGTH_SHORT).show()
                                activeDialog = null
                            }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Accept All", color = Color.White)
                            }
                        }
                    )
                }
                "ChannelInvites" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Channel Invites", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Text("ការអញ្ជើញចូលរួមក្នុង Broadcast Channel៖\n📢 CMK Steel Daily Price Official Channel", fontSize = 13.sp)
                        },
                        confirmButton = {
                            Button(onClick = {
                                Toast.makeText(context, "Joined CMK Daily Channel!", Toast.LENGTH_SHORT).show()
                                activeDialog = null
                            }, colors = ButtonDefaults.buttonColors(containerColor = CMKGoldAccent)) {
                                Text("Join Channel", color = Color.Black)
                            }
                        }
                    )
                }
                "Moments" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Moments (ទិដ្ឋភាពប្រចាំថ្ងៃ)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Text("📸 បង្ហោះរូបភាព ឬមើលវីដេអូខ្លីៗពីការដ្ឋានសំណង់ប្រចាំថ្ងៃ។ មុខងាររៀបចំរួចរាល់ 100%!", fontSize = 13.sp)
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("View Moments", color = Color.White)
                            }
                        }
                    )
                }
                "SwitchProfile" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Switch Profile / Accounts", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("ជ្រើសរើសគណនីប្រើប្រាស់៖", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(10.dp))
                                ProfileOption("Mr. MAKAR CH (Personal)", "@CHHUOYMAKARA64", true)
                                Spacer(modifier = Modifier.height(6.dp))
                                ProfileOption("CMK Business Account", "@cmk_procurement_vip", false)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Done", color = Color.White)
                            }
                        }
                    )
                }
                "FamilyCenter" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Family Center (មជ្ឈមណ្ឌលគ្រួសារ)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Text("👨‍👩‍👧‍👦 ភ្ជាប់គណនីអាណាព្យាបាលដើម្បីគ្រប់គ្រងសុវត្ថិភាព និងកំណត់ពេលវេលាប្រើប្រាស់ App របស់កូនៗ។", fontSize = 13.sp)
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Set Up Family Center", color = Color.White)
                            }
                        }
                    )
                }
                "Avatar" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("3D Avatar Editor", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("🎨 រូបតំណាង 3D តំណាងឱ្យខ្លួនឯងក្នុងសហគមន៍សំណង់ CMK", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(CMKGoldAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Engineering, contentDescription = null, tint = Color.Black, modifier = Modifier.size(40.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Safety Helmet + Construction Vest Avatar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                Toast.makeText(context, "3D Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                                activeDialog = null
                            }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Save Avatar", color = Color.White)
                            }
                        }
                    )
                }
                "AppIcon" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("App Icon Theme Selector", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("ជ្រើសរើស Logo App នៅលើអេក្រង់ទូរស័ព្ទ៖", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(10.dp))
                                IconOption("Classic CMK Blue Icon", "Default Official", true)
                                IconOption("Gold VIP Shield Icon", "Pro Member", false)
                                IconOption("Dark Stealth Icon", "Night Theme", false)
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                Toast.makeText(context, "App icon updated!", Toast.LENGTH_SHORT).show()
                                activeDialog = null
                            }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Apply Icon", color = Color.White)
                            }
                        }
                    )
                }
                "BugReport" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Report Technical Problem", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("រាយការណ៍បញ្ហាបច្ចេកទេស ឬ Bug មកកាន់ក្រុមការងារ IT CMK:", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = bugDescription,
                                    onValueChange = { bugDescription = it },
                                    placeholder = { Text("Describe the technical issue here...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (bugDescription.isNotBlank()) {
                                    Toast.makeText(context, "Thank you! Bug report sent to CMK Tech Team.", Toast.LENGTH_LONG).show()
                                    bugDescription = ""
                                    activeDialog = null
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Send Report", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { activeDialog = null }) { Text("Cancel", color = Color.Gray) }
                        }
                    )
                }
                "Help" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Help & User Guide (ជំនួយ)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("📘 របៀបប្រើប្រាស់កម្មវិធី និងសំណួរញឹកញាប់ (FAQ):", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("• Q: របៀបផ្ញើសារសួើតម្លៃសម្ភារៈសំណង់?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("  A: ចុចប៊ូតុង + នៅជ្រុងខាងក្រោមដើម្បីបង្កើតឆាតថ្មី។", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Q: តើអាចបង្កើតក្រុមការដ្ឋានបានទេ?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("  A: បាន! ចុច New Message រួចជ្រើសរើស Create a group។", fontSize = 11.sp, color = Color.Gray)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Got It", color = Color.White)
                            }
                        }
                    )
                }
                "Legal" -> {
                    AlertDialog(
                        onDismissRequest = { activeDialog = null },
                        title = { Text("Legal & Policies", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                        text = {
                            Column {
                                Text("📜 លក្ខខណ្ឌច្បាប់ និងគោលការណ៍រក្សាការសម្ងាត់:", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("1. Terms of Service (លក្ខខណ្ឌប្រើប្រាស់)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("2. Privacy Policy & Data Protection (គោលការណ៍ឯកជនភាព)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("3. ISO 27001 Security Standard Compliant", fontSize = 11.sp, color = CMKGoldAccent)
                            }
                        },
                        confirmButton = {
                            Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)) {
                                Text("Close", color = Color.White)
                            }
                        }
                    )
                }
            }
        }
    }
}

// Helper composables for Settings Layout
@Composable
fun SettingsSectionHeader(titleKh: String, titleEn: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = titleKh, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = titleEn, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), content = content)
    }
}

@Composable
fun SettingsClickableRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeCount: String? = null,
    valueText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp)
        }
        if (valueText != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(color = Color(0xFF334155), shape = RoundedCornerShape(8.dp)) {
                Text(text = valueText, color = CMKGoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        if (badgeCount != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CMKGoldAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(text = badgeCount, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = Color.LightGray,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, color = Color.Gray, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = CMKGoldAccent,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}

@Composable
fun CommunityItem(name: String, count: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = count, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileOption(name: String, handle: String, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFF1E293B) else Color.Transparent, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = if (isSelected) CMKGoldAccent else Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color.White else Color.Black)
            Text(text = handle, fontSize = 11.sp, color = Color.Gray)
        }
        if (isSelected) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CMKGoldAccent)
        }
    }
}

@Composable
fun IconOption(name: String, tag: String, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Surface(color = if (isSelected) CMKGoldAccent else Color.LightGray, shape = RoundedCornerShape(4.dp)) {
            Text(text = tag, fontSize = 10.sp, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun ActiveNowOnlineBar(
    onlineUsers: List<OnlineUser>,
    onUserClick: (OnlineUser) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "កំពុងប្រើប្រាស់ (Active Now)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Text(
                text = "${onlineUsers.size - 1} នាក់ Online",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(onlineUsers, key = { it.id }) { u ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(62.dp)
                        .clickable { onUserClick(u) }
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            shape = CircleShape,
                            color = u.avatarBgColor,
                            modifier = Modifier
                                .size(54.dp)
                                .border(
                                    width = if (u.isSelf) 2.dp else 1.5.dp,
                                    color = if (u.isSelf) CMKGoldAccent else Color(0xFFE2E8F0),
                                    shape = CircleShape
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = u.initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }

                        if (u.isSelf) {
                            Surface(
                                shape = CircleShape,
                                color = CMKGoldAccent,
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(1.5.dp, Color.White, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Your status",
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        } else {
                            // Active green dot
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = u.shortName,
                        fontSize = 11.sp,
                        fontWeight = if (u.isSelf) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
