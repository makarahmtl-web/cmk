package com.example.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.data.model.FeedPost
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val posts by viewModel.feedPosts.collectAsState()
    val user by viewModel.currentUserSession.collectAsState()
    val isMenuVisible by viewModel.isFeedScrollMenuVisible.collectAsState()
    val followedUserIds by viewModel.followedUserIds.collectAsState()

    var showPostCreator by rememberSaveable { mutableStateOf(false) }
    var activePostForComments by remember { mutableStateOf<FeedPost?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilterTab by rememberSaveable { mutableStateOf("ទាំងអស់") }

    // Filter posts based on search query
    val displayedPosts = remember(posts, searchQuery) {
        if (searchQuery.isBlank()) posts
        else posts.filter { 
            it.content.contains(searchQuery, ignoreCase = true) ||
            it.userDisplayName.contains(searchQuery, ignoreCase = true)
        }
    }

    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                if (delta < -15f) {
                    viewModel.setFeedScrollMenuVisible(false)
                } else if (delta > 15f) {
                    viewModel.setFeedScrollMenuVisible(true)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isMenuVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 600)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 600)
                )
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("CMK Social Hub", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 19.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    },
                    actions = {
                        // Invite / Share Action
                        IconButton(
                            onClick = {
                                val sendIntent: android.content.Intent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "សូមចូលរួមបណ្តាញផ្គត់ផ្គង់ និងម៉ៅការសំណង់ CMK Construction Hub ឥឡូវនេះ! https://cmkconstruction.com")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "ចែករំលែកទៅកាន់មិត្តភក្តិ"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("btn_feed_top_menu")
                        ) {
                            FourStoryMenuIcon(tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CMKDeepBlue
                    )
                )
            }
        },
        containerColor = Color(0xFFF1F5F9),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPostCreator = true }, 
                containerColor = CMKDeepBlue, 
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        val targetTopPadding = if (isMenuVisible) innerPadding.calculateTopPadding() else 0.dp
        val animatedTopPadding by androidx.compose.animation.core.animateDpAsState(
            targetValue = targetTopPadding,
            animationSpec = tween(durationMillis = 600),
            label = "topPadding"
        )

        Box(
            modifier = Modifier
                .padding(top = animatedTopPadding)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. GLOBAL FACEBOOK-STYLE SEARCH & FILTER BAR
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { 
                                    searchQuery = it
                                    viewModel.searchPosts(it)
                                },
                                placeholder = { 
                                    Text(
                                        "ស្វែងរកការផុស មិត្តភក្តិ ឬទីតាំងសំណង់...", 
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    ) 
                                },
                                textStyle = TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF0F172A))
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0F172A),
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    unfocusedContainerColor = Color(0xFFF1F5F9),
                                    focusedContainerColor = Color.White,
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedPlaceholderColor = Color(0xFF64748B),
                                    unfocusedPlaceholderColor = Color(0xFF64748B)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Filter Chips with high contrast text & borders
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ទាំងអស់", "ការផុសពេញនិយម", "រូបភាព", "វីដេអូ").forEach { tab ->
                                    val isSelected = selectedFilterTab == tab
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilterTab = tab },
                                        label = { 
                                            Text(
                                                tab, 
                                                fontSize = 12.sp, 
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) Color.White else Color(0xFF334155)
                                            ) 
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CMKDeepBlue,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFFF1F5F9),
                                            labelColor = Color(0xFF334155)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = Color(0xFFCBD5E1),
                                            selectedBorderColor = CMKDeepBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. POSTS FEED LIST
                if (displayedPosts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Feed,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isNotEmpty()) "មិនមានការផុសណាដែលត្រូវនឹង '$searchQuery' ទេ" else "មិនទាន់មានការផុសព័ត៌មាននៅឡើយទេ",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            Text(
                                "ក្លាយជាអ្នកដំបូងគេដែលចែករំលែករូបភាព រឿងរ៉ាវ ឬព័ត៌មានលើបណ្តាញសង្គម!",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    items(displayedPosts, key = { it.id }) { post ->
                        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                            PostItemCard(
                                post = post,
                                currentUserId = user?.uid,
                                onLike = { viewModel.toggleLike(post.id) },
                                onCommentClick = {
                                    activePostForComments = post
                                    viewModel.setCommentPostId(post.id)
                                },
                                onBlockUser = { viewModel.blockUser(post.userDisplayName) }
                            )
                        }
                    }
                }
            }

            // Post Creator Sheet
            if (showPostCreator) {
                PostCreatorDialog(
                    userInitials = user?.avatarInitials ?: "CMK",
                    onDismiss = { showPostCreator = false },
                    onPostCreated = { text, imgUri, filter ->
                        viewModel.createPost(context, text, imgUri, filter)
                        showPostCreator = false
                    }
                )
            }

            // Comments Bottom Sheet Dialog
            activePostForComments?.let { post ->
                CommentsDialog(
                    post = post,
                    viewModel = viewModel,
                    onDismiss = {
                        activePostForComments = null
                        viewModel.setCommentPostId(null)
                    }
                )
            }
        }
    }
}

