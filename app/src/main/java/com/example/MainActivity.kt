package com.example

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CMKDeepBlue
import com.example.ui.theme.CMKGoldAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.screen.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Instantiate ViewModel directly using standard Android ViewModelProvider
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (appTheme) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemInDark
            }

            MyApplicationTheme(darkTheme = isDark) {
                AppScaffold(viewModel, isDark)
            }
        }
    }
}

@Composable
fun AppScaffold(viewModel: MainViewModel, isDark: Boolean) {
    val currentUser by viewModel.currentUserSession.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    
    if (currentUser == null) {
        // Mandatory Auth Guard: Redirects instantly to Auth Screen
        AuthScreen(viewModel = viewModel)
    } else if (isAppLocked) {
        // Passcode Guard: Forces unlocking via app password passcode
        PasscodeLockScreen(viewModel = viewModel)
    } else {
        // Logged-in Core App Navigation
        val currentScreen by viewModel.currentScreen.collectAsState()
        val envMode by viewModel.environmentalMode.collectAsState()
        val isFeedScrollMenuVisible by viewModel.isFeedScrollMenuVisible.collectAsState()

        val primaryScreens = remember {
            listOf(
                AppScreen.FEED,
                AppScreen.CATALOGUE,
                AppScreen.CALL_LOG,
                AppScreen.CHAT,
                AppScreen.PROFILE
            )
        }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { primaryScreens.size }
        )
        val coroutineScope = rememberCoroutineScope()

        // Synchronize when currentScreen is changed programmatically
        LaunchedEffect(currentScreen) {
            val targetIndex = primaryScreens.indexOf(currentScreen)
            if (targetIndex != -1 && pagerState.currentPage != targetIndex) {
                pagerState.animateScrollToPage(targetIndex)
            }
        }

        // Synchronize when user swipes pages with swipe gesture
        LaunchedEffect(pagerState.currentPage) {
            val targetScreen = primaryScreens[pagerState.currentPage]
            if (viewModel.currentScreen.value != targetScreen && viewModel.currentScreen.value != AppScreen.SETTINGS) {
                viewModel.navigateTo(targetScreen)
            }
        }

        val showBottomBar = if (currentScreen == AppScreen.FEED || currentScreen == AppScreen.CATALOGUE) isFeedScrollMenuVisible else true
        val showBanner = if (currentScreen == AppScreen.FEED || currentScreen == AppScreen.CATALOGUE) isFeedScrollMenuVisible else true

        val scaffoldBgColor = if (isDark) Color.Black else Color.White
        val bottomBarBgColor = if (isDark) Color(0xFF121212) else Color.White
        val bottomBarItemColor = if (isDark) Color.White else CMKDeepBlue

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (currentScreen != AppScreen.SETTINGS) {
                    AnimatedVisibility(
                        visible = showBottomBar,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(durationMillis = 400)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(durationMillis = 400)
                        )
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bottom_nav_bar"),
                            color = Color(0xFF071D41), // Rich dark navy blue matching user design
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .height(56.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CMKBottomNavItem(
                                    icon = Icons.Default.Home,
                                    contentDescription = "Home",
                                    selected = currentScreen == AppScreen.FEED,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(0)
                                        }
                                        viewModel.navigateTo(AppScreen.FEED)
                                    },
                                    testTag = "nav_feed_tab"
                                )
                                CMKBottomNavItem(
                                    icon = Icons.Default.ShoppingCart,
                                    contentDescription = "Store",
                                    selected = currentScreen == AppScreen.CATALOGUE,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                        viewModel.navigateTo(AppScreen.CATALOGUE)
                                    },
                                    testTag = "nav_store_tab"
                                )
                                CMKBottomNavItem(
                                    icon = Icons.Default.People,
                                    contentDescription = "Friends",
                                    selected = currentScreen == AppScreen.CALL_LOG,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(2)
                                        }
                                        viewModel.navigateTo(AppScreen.CALL_LOG)
                                    },
                                    testTag = "nav_call_tab"
                                )
                                CMKBottomNavItem(
                                    icon = Icons.Default.Forum,
                                    contentDescription = "Chat",
                                    selected = currentScreen == AppScreen.CHAT,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(3)
                                        }
                                        viewModel.navigateTo(AppScreen.CHAT)
                                    },
                                    testTag = "nav_chat_tab"
                                )
                                CMKBottomNavItem(
                                    icon = Icons.Outlined.Person,
                                    contentDescription = "Profile",
                                    selected = currentScreen == AppScreen.PROFILE,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(4)
                                        }
                                        viewModel.navigateTo(AppScreen.PROFILE)
                                    },
                                    testTag = "nav_profile_tab"
                                )
                            }
                        }
                    }
                }
            },
            containerColor = scaffoldBgColor
        ) { innerPadding ->
            val targetBottomPadding = if (currentScreen != AppScreen.SETTINGS) {
                if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
            } else {
                0.dp
            }
            val animatedBottomPadding by androidx.compose.animation.core.animateDpAsState(
                targetValue = targetBottomPadding,
                animationSpec = tween(durationMillis = 400),
                label = "bottomPadding"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = animatedBottomPadding
                    )
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (currentScreen == AppScreen.SETTINGS) {
                        SettingsScreen(viewModel = viewModel)
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1
                        ) { page ->
                            when (primaryScreens[page]) {
                                AppScreen.FEED -> FeedScreen(viewModel = viewModel)
                                AppScreen.CATALOGUE -> StoreScreen(viewModel = viewModel)
                                AppScreen.CALL_LOG -> CallScreen(viewModel = viewModel)
                                AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                                AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                                else -> FeedScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CMKBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF8F6B05) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "pillBgColor"
    )
    val pillWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (selected) 64.dp else 44.dp,
        animationSpec = tween(durationMillis = 250),
        label = "pillWidth"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(pillWidth)
                .height(36.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(pillBgColor)
                .clickable(onClick = onClick)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
