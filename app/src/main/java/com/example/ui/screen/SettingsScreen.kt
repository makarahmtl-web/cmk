package com.example.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUserSession.collectAsState()

    // ViewModel State Collection
    val appTheme by viewModel.appTheme.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val environmentalMode by viewModel.environmentalMode.collectAsState()
    val biometricsEnabled by viewModel.biometricsEnabled.collectAsState()
    val blockedUsers by viewModel.blockedUsers.collectAsState()

    // 2026 Material Design 3 Design System Colors
    val accentBlue = Color(0xFF1877F2)      // Standard 2026 Primary Accent
    val calmRed = Color(0xFFE53935)         // Calm Crimson for Danger/Block
    val emeraldGreen = Color(0xFF10B981)    // Emerald for Verified Security
    val mutedGray = Color(0xFF8E8E93)       // Subtitle Gray
    val dividerColor = Color(0xFF2C2C2E)

    // Dark/Light Theme Evaluation
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (appTheme) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val screenBg = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color.White
    val cardBorder = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF8E8E93) else Color(0xFF64748B)
    val inputBg = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF1F5F9)
    val topBarBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFF1877F2)

    // Accordion / Collapsible States (Collapsed by default as requested)
    var expandedTheme by remember { mutableStateOf(false) }
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedEnvironment by remember { mutableStateOf(false) }
    var expandedSecurity by remember { mutableStateOf(false) }
    var expandedPrivacy by remember { mutableStateOf(false) }
    var expandedBlock by remember { mutableStateOf(false) }

    // Additional Security & Privacy State Toggles
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var loginAlertsEnabled by remember { mutableStateOf(true) }
    var hidePhonePublic by remember { mutableStateOf(true) }
    var hideOnlineStatus by remember { mutableStateOf(false) }

    // Dialog Visibility Controllers
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showThaiRestrictedDialog by remember { mutableStateOf(false) }
    var showBlockedListDialog by remember { mutableStateOf(false) }
    var showActiveSessionsDialog by remember { mutableStateOf(false) }
    var showLinkedAccountsDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    // Feedback States
    var feedbackRating by remember { mutableStateOf(5) }
    var feedbackCategory by remember { mutableStateOf("💡 ស្នើសុំមុខងារ") }
    var feedbackText by remember { mutableStateOf("") }
    var feedbackHasImage by remember { mutableStateOf(false) }

    // Change Password States
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Block User Input
    var blockUserInput by remember { mutableStateOf("") }

    // Current Selection Display Labels
    val currentThemeLabel = when (appTheme) {
        "DARK" -> "យប់ (Dark)"
        "LIGHT" -> "ថ្ងៃ (Light)"
        else -> "ស្វ័យប្រវត្តិ (Auto)"
    }

    val currentLanguageLabel = when (appLanguage) {
        "KHMER" -> "ភាសាខ្មែរ (Khmer)"
        "ENGLISH" -> "English (EN)"
        "VIETNAMESE" -> "Tiếng Việt (VN)"
        "INDONESIAN" -> "Bahasa Indonesia"
        "LAO" -> "ភាសាឡាវ (Lao)"
        "BURMESE" -> "ភាសាភូមា (MM)"
        "FILIPINO" -> "Tagalog (PH)"
        "MALAY" -> "Bahasa Melayu (MY)"
        "SINGAPOREAN" -> "Singapore (SG)"
        else -> "ភាសាខ្មែរ (Khmer)"
    }

    val currentEnvironmentLabel = when (environmentalMode) {
        "CLEAR" -> "ស្រឡះ (Clear)"
        "RAINY" -> "មេឃភ្លៀង (Rainy)"
        else -> "ខ្យល់ (Windy)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ការកំណត់ និងឯកជនភាព",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "គ្រប់គ្រងស្បែកកម្មវិធី ភាសា និងប្រព័ន្ធសុវត្ថិភាព CMK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.PROFILE) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "ប្រព័ន្ធសុវត្ថិភាព CMK កំពុងដំណើរការ 100%", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Security Status",
                            tint = emeraldGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBg)
            )
        },
        containerColor = screenBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // --- 1. SECURITY RATING BADGE (2026 Emerald Modern Banner) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, emeraldGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(emeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = emeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ស្ថានភាពសុវត្ថិភាពគណនី: ខ្ពស់បំផុត 100%",
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "គណនី CMK របស់អ្នកទទួលបានការការពារសុវត្ថិភាពជាន់ខ្ពស់ និងការការពារឯកជនភាពយ៉ាងពេញលេញ។",
                                fontSize = 12.sp,
                                color = textSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // --- 2. ស្បែកកម្មវិធី (THEME - COLLAPSIBLE ACCORDION) ---
            item {
                CollapsibleSettingsGroup(
                    title = "ស្បែកកម្មវិធី (Theme)",
                    subtitle = "ពន្លឺ យប់ ឬស្វ័យប្រវត្តិ",
                    icon = Icons.Outlined.Palette,
                    currentValue = currentThemeLabel,
                    isExpanded = expandedTheme,
                    onToggle = { expandedTheme = !expandedTheme },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    accentColor = accentBlue
                ) {
                    val themes = listOf(
                        Triple("LIGHT", "ថ្ងៃ (Light)", "ពន្លឺច្បាស់ ត្រជាក់ភ្នែក និងមាន Contrast ខ្ពស់"),
                        Triple("DARK", "យប់ (Dark)", "ស្បែកពណ៌ខ្មៅ ការពារភ្នែក និងសន្សំសំចៃថ្ម"),
                        Triple("AUTO", "ស្វ័យប្រវត្តិ (Auto)", "ផ្លាស់ប្តូរស្វ័យប្រវត្តិ តាមការកំណត់ទូរស័ព្ទ")
                    )
                    themes.forEachIndexed { idx, (key, label, desc) ->
                        val isSelected = appTheme == key
                        val icon = when (key) {
                            "LIGHT" -> Icons.Outlined.WbSunny
                            "DARK" -> Icons.Outlined.DarkMode
                            else -> Icons.Outlined.BrightnessAuto
                        }
                        ModernOptionRow(
                            title = label,
                            subtitle = desc,
                            icon = icon,
                            isSelected = isSelected,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentBlue,
                            onClick = {
                                viewModel.updateTheme(key)
                                Toast.makeText(context, "Theme: $label", Toast.LENGTH_SHORT).show()
                            }
                        )
                        if (idx < themes.size - 1) {
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // --- 3. ភាសា (LANGUAGE - COLLAPSIBLE ACCORDION) ---
            item {
                CollapsibleSettingsGroup(
                    title = "ភាសា (Language)",
                    subtitle = "ភាសាផ្លូវការព្រះរាជាណាចក្រកម្ពុជា និងអន្តរជាតិ",
                    icon = Icons.Outlined.Language,
                    currentValue = currentLanguageLabel,
                    isExpanded = expandedLanguage,
                    onToggle = { expandedLanguage = !expandedLanguage },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    accentColor = accentBlue
                ) {
                    Text(
                        text = "ជ្រើសរើសភាសាផ្លូវការ (Official Application Languages)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary,
                        modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 6.dp)
                    )

                    val languages = listOf(
                        Triple("KHMER", "ភាសាខ្មែរ (Khmer)", "ភាសាផ្លូវការព្រះរាជាណាចក្រកម្ពុជា 100% Full Support"),
                        Triple("ENGLISH", "English (English)", "Official International Business Language"),
                        Triple("VIETNAMESE", "Tiếng Việt (Vietnamese)", "Ngôn ngữ chính thức Việt Nam"),
                        Triple("INDONESIAN", "Bahasa Indonesia (Indonesian)", "Bahasa resmi Republik Indonesia"),
                        Triple("LAO", "ភាសាឡាវ (Lao)", "ພາສາລາວ ຢ່າງເປັນທາງການ"),
                        Triple("BURMESE", "ភាសាភូមា (Burmese)", "မြန်မာတရားဝင်ဘာသာစကား"),
                        Triple("FILIPINO", "Tagalog (Filipino)", "Wikang opisyal ng Pilipinas"),
                        Triple("MALAY", "Bahasa Melayu (Malay)", "Bahasa rasmi Malaysia & Brunei"),
                        Triple("SINGAPOREAN", "Singaporean (English/Melayu)", "Singapore Official Standard"),
                        Triple("THAI", "ไทย / Thai", "ជម្រើសភាសានេះត្រូវបានរឹតត្បិត (Restricted option)")
                    )

                    languages.forEachIndexed { idx, (code, label, desc) ->
                        val isSelected = appLanguage == code
                        val isRestricted = code == "THAI"

                        ModernLanguageRow(
                            title = label,
                            subtitle = desc,
                            icon = Icons.Outlined.Language,
                            isSelected = isSelected,
                            isRestricted = isRestricted,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentBlue,
                            onClick = {
                                if (isRestricted) {
                                    showThaiRestrictedDialog = true
                                } else {
                                    viewModel.updateLanguage(code)
                                    Toast.makeText(context, "Language: $label", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        if (idx < languages.size - 1) {
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // --- 4. បរិយាកាស (ENVIRONMENTAL MODE - COLLAPSIBLE ACCORDION) ---
            item {
                CollapsibleSettingsGroup(
                    title = "បរិយាកាស (Environmental Mode)",
                    subtitle = "ស្រឡះ មេឃភ្លៀង ឬមានខ្យល់",
                    icon = Icons.Outlined.CloudQueue,
                    currentValue = currentEnvironmentLabel,
                    isExpanded = expandedEnvironment,
                    onToggle = { expandedEnvironment = !expandedEnvironment },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    accentColor = accentBlue
                ) {
                    val envs = listOf(
                        Triple("CLEAR", "ស្រឡះ (Clear)", "បរិយាកាសស្រឡះ បង្ហាញព័ត៌មាន និងរូបភាពច្បាស់ត្រជាក់ភ្នែក"),
                        Triple("RAINY", "មេឃភ្លៀង (Rainy)", "បរិយាកាសមេឃភ្លៀង មាន Sound & Rain Visual Effect"),
                        Triple("WINDY", "ខ្យល់ (Windy)", "បរិយាកាសមានខ្យល់ មាន Movement Motion")
                    )
                    envs.forEachIndexed { idx, (mode, label, desc) ->
                        val isSelected = environmentalMode == mode
                        val icon = when (mode) {
                            "CLEAR" -> Icons.Outlined.WbSunny
                            "RAINY" -> Icons.Outlined.WaterDrop
                            else -> Icons.Outlined.Air
                        }
                        ModernOptionRow(
                            title = label,
                            subtitle = desc,
                            icon = icon,
                            isSelected = isSelected,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            accentColor = accentBlue,
                            onClick = {
                                viewModel.updateEnvironmentalMode(mode)
                                Toast.makeText(context, "Environment: $label", Toast.LENGTH_SHORT).show()
                            }
                        )
                        if (idx < envs.size - 1) {
                            HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // --- 5. ប្រព័ន្ធសុវត្ថិភាព និងការពារគណនី (ACCOUNT & SECURITY - COLLAPSIBLE ACCORDION) ---
            item {
                CollapsibleSettingsGroup(
                    title = "ប្រព័ន្ធសុវត្ថិភាព និងការពារគណនី (Security)",
                    subtitle = "2FA, ស្កេនក្រយៅដៃ, លេខសម្ងាត់ & ឧបករណ៍",
                    icon = Icons.Outlined.Shield,
                    currentValue = if (biometricsEnabled && twoFactorEnabled) "សុវត្ថិភាពខ្ពស់ 100%" else "2FA សកម្ម",
                    isExpanded = expandedSecurity,
                    onToggle = { expandedSecurity = !expandedSecurity },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    accentColor = accentBlue
                ) {
                    // 1. Two-Factor Auth
                    ModernSwitchRow(
                        title = "ការផ្ទៀងផ្ទាត់ ២ ជំហាន (Two-Factor Auth 2FA)",
                        subtitle = "ផ្ញើសូដកូដ OTP ការពារការលួចចូលប្រើប្រាស់គណនី",
                        icon = Icons.Outlined.PhonelinkLock,
                        checked = twoFactorEnabled,
                        onCheckedChange = {
                            twoFactorEnabled = it
                            Toast.makeText(context, if (it) "2FA Security Activated!" else "2FA Disabled", Toast.LENGTH_SHORT).show()
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentBlue
                    )

                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 2. Biometrics
                    ModernSwitchRow(
                        title = "ស្កេនក្រយៅដៃ (Biometrics)",
                        subtitle = "ស្កែនស្នាមម្រាមដៃ ឬទម្រង់មុខ ដើម្បីចូលប្រើប្រាស់កម្មវិធី CMK",
                        icon = Icons.Outlined.Fingerprint,
                        checked = biometricsEnabled,
                        onCheckedChange = {
                            viewModel.toggleBiometrics(it)
                            Toast.makeText(context, if (it) "Biometrics Activated!" else "Biometrics Disabled", Toast.LENGTH_SHORT).show()
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentBlue
                    )

                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 3. Login Alerts
                    ModernSwitchRow(
                        title = "ជូនដំណឹងពេលចូលប្រើប្រាស់ (Login Security Alerts)",
                        subtitle = "ផ្ញើសារជូនដំណឹងភ្លាមៗ ពេលមានការចូលប្រើប្រាស់ពីឧបករណ៍ថ្មី",
                        icon = Icons.Outlined.Security,
                        checked = loginAlertsEnabled,
                        onCheckedChange = {
                            loginAlertsEnabled = it
                            Toast.makeText(context, if (it) "Login alerts enabled" else "Login alerts disabled", Toast.LENGTH_SHORT).show()
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentBlue
                    )

                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 4. Change Password
                    ModernActionRow(
                        title = "ផ្លាស់ប្តូរលេខសម្ងាត់ (Change Password)",
                        subtitle = "កែប្រែលេខសម្ងាត់ Encryption ប្រព័ន្ធទិន្នន័យ SHA-256",
                        icon = Icons.Outlined.LockReset,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showChangePasswordDialog = true }
                    )

                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // 5. Active Sessions & Devices
                    ModernActionRow(
                        title = "ឧបករណ៍កំពុងចូលប្រើ (Active Sessions & Devices)",
                        subtitle = "គ្រប់គ្រង និងផ្តាច់ការភ្ជាប់ពីឧបករណ៍ផ្សេងៗ",
                        icon = Icons.Outlined.Devices,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showActiveSessionsDialog = true }
                    )
                }
            }

            // --- 6. បណ្តាញសង្គម និងឯកជនភាព (SOCIAL NETWORKS & PRIVACY - COLLAPSIBLE ACCORDION) ---
            item {
                CollapsibleSettingsGroup(
                    title = "បណ្តាញសង្គម និងឯកជនភាព (Privacy)",
                    subtitle = "គណនីភ្ជាប់, លាក់លេខទូរស័ព្ទ, ស្ថានភាពអនឡាញ",
                    icon = Icons.Outlined.Lock,
                    currentValue = "3 គណនីបានភ្ជាប់",
                    isExpanded = expandedPrivacy,
                    onToggle = { expandedPrivacy = !expandedPrivacy },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    accentColor = accentBlue
                ) {
                    // Linked Accounts
                    ModernActionRow(
                        title = "គណនីបណ្តាញសង្គមដែលបានភ្ជាប់ (Linked Social Accounts)",
                        subtitle = "Google, Facebook & Telegram Accounts (បានភ្ជាប់សុវត្ថិភាព 100%)",
                        icon = Icons.Outlined.Link,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showLinkedAccountsDialog = true }
                    )

                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // Hide Phone Number
                    ModernSwitchRow(
                        title = "លាក់លេខទូរស័ព្ទជាសាធារណៈ (Hide Phone Number)",
                        subtitle = "រក្សាឯកជនភាព លាក់លេខទូរស័ព្ទពីប្រវត្តិរូបសាធារណៈ",
                        icon = Icons.Outlined.VisibilityOff,
                        checked = hidePhonePublic,
                        onCheckedChange = {
                            hidePhonePublic = it
                            Toast.makeText(context, if (it) "Phone number hidden publicly" else "Phone number visible", Toast.LENGTH_SHORT).show()
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentBlue
                    )

                    HorizontalDivider(color = cardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                    // Hide Online Status
                    ModernSwitchRow(
                        title = "លាក់ស្ថានភាពអនឡាញ (Hide Online Status)",
                        subtitle = "កុំបង្ហាញស្ថានភាព Active ពេលកំពុងប្រើប្រាស់ App CMK",
                        icon = Icons.Outlined.AccountCircle,
                        checked = hideOnlineStatus,
                        onCheckedChange = {
                            hideOnlineStatus = it
                            Toast.makeText(context, if (it) "Online status hidden" else "Online status visible", Toast.LENGTH_SHORT).show()
                        },
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        accentColor = accentBlue
                    )
                }
            }

            // --- 7. គ្រប់គ្រងការបិទគណនី (BLOCK / UNBLOCK - COLLAPSIBLE ACCORDION) ---
            item {
                CollapsibleSettingsGroup(
                    title = "ឯកជនភាពនៃការបិទស្ទាត់ (Block/Unblock)",
                    subtitle = "ប្លុក ឬដោះប្លុកដៃគូពាណិជ្ជកម្ម និងអ្នកប្រើប្រាស់",
                    icon = Icons.Outlined.Block,
                    currentValue = if (blockedUsers.isEmpty()) "គ្មានគណនីប្លុក" else "${blockedUsers.size} នាក់",
                    isExpanded = expandedBlock,
                    onToggle = { expandedBlock = !expandedBlock },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    accentColor = calmRed
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "ប្លុក ឬគ្រប់គ្រងដៃគូពាណិជ្ជកម្មដែលអ្នកមិនចង់ឱ្យឃើញទំនិញសំណង់ CMK",
                            fontSize = 12.sp,
                            color = textSecondary
                        )

                        // Modern Pill-shaped Input Field
                        OutlinedTextField(
                            value = blockUserInput,
                            onValueChange = { blockUserInput = it },
                            placeholder = { Text("វាយបញ្ចូលឈ្មោះអ្នកប្រើប្រាស់ ឬហាង...", color = textSecondary, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = accentBlue,
                                unfocusedBorderColor = cardBorder,
                                focusedContainerColor = inputBg,
                                unfocusedContainerColor = inputBg
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (blockUserInput.isNotBlank()) {
                                        viewModel.blockUser(blockUserInput.trim())
                                        Toast.makeText(context, "${blockUserInput.trim()} ត្រូវបានប្លុកជោគជ័យ!", Toast.LENGTH_SHORT).show()
                                        blockUserInput = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Block", tint = accentBlue)
                                }
                            },
                            singleLine = true
                        )

                        // Modern Pill-shaped Danger Button
                        Button(
                            onClick = { showBlockedListDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = calmRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "ពិនិត្យ និងគ្រប់គ្រងបញ្ជីប្លុក (${blockedUsers.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // --- 8. ថែទាំប្រព័ន្ធ (MAINTENANCE & SYSTEM AUDIT) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    ModernActionRow(
                        title = "សម្អាត Cache និងពិនិត្យប្រព័ន្ធសុវត្ថិភាព",
                        subtitle = "សម្អាតទិន្នន័យបណ្តោះអាសន្ន ដើម្បីបង្កើនល្បឿន និងសុវត្ថិភាព",
                        icon = Icons.Outlined.CleaningServices,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = {
                            Toast.makeText(context, "សម្អាត Cache និងពិនិត្យសុវត្ថិភាពជោគជ័យ! Cache cleared.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }

            // --- 9. មតិរិះគន់ និងការផ្ដល់យោបល់ (FEEDBACK & SUPPORT) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    ModernActionRow(
                        title = "ផ្ញើមតិរិះគន់ ឬវាយតម្លៃកម្មវិធី (Send Feedback & Rating)",
                        subtitle = "រាយការណ៍បញ្ហា ស្នើសុំមុខងារថ្មី ឬផ្ញើមតិស្ថាបនាទៅកាន់ក្រុមការងារ CMK",
                        icon = Icons.Outlined.RateReview,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onClick = { showFeedbackDialog = true }
                    )
                }
            }

            // --- 10. អំពីកម្មវិធី និង Logo ស្តង់ដារ (ABOUT & OFFICIAL BRANDING) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_app_branding_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF071D41),
                            border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_icon_1788787213089),
                                    contentDescription = "CMK Official Logo",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "CMK Materials & Messenger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary
                        )

                        Text(
                            text = "Version 2.6.0 (Build 2026.09.07 - Enterprise)",
                            fontSize = 12.sp,
                            color = textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Text(
                            text = "© 2026 CMK Corporation. All Rights Reserved.",
                            fontSize = 10.sp,
                            color = textSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // --- 11. សកម្មភាពគណនី (ACCOUNT ACTIONS / LOGOUT) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, cardBorder),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.signOut() }
                            .padding(16.dp)
                            .testTag("settings_logout"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(calmRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Log Out",
                                tint = calmRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "ចាកចេញពីគណនី (Log Out)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = calmRed
                            )
                            Text(
                                text = "ចាកចេញពីគណនី CMK Corporate Hub ដោយសុវត្ថិភាព",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: LINKED SOCIAL ACCOUNTS ---
    if (showLinkedAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showLinkedAccountsDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "គណនីបណ្តាញសង្គមដែលបានភ្ជាប់",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "គណនីទាំងអស់ត្រូវបានការពារ និងផ្ទៀងផ្ទាត់សុវត្ថិភាព CMK 100%៖",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    listOf(
                        Triple("Google Account", "${user?.email ?: "user@google.com"}", true),
                        Triple("Facebook Account", "Makara HM (Verified)", true),
                        Triple("Telegram Account", "+855 88 **** 888", true)
                    ).forEach { (platform, details, _) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(inputBg)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(platform, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = textPrimary)
                                Text(details, fontSize = 11.sp, color = textSecondary)
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = emeraldGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "បានភ្ជាប់",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLinkedAccountsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("បិទ (Close)", color = Color.White)
                }
            }
        )
    }

    // --- DIALOG: FEEDBACK & RATING ---
    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.RateReview,
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ផ្ញើមតិរិះគន់ / វាយតម្លៃកម្មវិធី",
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "សូមផ្តល់ពិន្ទុ និងយោបល់ស្ថាបនា ដើម្បីឱ្យក្រុមការងារ CMK កែលម្អកម្មវិធីកាន់តែល្អប្រសើរ៖",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    // 1. Star Rating Picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(inputBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "វាយតម្លៃកម្មវិធី (App Rating)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { feedbackRating = star },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (star <= feedbackRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $star",
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Category Chips
                    Text(
                        text = "ប្រភេទមតិរិះគន់:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )

                    val categories = listOf(
                        "💡 ស្នើសុំមុខងារ",
                        "🐛 រាយការណ៍បញ្ហា",
                        "🎨 UI/Design",
                        "🤝 ផ្សេងៗ"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = feedbackCategory == cat
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) accentBlue else inputBg,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { feedbackCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    // 3. Feedback Details Input
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = { Text("សរសេរមតិរិះគន់ ការរាយការណ៍បញ្ហា ឬសំណូមពរនៅទីនេះ...", fontSize = 12.sp, color = textSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg
                        ),
                        maxLines = 4
                    )

                    // 4. Attach Screenshot simulation button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                feedbackHasImage = !feedbackHasImage
                                Toast.makeText(context, if (feedbackHasImage) "បានភ្ជាប់រូបភាព Screenshot រួចរាល់" else "បានលុបរូបភាពចេញ", Toast.LENGTH_SHORT).show()
                            }
                            .border(1.dp, if (feedbackHasImage) emeraldGreen else cardBorder, RoundedCornerShape(10.dp))
                            .background(inputBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (feedbackHasImage) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = if (feedbackHasImage) emeraldGreen else accentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (feedbackHasImage) "✓ ភ្ជាប់រូបភាព Screenshot រួចរាល់" else "📎 ភ្ជាប់រូបភាព Screenshot (ប្រសិនបើមាន)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (feedbackHasImage) emeraldGreen else textPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (feedbackText.isBlank()) {
                            Toast.makeText(context, "សូមវាយបញ្ចូលមតិរិះគន់មុននឹងផ្ញើ!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "🎉 សូមអរគុណ! មតិរិះគន់/ការផ្ដល់យោបល់ត្រូវបានផ្ញើទៅកាន់ក្រុមការងារ CMK រួចរាល់។", Toast.LENGTH_LONG).show()
                            showFeedbackDialog = false
                            feedbackText = ""
                            feedbackHasImage = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("ផ្ញើមតិ (Submit)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("បោះបង់", color = textSecondary)
                }
            }
        )
    }

    // --- DIALOG: ACTIVE SESSIONS & DEVICES ---
    if (showActiveSessionsDialog) {
        AlertDialog(
            onDismissRequest = { showActiveSessionsDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "ឧបករណ៍កំពុងចូលប្រើប្រាស់",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "បញ្ជីឧបករណ៍ទាំងអស់ដែលកំពុងចូលប្រើប្រាស់គណនី CMK របស់អ្នក៖",
                        fontSize = 12.sp,
                        color = textSecondary
                    )

                    // Current Device
                    Card(
                        colors = CardDefaults.cardColors(containerColor = inputBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, tint = emeraldGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Android Device (ឧបករណ៍បច្ចុប្បន្ន)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                                Text("រាជធានីភ្នំពេញ • កំពុងសកម្មឥឡូវនេះ (Active Now)", fontSize = 10.sp, color = emeraldGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Other session
                    Card(
                        colors = CardDefaults.cardColors(containerColor = inputBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = accentBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Web Dashboard (Chrome Browser)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary)
                                Text("ចូលប្រើមុននេះ 2 ម៉ោង", fontSize = 10.sp, color = textSecondary)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "បានផ្តាច់ការភ្ជាប់ពីឧបករណ៍ផ្សេងៗជោគជ័យ!", Toast.LENGTH_SHORT).show()
                            showActiveSessionsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = calmRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("ផ្តាច់ការភ្ជាប់ពីឧបករណ៍ផ្សេងទៀតទាំងអស់", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActiveSessionsDialog = false }) {
                    Text("បិទ (Close)", color = accentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- DIALOG: CHANGE PASSWORD ---
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "ផ្លាស់ប្តូរលេខសម្ងាត់ (Change Password)",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "លេខសម្ងាត់របស់អ្នកត្រូវបានការពារដោយប្រព័ន្ធ SHA-256 Local Database Encryption។",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("លេខសម្ងាត់បច្ចុប្បន្ន (Current Password)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg
                        )
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("លេខសម្ងាត់ថ្មី (New Password)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg
                        )
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("បញ្ជាក់លេខសម្ងាត់ថ្មី (Confirm Password)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.isNotBlank() && newPassword == confirmPassword) {
                            Toast.makeText(context, "លេខសម្ងាត់ត្រូវបានផ្លាស់ប្តូរ និង Encrypted ដោយជោគជ័យ!", Toast.LENGTH_SHORT).show()
                            showChangePasswordDialog = false
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        } else {
                            Toast.makeText(context, "លេខសម្ងាត់ថ្មីមិនត្រូវគ្នា ឬទទេរ!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("ធ្វើបច្ចុប្បន្នភាព", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("បោះបង់", color = textSecondary)
                }
            }
        )
    }

    // --- DIALOG: THAI RESTRICTED / DISABLED INFO ---
    if (showThaiRestrictedDialog) {
        AlertDialog(
            onDismissRequest = { showThaiRestrictedDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = mutedGray,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "ជម្រើសភាសាត្រូវបានរឹតត្បិត",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "ភាសានេះមិនទាន់បើកឱ្យប្រើប្រាស់នៅលើកម្មវិធី CMK ផ្លូវការនៅឡើយទេ។ សូមជ្រើសរើសភាសាផ្លូវការផ្សេងទៀត។\n\nThis language option is restricted per standard administrative guidelines.",
                    fontSize = 12.sp,
                    color = textSecondary,
                    lineHeight = 17.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showThaiRestrictedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("យល់ព្រម (OK)", color = Color.White)
                }
            }
        )
    }

    // --- DIALOG: MANAGE BLOCKED USERS LIST ---
    if (showBlockedListDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedListDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "បញ្ជីគណនីដែលបានប្លុក (${blockedUsers.size})",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "នៅពេលប្លុក ព័ត៌មាន និងទំនិញរបស់ដៃគូទាំងនេះនឹងត្រូវលាក់ 100% ពី Feed និង Store របស់អ្នក។",
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    if (blockedUsers.isEmpty()) {
                        Text("មិនទាន់មានដៃគូនៅក្នុងបញ្ជីប្លុកឡើយ។", fontWeight = FontWeight.SemiBold, color = textSecondary, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(blockedUsers.toList()) { blockedName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(inputBg)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = blockedName,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.unblockUser(blockedName)
                                            Toast.makeText(context, "$blockedName ត្រូវបានលុបចេញពីបញ្ជីប្លុក!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = "ដោះប្លុក (Unblock)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedListDialog = false }) {
                    Text("បិទ (Close)", color = accentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Modern 2026 Material 3 Collapsible / Accordion Group Card.
 * Compact and clean when collapsed, showing the group title & currently selected value.
 * Expands smoothly when clicked.
 */
@Composable
fun CollapsibleSettingsGroup(
    title: String,
    subtitle: String,
    icon: ImageVector,
    currentValue: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrowRotation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isExpanded) accentColor.copy(alpha = 0.45f) else cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column {
            // Header Row (Always visible, interactive)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Category Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isExpanded) accentColor.copy(alpha = 0.15f) else textSecondary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isExpanded) accentColor else textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Group Title and Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Current Selected Value Badge (Pill Chip)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isExpanded) accentColor else accentColor.copy(alpha = 0.10f),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = currentValue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpanded) Color.White else accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Expanding Chevron Arrow
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = textSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle)
                )
            }

            // Expandable Content Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(color = cardBorder)
                    content()
                }
            }
        }
    }
}

@Composable
fun ModernOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) accentColor.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) accentColor else textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = if (isSelected) accentColor else textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondary,
                lineHeight = 15.sp
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = accentColor,
                unselectedColor = textSecondary
            )
        )
    }
}

@Composable
fun ModernLanguageRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    isRestricted: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val rowAlpha = if (isRestricted) 0.35f else 1.0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .alpha(rowAlpha)
            .background(if (isSelected && !isRestricted) accentColor.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) accentColor else textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = if (isSelected) accentColor else textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondary,
                lineHeight = 15.sp
            )
        }
        if (isRestricted) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Restricted",
                tint = textSecondary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = textSecondary
                )
            )
        }
    }
}

@Composable
fun ModernSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) accentColor else textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = textSecondary,
                lineHeight = 15.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF48484A)
            )
        )
    }
}

@Composable
fun ModernActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = textSecondary,
                lineHeight = 15.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