@Composable
fun PostItemCard(
    post: FeedPost,
    currentUserId: String?,
    onLike: () -> Unit,
    onCommentClick: () -> Unit,
    onBlockUser: () -> Unit
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var isSavedPost by rememberSaveable { mutableStateOf(false) }
    var isHiddenPost by rememberSaveable { mutableStateOf(false) }
    var isReported by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var isCommentClicked by rememberSaveable { mutableStateOf(false) }
    val commentScale by animateFloatAsState(
        targetValue = if (isCommentClicked) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { if (isCommentClicked) isCommentClicked = false },
        label = "commentScale"
    )

    var isShareClicked by rememberSaveable { mutableStateOf(false) }
    val shareScale by animateFloatAsState(
        targetValue = if (isShareClicked) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { if (isShareClicked) isShareClicked = false },
        label = "shareScale"
    )

    var isSaveClicked by rememberSaveable { mutableStateOf(false) }
    val saveScale by animateFloatAsState(
        targetValue = if (isSaveClicked) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { if (isSaveClicked) isSaveClicked = false },
        label = "saveScale"
    )

    val likeScale by animateFloatAsState(
        targetValue = if (post.isLikedByMe) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "likeScale"
    )

    // If hidden, show a neat undo card
    if (isHiddenPost) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ការផ្សាយនេះត្រូវបានលាក់ (Hidden)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Text("អ្នកនឹងលែងបានឃើញការផ្សាយនេះទៀតហើយ។", fontSize = 10.sp, color = Color.LightGray)
                }
                TextButton(
                    onClick = { isHiddenPost = false }
                ) {
                    Text("សារដើម (Undo)", fontWeight = FontWeight.ExtraBold, color = CMKDeepBlue, fontSize = 11.sp)
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Profile & Meta
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(42.dp).background(CMKDeepBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.userAvatarInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.userDisplayName, fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.timeAgo, fontSize = 9.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                    }
                }

                // Options Menu (Follow, Save, Hide, Report, Block)
                if (currentUserId != null) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isFollowing) "ឈប់តាមដាន ${post.userDisplayName} (Unfollow)" else "តាមដាន ${post.userDisplayName} (Follow)", color = CMKDeepBlue, fontSize = 12.sp) },
                                leadingIcon = { Icon(if (isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    isFollowing = !isFollowing
                                    Toast.makeText(context, if (isFollowing) "កំពុងតាមដាន ${post.userDisplayName}" else "ឈប់តាមដាន ${post.userDisplayName}", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isSavedPost) "ឈប់រក្សាទុកការផ្សាយ (Unsave)" else "រក្សាទុកការផ្សាយ (Save Post)", color = CMKDeepBlue, fontSize = 12.sp) },
                                leadingIcon = { Icon(if (isSavedPost) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    isSavedPost = !isSavedPost
                                    Toast.makeText(context, if (isSavedPost) "បានរក្សាទុកការផ្សាយនេះ!" else "បានដកការរក្សាទុក!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("លាក់ការផ្សាយនេះ (Hide Post)", color = Color.DarkGray, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    isHiddenPost = true
                                    Toast.makeText(context, "បានលាក់ការផ្សាយនេះ", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("រាយការណ៍ការផ្សាយ (Report Post)", color = Color.Red, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showMenu = false
                                    isReported = true
                                    Toast.makeText(context, "សូមអរគុណសម្រាប់ការរាយការណ៍។ ក្រុមការងារ CMK នឹងពិនិត្យមើលវា!", Toast.LENGTH_LONG).show()
                                }
                            )
                            if (post.userId != currentUserId) {
                                DropdownMenuItem(
                                    text = { Text("Block ${post.userDisplayName}", color = Rose500, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = Rose500, modifier = Modifier.size(18.dp)) },
                                    onClick = {
                                        showMenu = false
                                        onBlockUser()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Content Text
            Text(
                text = post.content,
                fontSize = 13.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                lineHeight = 18.sp
            )

            // Content Media (Video or Images)
            if (post.videoUrl != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(0.dp))) {
                    VideoPlayerItem(videoUrl = post.videoUrl, modifier = Modifier.fillMaxSize())
                }
            } else {
                val imagesList = if (post.imageUrls.isNotEmpty()) post.imageUrls else listOfNotNull(post.imageUrl)
                if (imagesList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (imagesList.size == 1) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imagesList[0])
                                .crossfade(true)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Post Image",
                            modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(0.dp)),
                            contentScale = ContentScale.Crop,
                            colorFilter = getColorFilterForName(post.imageFilter)
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(210.dp)) {
                            LazyRow(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(imagesList, key = { it }) { imgUrl ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imgUrl)
                                            .crossfade(true)
                                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = "Post Image",
                                        modifier = Modifier.width(300.dp).fillMaxHeight(),
                                        contentScale = ContentScale.Crop,
                                        colorFilter = getColorFilterForName(post.imageFilter)
                                    )
                                }
                            }
                            // Beautiful Floating badge for multiple images count
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "រូបភាព 1/${imagesList.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reaction Counts (Likes & Comments)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = Rose500, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likesCount + (if (post.isLikedByMe) 0 else 0)} Likes", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
                Text("${post.commentsCount} Comments", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = Color(0xFFF1F5F9))

            // Facebook-style 4-action row (Like, Comment, Share, Save)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLike() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByMe) Rose500 else Color.Gray,
                        modifier = Modifier
                            .graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "ពេញចិត្ត",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (post.isLikedByMe) Rose500 else Color.Gray,
                        modifier = Modifier.graphicsLayer(scaleX = likeScale, scaleY = likeScale)
                    )
                }

                // Comment Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { 
                            isCommentClicked = true
                            onCommentClick()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = if (isCommentClicked) CMKDeepBlue else Color.Gray,
                        modifier = Modifier
                            .graphicsLayer(scaleX = commentScale, scaleY = commentScale)
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "មតិយោបល់",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCommentClicked) CMKDeepBlue else Color.Gray,
                        modifier = Modifier.graphicsLayer(scaleX = commentScale, scaleY = commentScale)
                    )
                }

                // Share Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { 
                            isShareClicked = true
                            Toast.makeText(context, "បានចែករំលែកទៅកាន់មិត្តភក្ដិរបស់អ្នករួចរាល់!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = if (isShareClicked) CMKGoldAccent else Color.Gray,
                        modifier = Modifier
                            .graphicsLayer(scaleX = shareScale, scaleY = shareScale)
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "ចែករំលែក",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShareClicked) CMKGoldAccent else Color.Gray,
                        modifier = Modifier.graphicsLayer(scaleX = shareScale, scaleY = shareScale)
                    )
                }

                // Save Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { 
                            isSaveClicked = true
                            isSavedPost = !isSavedPost
                            Toast.makeText(context, if (isSavedPost) "បានរក្សាទុកការផ្សាយនេះ!" else "បានដកការរក្សាទុក!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSavedPost) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSavedPost) CMKGoldAccent else Color.Gray,
                        modifier = Modifier
                            .graphicsLayer(scaleX = saveScale, scaleY = saveScale)
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "រក្សាទុក",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSavedPost) CMKGoldAccent else Color.Gray,
                        modifier = Modifier.graphicsLayer(scaleX = saveScale, scaleY = saveScale)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreatorDialog(
    userInitials: String,
    onDismiss: () -> Unit,
    onPostCreated: (String, List<String>, String?) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf<String?>("ORIGINAL") }
    var isCompressing by remember { mutableStateOf(false) }

    // Facebook Privacy, Tag, Location, Feeling State
    var selectedPrivacy by rememberSaveable { mutableStateOf("សាធារណៈ") }
    var selectedLocation by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFeeling by rememberSaveable { mutableStateOf<String?>(null) }
    var taggedFriends by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    // Sheet visibility states for Facebook Modal Bottom Sheets
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showTagSheet by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }
    var showFeelingSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            isCompressing = true
            coroutineScope.launch(Dispatchers.IO) {
                val compressedUris = uris.take(5).mapNotNull { uri ->
                    compressImageUri(context, uri)
                }
                withContext(Dispatchers.Main) {
                    selectedImages = (selectedImages + compressedUris).take(5)
                    isCompressing = false
                    Toast.makeText(context, "បានបញ្ចូលរូបភាព ${compressedUris.size} សន្លឹកជោគជ័យ!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    "បង្កើតការផ្សាយ (Create Post)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF0F172A))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                        actions = {
                            val canPost = (text.isNotBlank() || selectedImages.isNotEmpty()) && !isCompressing
                            Button(
                                onClick = {
                                    if (canPost) {
                                        val finalText = buildString {
                                            append(text.trim())
                                            if (taggedFriends.isNotEmpty()) {
                                                append("\n👤 ជាមួយ៖ ${taggedFriends.joinToString(", ")}")
                                            }
                                            if (!selectedLocation.isNullOrBlank()) {
                                                append("\n📍 នៅ៖ $selectedLocation")
                                            }
                                            if (!selectedFeeling.isNullOrBlank()) {
                                                append("\n✨ $selectedFeeling")
                                            }
                                        }
                                        onPostCreated(finalText, selectedImages, selectedFilter)
                                    }
                                },
                                enabled = canPost,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canPost) Color(0xFF1877F2) else Color(0xFFE2E8F0),
                                    contentColor = if (canPost) Color.White else Color(0xFF94A3B8)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("ផុស", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    )
                },
                bottomBar = {
                    // Persistent Facebook Bottom Attachment Toolbar (Raised above gesture bar)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding(),
                        color = Color.White,
                        tonalElevation = 8.dp,
                        shadowElevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 68.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "បន្ថែមក្នុងការផ្សាយ (Add to Post)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    fontSize = 13.sp
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Photo/Video
                                    IconButton(
                                        onClick = {
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Collections, contentDescription = "Photo", tint = Color(0xFF45BD62), modifier = Modifier.size(24.dp))
                                    }

                                    // 2. Tag Friends
                                    IconButton(
                                        onClick = { showTagSheet = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = "Tag Friends", tint = Color(0xFF1877F2), modifier = Modifier.size(24.dp))
                                    }

                                    // 3. Location
                                    IconButton(
                                        onClick = { showLocationSheet = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFFF5533D), modifier = Modifier.size(24.dp))
                                    }

                                    // 4. Feeling/Activity
                                    IconButton(
                                        onClick = { showFeelingSheet = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.EmojiEmotions, contentDescription = "Feeling", tint = Color(0xFFF7B928), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Profile Header & Facebook Privacy/Tag Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF1877F2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(userInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ឈួយ មករា",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (taggedFriends.isNotEmpty()) {
                                    Text(
                                        text = " ជាមួយ ${taggedFriends.first()} ${if (taggedFriends.size > 1) "និង ${taggedFriends.size - 1} នាក់ផ្សេងទៀត" else ""}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Horizontal Chip Selector Row (Facebook Style)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Privacy Chip
                                item {
                                    Surface(
                                        onClick = { showPrivacySheet = true },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFE7F3FF),
                                        border = BorderStroke(1.dp, Color(0xFF1877F2).copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(selectedPrivacy, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                // Tag People Chip
                                item {
                                    Surface(
                                        onClick = { showTagSheet = true },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (taggedFriends.isNotEmpty()) Color(0xFFE7F3FF) else Color(0xFFF1F5F9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                if (taggedFriends.isEmpty()) "+ មនុស្ស" else "${taggedFriends.size} នាក់",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                    }
                                }

                                // Location Chip
                                item {
                                    Surface(
                                        onClick = { showLocationSheet = true },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selectedLocation != null) Color(0xFFFFE4E6) else Color(0xFFF1F5F9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                selectedLocation ?: "+ ទីតាំង",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selectedLocation != null) Color(0xFF9F1239) else Color(0xFF334155),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Feeling Chip
                                item {
                                    Surface(
                                        onClick = { showFeelingSheet = true },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (selectedFeeling != null) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.EmojiEmotions, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                selectedFeeling ?: "+ អារម្មណ៍",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selectedFeeling != null) Color(0xFF92400E) else Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text Input Area (Standard Facebook Unframed Large Input)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text(
                                "តើអ្នកកំពុងគិតអ្វី? (What's on your mind?)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF64748B)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 120.dp)
                            .testTag("post_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A)
                        ),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, color = Color(0xFF0F172A))
                    )

                    // Selected Images Display Grid
                    if (selectedImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        selectedImages.forEachIndexed { index, uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Photo ${index + 1}",
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                    contentScale = ContentScale.FillWidth
                                )
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.toMutableList().apply { removeAt(index) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .size(30.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // -------------------------------------------------------------
    // SLIDE-UP MODAL BOTTOM SHEETS (Facebook Standard)
    // -------------------------------------------------------------

    // 1. PRIVACY SHEET (អ្នកមើលសារលិខិត)
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "អ្នកមើលសារលិខិត",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    "តើអ្នកណាខ្លះអាចមើលឃើញការផ្សាយរបស់អ្នក?",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val privacyOptions = listOf(
                    Triple("សាធារណៈ", "នរណាក៏ដោយនៅលើ ឬក្រៅពី Facebook", Icons.Default.Public),
                    Triple("មិត្តភក្តិ", "មិត្តភក្តិរបស់អ្នកនៅលើ Facebook", Icons.Default.People),
                    Triple("មិត្តភក្តិលើកលែងតែ...", "កុំបង្ហាញដល់មិត្តភក្តិទាំងនេះ", Icons.Default.PersonOff),
                    Triple("តែខ្ញុំប៉ុណ្ណោះ", "បង្ហាញតែរូបអ្នកប៉ុណ្ណោះ", Icons.Default.Lock)
                )

                privacyOptions.forEach { (title, desc, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPrivacy = title
                                showPrivacySheet = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                            Text(desc, fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        RadioButton(
                            selected = selectedPrivacy == title,
                            onClick = {
                                selectedPrivacy = title
                                showPrivacySheet = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1877F2))
                        )
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 2. TAG PEOPLE SHEET (ស្លាកអ្នកផ្សេង)
    if (showTagSheet) {
        var friendSearchQuery by remember { mutableStateOf("") }
        val allFriends = listOf(
            "សំរិត ស៊ុនស៊ុង", "ប៊ុន លុយ", "ឈួយ មករា", "អ៊ុំ សុវណ្ណ",
            "ស៊ឹម កឹមស៊ិន", "ម៉ារី ណា", "ទ្រី សុខហេង", "សៀវ ម៉ី"
        )
        val filteredFriends = allFriends.filter { it.contains(friendSearchQuery, ignoreCase = true) }

        ModalBottomSheet(
            onDismissRequest = { showTagSheet = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ស្លាកអ្នកផ្សេង", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Button(
                        onClick = { showTagSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                    ) {
                        Text("រួចរាល់", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = friendSearchQuery,
                    onValueChange = { friendSearchQuery = it },
                    placeholder = { Text("ស្វែងរកមិត្តភក្តិ...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF8FAFC))
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(filteredFriends) { friend ->
                        val isSelected = taggedFriends.contains(friend)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    taggedFriends = if (isSelected) {
                                        taggedFriends - friend
                                    } else {
                                        taggedFriends + friend
                                    }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF1877F2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(friend.take(2), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(friend, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    taggedFriends = if (checked) taggedFriends + friend else taggedFriends - friend
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1877F2))
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 3. LOCATION SHEET (បន្ថែមទីតាំង)
    if (showLocationSheet) {
        var locationQuery by remember { mutableStateOf("") }
        val locationsList = listOf(
            "Kandol, Battambang, Cambodia",
            "Ta Phraya, Sa Kaeo, Thailand",
            "ប្រាសាទបន្ទាយឆ្មារ - Banteay Chmar",
            "សាលារៀនតាប្រាយ៉ា (Taphraya School)",
            "ភូមិថ្មពួក",
            "រាជធានីភ្នំពេញ",
            "ខេត្តសៀមរាប",
            "ខេត្តបាត់ដំបង",
            "ខេត្តព្រះសីហនុ"
        ).filter { it.contains(locationQuery, ignoreCase = true) }

        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("បន្ថែមទីតាំង", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = { locationQuery = it },
                    placeholder = { Text("ស្វែងរកទីតាំង...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFF8FAFC))
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(locationsList) { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLocation = loc
                                    showLocationSheet = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(loc, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                            if (selectedLocation == loc) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 4. FEELING / ACTIVITY SHEET (តើអ្នកមានអារម្មណ៍ដូចម្តេច?)
    if (showFeelingSheet) {
        val feelingsList = listOf(
            "😊 សប្បាយចិត្ត", "😍 មានសេចក្តីសុខ", "😎 អស្ចារ្យ",
            "😴 អស់កម្លាំង", "🤔 គិតច្រើន", "🥳 រំភើប",
            "😇 មានសន្តិភាព", "🤩 រំភើបចិត្តខ្លាំង", "💡 កំពុងគិតគូរ"
        )

        ModalBottomSheet(
            onDismissRequest = { showFeelingSheet = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("តើអ្នកមានអារម្មណ៍ដូចម្តេច?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(feelingsList) { feel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFeeling = feel
                                    showFeelingSheet = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(feel, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                            if (selectedFeeling == feel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// True asynchronous client-side image compression helper
fun compressImageUri(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (originalBitmap == null) return null
        
        // Scale down to max 800px width/height for premium client-side performance
        val maxDim = 800
        val (width, height) = if (originalBitmap.width > originalBitmap.height) {
            val ratio = originalBitmap.height.toFloat() / originalBitmap.width
            maxDim to (maxDim * ratio).toInt()
        } else {
            val ratio = originalBitmap.width.toFloat() / originalBitmap.height
            (maxDim * ratio).toInt() to maxDim
        }
        
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        val outputStream = ByteArrayOutputStream()
        // Compress JPEG to 75% quality
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val bytes = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsDialog(
    post: FeedPost,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val comments by viewModel.activeComments.collectAsState()
    var commentText by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f).padding(horizontal = 16.dp)
        ) {
            Text(
                "Comments",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Comments list
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (comments.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No comments yet", color = Color.Gray, fontSize = 13.sp)
                            Text("Be the first to share your opinion!", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                } else {
                    items(comments) { comment ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier.size(36.dp).background(CMKDeepBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(comment.userAvatarInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)).padding(12.dp).weight(1f)
                            ) {
                                Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                Text(comment.content, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Comment input
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Write a comment...") },
                    modifier = Modifier.weight(1f).testTag("comment_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CMKDeepBlue,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    maxLines = 3,
                    singleLine = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(post.id, commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier.background(if (commentText.isNotBlank()) CMKDeepBlue else Color.LightGray, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

fun getColorFilterForName(name: String?): ColorFilter? {
    return when (name) {
        "GRAYSCALE" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        "VINTAGE" -> {
            val sepiaMatrix = ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f,     0f,     0f,     1f, 0f
            ))
            ColorFilter.colorMatrix(sepiaMatrix)
        }
        "WARM" -> {
            val warmMatrix = ColorMatrix(floatArrayOf(
                1.2f, 0f, 0f, 0f, 0f,
                0f, 1.0f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1.0f, 0f
            ))
            ColorFilter.colorMatrix(warmMatrix)
        }
        "COOL" -> {
            val coolMatrix = ColorMatrix(floatArrayOf(
                0.8f, 0f, 0f, 0f, 0f,
                0f, 1.0f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 0f,
                0f, 0f, 0f, 1.0f, 0f
            ))
            ColorFilter.colorMatrix(coolMatrix)
        }
        else -> null
    }
}
