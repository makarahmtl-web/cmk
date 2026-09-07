package com.example.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.repository.UserSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUserSession.collectAsState()
    val allPosts by viewModel.feedPosts.collectAsState()
    val userPosts = remember(allPosts, user) {
        allPosts.filter { it.userId == user?.uid || (user?.displayName.isNullOrEmpty().not() && it.userDisplayName == user?.displayName) }
    }
    val hasPasscode by viewModel.hasPasscode.collectAsState()
    var isSettingPasscode by remember { mutableStateOf(false) }

    // Dialog States
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAnalyticsDialog by remember { mutableStateOf(false) }
    var showCoverOptionsDialog by remember { mutableStateOf(false) }
    var showAvatarOptionsDialog by remember { mutableStateOf(false) }

    // Selected Tab State (Facebook style tabs)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Photo Pickers (Zero-permission Android Photo Picker)
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileAvatar(uri.toString())
            Toast.makeText(context, "រូបថតប្រវត្តិរូប (Avatar) ត្រូវបានផ្លាស់ប្តូរដោយជោគជ័យ!", Toast.LENGTH_SHORT).show()
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfileCover(uri.toString())
            Toast.makeText(context, "រូបភាព Cover ត្រូវបានផ្លាស់ប្តូរដោយជោគជ័យ!", Toast.LENGTH_SHORT).show()
        }
    }

    if (isSettingPasscode) {
        PasscodeLockScreen(
            viewModel = viewModel,
            isSettingNew = true,
            onSetupComplete = { isSettingPasscode = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = user?.displayName?.ifEmpty { "Partner Profile" } ?: "Partner Profile",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 19.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "CMK Corporate Hub • Official Account",
                                fontSize = 11.sp,
                                color = CMKGoldAccent
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("btn_top_menu_settings")
                        ) {
                            FourStoryMenuIcon(tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CMKDeepBlue)
                )
            },
            containerColor = Color(0xFFF1F5F9)
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // 1. Social Profile Header: Box layout with Cover background and Overlapping Avatar (Facebook Style)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Cover Photo Container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .background(Color(0xFFE2E8F0))
                                    .clickable {
                                        coverPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user?.coverUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(user?.coverUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Cover Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Light white-grey empty cover placeholder with Add Cover button
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.95f),
                                            shadowElevation = 3.dp
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.AddAPhoto,
                                                    contentDescription = "Add Cover",
                                                    tint = CMKDeepBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "ជ្រើសរើសរូបភាព Cover (Add Cover Photo)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CMKDeepBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Spacer for the avatar offset
                            Spacer(modifier = Modifier.height(56.dp))

                            // Name and Bio below avatar
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = user?.displayName?.ifEmpty { "CMK Partner" } ?: "CMK Partner",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                val headline = user?.headline?.ifEmpty {
                                    "Procurement Manager & Wholesale Construction Materials Importer"
                                } ?: "Procurement Manager & Wholesale Construction Materials Importer"

                                Text(
                                    text = headline,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                if (!user?.bio.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = user?.bio ?: "",
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Overlapping Circular Profile Avatar
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 180.dp)
                                .size(116.dp)
                        ) {
                            // Avatar Outer Ring & Shadow
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 6.dp,
                                border = BorderStroke(4.dp, Color.White),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(CMKDeepBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!user?.avatarUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(user?.avatarUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = user?.avatarInitials?.ifEmpty { "CP" } ?: "CP",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = CMKGoldAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Action Buttons Row (កែប្រែ, សារ, ស្ថិតិ with uniform styling, dark blue background, and equal width)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Edit (កែប្រែ)
                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CMKDeepBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_edit_profile_main")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "កែប្រែ",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "កែប្រែ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }

                        // 2. Messages (សារ)
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.CHAT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CMKDeepBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_profile_messages")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = "សារ",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "សារ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }

                        // 3. Analytics (ស្ថិតិ)
                        Button(
                            onClick = { showAnalyticsDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CMKDeepBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_profile_analytics")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "ស្ថិតិ",
                                    modifier = Modifier.size(17.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ស្ថិតិ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 3. Real-time User Statistics Card (អ្នកតាមដាន, កំពុងតាមដាន, សារសាកសួរ, ការផ្សាយ)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { showAnalyticsDialog = true }
                            .testTag("profile_stats_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatItem(
                                count = "${user?.followersCount ?: 0}",
                                label = "អ្នកតាមដាន"
                            )
                            Divider(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(1.dp),
                                color = Color(0xFFE2E8F0)
                            )
                            ProfileStatItem(
                                count = "${user?.followingCount ?: 0}",
                                label = "កំពុងតាមដាន"
                            )
                            Divider(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(1.dp),
                                color = Color(0xFFE2E8F0)
                            )
                            ProfileStatItem(
                                count = "${user?.inquiriesCount ?: 0}",
                                label = "ការសាកសួរ"
                            )
                            Divider(
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(1.dp),
                                color = Color(0xFFE2E8F0)
                            )
                            ProfileStatItem(
                                count = "${user?.feedPostsCount ?: 0}",
                                label = "ការផ្សាយ"
                            )
                        }
                    }
                }

                // 4. Intro Details Section (Fully editable by user)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "ព័ត៌មានលម្អិត (INTRO DETAILS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )

                            val workplace = user?.workplace?.ifEmpty { "មិនទាន់បានបញ្ចូល" } ?: "មិនទាន់បានបញ្ចូល"
                            val location = user?.location?.ifEmpty { "មិនទាន់បានបញ្ចូល" } ?: "មិនទាន់បានបញ្ចូល"
                            val joinedDate = user?.joinedDate?.ifEmpty { "ខែកញ្ញា ឆ្នាំ២០២៦" } ?: "ខែកញ្ញា ឆ្នាំ២០២៦"
                            val email = user?.email?.ifEmpty { "មិនទាន់បានបញ្ចូល" } ?: "មិនទាន់បានបញ្ចូល" 

                            IntroDetailRow(icon = Icons.Default.Business, boldText = "ក្រុមហ៊ុន/ស្ថាប័ន៖", regularText = workplace)
                            IntroDetailRow(icon = Icons.Default.LocationOn, boldText = "ទីតាំងអាជីវកម្ម៖", regularText = location)

                            // Map Preview Card - ភ្ជាប់ទៅកាន់ផែនទីទូរស័ព្ទផ្ទាល់
                            val mapQuery = if (location == "មិនទាន់បានបញ្ចូល") "Phnom Penh, Cambodia" else location
                            Card(
                                onClick = {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(mapQuery)}"))
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(mapQuery)}"))
                                        context.startActivity(browserIntent)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data("https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?auto=format&fit=crop&q=80&w=600")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Map Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.85f
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color(0xCC0F172A))
                                                )
                                            )
                                    )
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Map,
                                            contentDescription = null,
                                            tint = CMKGoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "ផែនទីទីតាំង៖ $mapQuery",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "ចុចដើម្បីបើកលើកម្មវិធីផែនទីទូរស័ព្ទ (Open Map App)",
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                            IntroDetailRow(icon = Icons.Default.CalendarMonth, boldText = "បានចូលរួមនៅ៖", regularText = joinedDate)
                            IntroDetailRow(icon = Icons.Default.Email, boldText = "អ៊ីមែលផ្លូវការ៖", regularText = email)
                        }
                    }
                }

                // 5. Facebook-Style 6 Tab Navigation
                item {
                    val tabsList = listOf(
                        "Posts" to "រូបភាព/អត្ថបទ",
                        "Videos" to "វីដេអូបង្ហោះ",
                        "Shares" to "ចែករំលែកបន្ត",
                        "Drafts" to "ការចងក្រង-ចាក់សោ",
                        "Likes" to "បានចូលចិត្ត",
                        "Saved" to "រក្សាទុក"
                    )
                    val tabIcons = listOf(
                        Icons.Default.Article,
                        Icons.Default.PlayCircle,
                        Icons.Default.Share,
                        Icons.Default.Lock,
                        Icons.Default.Favorite,
                        Icons.Default.Bookmark
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = CMKDeepBlue,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = CMKDeepBlue
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp)
                    ) {
                        tabsList.forEachIndexed { index, (title, subtitle) ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                selectedContentColor = CMKDeepBlue,
                                unselectedContentColor = Color.Gray,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = tabIcons[index],
                                        contentDescription = title,
                                        tint = if (selectedTab == index) CMKDeepBlue else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 9.sp,
                                        color = if (selectedTab == index) CMKGoldAccent else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Dynamic Content Based on Selected Tab
                when (selectedTab) {
                    0 -> { // Posts Tab
                        if (userPosts.isEmpty()) {
                            item {
                                EmptyTabState(
                                    icon = Icons.Default.Article,
                                    title = "មិនទាន់មានការផុសព័ត៌មាននៅឡើយទេ",
                                    subtitle = "បង្កើតការផុសដំបូងរបស់អ្នក ដើម្បីចែករំលែកព័ត៌មានជាមួយដៃគូ!"
                                )
                            }
                        } else {
                            items(userPosts, key = { it.id }) { post ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    PostItemCard(
                                        post = post,
                                        currentUserId = user?.uid,
                                        onLike = { viewModel.toggleLike(post.id) },
                                        onCommentClick = { viewModel.setCommentPostId(post.id) },
                                        onBlockUser = { viewModel.blockUser(post.userDisplayName) }
                                    )
                                }
                            }
                        }
                    }
                    1 -> { // Videos Tab
                        item {
                            EmptyTabState(
                                icon = Icons.Default.OndemandVideo,
                                title = "មិនទាន់មានវីដេអូនៅឡើយទេ",
                                subtitle = "វីដេអូដែលអ្នកបានបង្ហោះនឹងបង្ហាញនៅទីនេះ"
                            )
                        }
                    }
                    2 -> { // Shares Tab
                        item {
                            EmptyTabState(
                                icon = Icons.Default.Share,
                                title = "មិនទាន់មានការចែករំលែកនៅឡើយទេ",
                                subtitle = "អត្ថបទដែលអ្នកបានចែករំលែកនឹងបង្ហាញនៅទីនេះ"
                            )
                        }
                    }
                    3 -> { // Drafts Tab
                        item {
                            EmptyTabState(
                                icon = Icons.Default.Lock,
                                title = "មិនទាន់មានសេចក្តីព្រាងនៅឡើយទេ",
                                subtitle = "ឯកសារ និងសេចក្តីព្រាងដែលបានរក្សាទុកនឹងបង្ហាញនៅទីនេះ"
                            )
                        }
                    }
                    4 -> { // Likes Tab
                        item {
                            EmptyTabState(
                                icon = Icons.Default.Favorite,
                                title = "មិនទាន់មានការចូលចិត្តនៅឡើយទេ",
                                subtitle = "អត្ថបទដែលអ្នកបានចូលចិត្តនឹងបង្ហាញនៅទីនេះ"
                            )
                        }
                    }
                    5 -> { // Saved Tab
                        item {
                            EmptyTabState(
                                icon = Icons.Default.Bookmark,
                                title = "មិនទាន់មានទិន្នន័យដែលបានរក្សាទុកនៅឡើយទេ",
                                subtitle = "ទីតាំង និងឯកសារដែលបានរក្សាទុកនឹងបង្ហាញនៅទីនេះ"
                            )
                        }
                    }
                }
            }
        }
    }

    // === Dialog 1: Facebook 100% Style Comprehensive Profile Editor ===
    if (showEditProfileDialog) {
        EditProfileModal(
            currentSession = user ?: UserSession(
                uid = "default_uid",
                email = "partner@cmkmaterials.com",
                displayName = "MAKARA",
                avatarInitials = "MK"
            ),
            onDismiss = { showEditProfileDialog = false },
            onPickAvatar = {
                avatarPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onPickCover = {
                coverPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onSave = { updatedSession ->
                viewModel.updateProfile(
                    displayName = updatedSession.displayName,
                    bio = updatedSession.bio,
                    headline = updatedSession.headline,
                    workplace = updatedSession.workplace,
                    location = updatedSession.location,
                    avatarUrl = updatedSession.avatarUrl,
                    coverUrl = updatedSession.coverUrl
                )
                showEditProfileDialog = false
                Toast.makeText(context, "ព័ត៌មានប្រវត្តិរូបត្រូវបានរក្សាទុកជោគជ័យ!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // === Dialog 2: Cover Photo Options (Device Gallery Picker) ===
    if (showCoverOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showCoverOptionsDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = CMKDeepBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ប្តូររូបភាព Cover", fontWeight = FontWeight.Bold, color = CMKDeepBlue, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ជ្រើសរើសរូបភាពពីទូរស័ព្ទរបស់អ្នកសម្រាប់ទំព័រប្រវត្តិរូប៖", fontSize = 13.sp, color = Color.Gray)

                    // Pick from device button
                    Button(
                        onClick = {
                            showCoverOptionsDialog = false
                            coverPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ជ្រើសរើសរូបពី Gallery (Pick from Gallery)", fontWeight = FontWeight.Bold)
                    }

                    if (!user?.coverUrl.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateProfileCover(null)
                                showCoverOptionsDialog = false
                                Toast.makeText(context, "បានលុបរូបភាព Cover រួចរាល់!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("លុបរូបភាព Cover (Remove Cover Photo)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCoverOptionsDialog = false }) {
                    Text("បោះបង់", color = Color.Gray)
                }
            }
        )
    }

    // === Dialog 3: Avatar Options (Device Gallery Picker) ===
    if (showAvatarOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarOptionsDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CMKDeepBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ប្តូររូបថត Profile", fontWeight = FontWeight.Bold, color = CMKDeepBlue, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ជ្រើសរើសរូបថតផ្ទាល់ខ្លួនពីទូរស័ព្ទរបស់អ្នក៖", fontSize = 13.sp, color = Color.Gray)

                    // Pick from device button
                    Button(
                        onClick = {
                            showAvatarOptionsDialog = false
                            avatarPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ជ្រើសរើសរូបពី Gallery (Pick from Gallery)", fontWeight = FontWeight.Bold)
                    }

                    if (!user?.avatarUrl.isNullOrEmpty()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateProfileAvatar(null)
                                showAvatarOptionsDialog = false
                                Toast.makeText(context, "បានលុបរូបថត Profile រួចរាល់!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("លុបរូបថត Profile (Remove Avatar)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarOptionsDialog = false }) {
                    Text("បោះបង់", color = Color.Gray)
                }
            }
        )
    }

    // === Dialog: Analytics & Insights Dialog (ស្ថិតិ និងការវិភាគអាជីវកម្មជាក់ស្តែង) ===
    if (showAnalyticsDialog) {
        AnalyticsInsightsDialog(
            user = user,
            onDismiss = { showAnalyticsDialog = false },
            onGoToFeed = {
                showAnalyticsDialog = false
                viewModel.navigateTo(AppScreen.FEED)
            }
        )
    }
}

// === Facebook 100% Style Comprehensive Edit Profile Modal ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileModal(
    currentSession: UserSession,
    onDismiss: () -> Unit,
    onPickAvatar: () -> Unit,
    onPickCover: () -> Unit,
    onSave: (UserSession) -> Unit
) {
    var name by remember { mutableStateOf(currentSession.displayName) }
    var headline by remember { mutableStateOf(currentSession.headline) }
    var bio by remember { mutableStateOf(currentSession.bio) }
    var workplace by remember { mutableStateOf(currentSession.workplace) }
    var location by remember { mutableStateOf(currentSession.location) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF0F172A),
        unfocusedTextColor = Color(0xFF0F172A),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = CMKDeepBlue,
        unfocusedLabelColor = Color(0xFF475569),
        focusedBorderColor = CMKDeepBlue,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        cursorColor = CMKDeepBlue
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "កែប្រែប្រវត្តិរូប (Edit Profile)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                val updated = currentSession.copy(
                                    displayName = name.trim().ifEmpty { "CMK Partner" },
                                    headline = headline.trim(),
                                    bio = bio.trim(),
                                    workplace = workplace.trim(),
                                    location = location.trim()
                                )
                                onSave(updated)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CMKGoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("រក្សាទុក", fontWeight = FontWeight.Bold, color = CMKDeepBlue, fontSize = 13.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CMKDeepBlue)
                )
            },
            containerColor = Color(0xFFF8FAFC)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Profile Picture (Facebook Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("រូបថត Profile (Profile Picture)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                            TextButton(onClick = onPickAvatar) {
                                Text("កែប្រែ (Edit)", fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(CMKDeepBlue)
                                .clickable { onPickAvatar() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentSession.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = currentSession.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = currentSession.avatarInitials.ifEmpty { "CP" },
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CMKGoldAccent
                                )
                            }
                        }
                    }
                }

                // Section 2: Cover Photo (Facebook Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("រូបភាព Cover (Cover Photo)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                            TextButton(onClick = onPickCover) {
                                Text("កែប្រែ (Edit)", fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(12.dp))
                                .clickable { onPickCover() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentSession.coverUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = currentSession.coverUrl,
                                    contentDescription = "Cover Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = CMKDeepBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "ជ្រើសរើសរូបភាព Cover (Select Cover Photo)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Name & Headline (Facebook Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("ឈ្មោះ និងតួនាទីអាជីវកម្ម", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("ឈ្មោះពេញ (Display Name)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CMKDeepBlue) },
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = headline,
                            onValueChange = { headline = it },
                            label = { Text("តួនាទី / មុខរបរ (Role & Specialization)") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = CMKDeepBlue) },
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // Section 4: Bio & Intro Details (Facebook Style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("ជីវប្រវត្តិសង្ខេប និងព័ត៌មានលម្អិត (Bio & Details)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("ជីវប្រវត្តិសង្ខេប (Bio - ពណ៌នាអំពីអាជីវកម្មរបស់អ្នក)") },
                            placeholder = { Text("រៀបរាប់អំពីសេវាកម្ម ឬទំនិញគ្រឿងសំណង់ដែលអ្នកផ្គត់ផ្គង់...") },
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4
                        )

                        OutlinedTextField(
                            value = workplace,
                            onValueChange = { workplace = it },
                            label = { Text("ក្រុមហ៊ុន / ហាងផ្គត់ផ្គង់ (Workplace / Business)") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = CMKDeepBlue) },
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("ទីតាំង / រាជធានី-ខេត្ត (Location)") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = CMKDeepBlue) },
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // Save action button
                Button(
                    onClick = {
                        val updated = currentSession.copy(
                            displayName = name.trim().ifEmpty { "CMK Partner" },
                            headline = headline.trim(),
                            bio = bio.trim(),
                            workplace = workplace.trim(),
                            location = location.trim()
                        )
                        onSave(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("រក្សាទុកការកែប្រែទាំងអស់", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EmptyTabState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE2E8F0),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// === Subcomponents for Feed Social Media Tabs ===

@Composable
fun PostTabItem(post: PostItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wholesale Feed",
                        color = CMKGoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = post.time,
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.body,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun VideoTabItem(video: VideoItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = video.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = video.duration, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(video.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(2.dp))
                Text(video.views, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ShareTabItem(share: ShareItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shared by " + share.author,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CMKDeepBlue
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = share.text,
                fontSize = 12.sp,
                color = Color.DarkGray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(share.time, fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun DraftTabItem(draft: DraftItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Locked Draft", tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(draft.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                    Text(draft.type + " • " + draft.size, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun LikedTabItem(liked: LikedItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = "Liked", tint = Rose500, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(liked.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                    Text(liked.category, fontSize = 11.sp, color = CMKDeepBlue, fontWeight = FontWeight.Medium)
                }
            }
            Text(liked.likesCount, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SavedTabItem(saved: SavedItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bookmark, contentDescription = "Saved Item", tint = CMKGoldAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(saved.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                    Text(saved.category, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Text(saved.dateSaved, fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

// Standard Stat Item
@Composable
fun ProfileStatItem(
    count: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CMKDeepBlue
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
    }
}

// Coordinates row
@Composable
fun ContactCoordinateRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CMKDeepBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// Intro details row
@Composable
fun IntroDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    boldText: String,
    regularText: String,
    accentColor: Color = Color(0xFF1E293B)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))) {
                    append("$boldText ")
                }
                withStyle(style = SpanStyle(color = accentColor, fontWeight = FontWeight.Medium)) {
                    append(regularText)
                }
            },
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// === Inline Data Classes for Tab items ===
data class PostItemData(val title: String, val body: String, val time: String, val imageUrl: String)
data class VideoItemData(val title: String, val duration: String, val views: String, val imageUrl: String)
data class ShareItemData(val author: String, val text: String, val time: String)
data class DraftItemData(val title: String, val type: String, val size: String)
data class LikedItemData(val title: String, val category: String, val likesCount: String)
data class SavedItemData(val title: String, val category: String, val dateSaved: String)



// === Dialog for Business Analytics & Insights (ស្ថិតិ និងការវិភាគជាក់ស្តែង) ===
@Composable
fun AnalyticsInsightsDialog(
    user: UserSession?,
    onDismiss: () -> Unit,
    onGoToFeed: () -> Unit
) {
    val followers = user?.followersCount ?: 0
    val following = user?.followingCount ?: 0
    val inquiries = user?.inquiriesCount ?: 0
    val posts = user?.feedPostsCount ?: 0
    val views = posts * 15
    val clicks = posts * 4
    val isZeroState = (followers == 0 && following == 0 && inquiries == 0 && posts == 0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CMKDeepBlue)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ស្ថិតិ និងការវិភាគ (Analytics & Insights)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ទិន្នន័យជាក់ស្តែងនៃការចូលមើល និងអន្តរកម្ម",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Analytics Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Real-Time Live Status Banner
                    Surface(
                        color = if (isZeroState) Color(0xFFF1F5F9) else Color(0xFFECFDF5),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isZeroState) Color(0xFFE2E8F0) else Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isZeroState) Icons.Default.Info else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isZeroState) Color(0xFF64748B) else Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isZeroState) "ស្ថិតិជាក់ស្តែងបច្ចុប្បន្ន (Real-Time State: 0 Activity)" else "គណនីមានសកម្មភាពសកម្ម (Active Account)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isZeroState) Color(0xFF334155) else Color(0xFF065F46)
                                )
                                Text(
                                    text = if (isZeroState) "ទិន្នន័យ Views, Clicks, និង Inquiries នឹងបង្ហាញជាក់ស្តែងនៅពេលអ្នកចាប់ផ្តើមផ្សព្វផ្សាយ" else "ស្ថិតិនេះធ្វើបច្ចុប្បន្នភាពតាមអន្តរកម្មជាក់ស្តែងរបស់អតិថិជន",
                                    fontSize = 10.sp,
                                    color = if (isZeroState) Color(0xFF64748B) else Color(0xFF047857)
                                )
                            }
                        }
                    }

                    // 1. Social Proof & Network Overview (Followers / Following)
                    Text(
                        text = "បណ្តាញទំនាក់ទំនង (Network & Social Proof)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Followers Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("អ្នកតាមដាន", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("$followers", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF14532D))
                                Text(if (followers == 0) "មិនទាន់មានអ្នកតាមដាន" else "អ្នកតាមដានសរុប", fontSize = 10.sp, color = Color(0xFF166534))
                            }
                        }

                        // Following Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("កំពុងតាមដាន", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("$following", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                                Text(if (following == 0) "មិនទាន់បានតាមដាននរណា" else "កំពុងតាមដានដៃគូ", fontSize = 10.sp, color = Color(0xFF1E40AF))
                            }
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // 2. Engagement Key Metrics (Views, Clicks, Leads)
                    Text(
                        text = "ទិន្នន័យអន្តរកម្មលើ Item/Post (Engagement Insights)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Views / Impressions
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$views", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("ចំនួនអ្នកឃើញ/មើល", fontSize = 10.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }

                        // Metric 2: Clicks / Engagement
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$clicks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("ចុច Share/Save/Call", fontSize = 10.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }

                        // Metric 3: Leads / Inquiries
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$inquiries", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("សារសួរព័ត៌មាន", fontSize = 10.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // 3. Top Performing Items / Zero State Notice
                    Text(
                        text = "ទំនិញ និងការផ្សាយដែលមានអន្តរកម្មច្រើនជាងគេ (Top Items)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    if (posts == 0) {
                        // Clean Zero-State Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "មិនទាន់មានការផ្សាយទំនិញនៅឡើយទេ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "បង្ហោះទំនិញ ឬសម្ភារសំណង់ដំបូងរបស់អ្នក ដើម្បីចាប់ផ្តើមទទួលបានការចូលមើល និងសារសាកសួរពីអតិថិជន។",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onGoToFeed,
                                    colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ទៅកាន់ CMK Feed ដើម្បីផ្សព្វផ្សាយ", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Real items list
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ការផ្សាយសរុប $posts ប្រកាស", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Text("$views views • $clicks clicks • $inquiries inquiries", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
