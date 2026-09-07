package com.example.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.model.MaterialCategory
import com.example.data.model.MaterialItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

// Comprehensive cambodian provinces & districts mapping
val cambodiaProvinces = listOf(
    "រាជធានីភ្នំពេញ" to listOf("ខណ្ឌដូនពេញ", "ខណ្ឌចំការមន", "ខណ្ឌ៧មករា", "ខណ្ឌទួលគោក", "ខណ្ឌមានជ័យ", "ខណ្ឌឫស្សីកែវ", "ខណ្ឌដង្កោ", "ខណ្ឌសែនសុខ", "ខណ្ឌពោធិ៍សែនជ័យ", "ខណ្ឌជ្រោយចង្វារ", "ខណ្ឌព្រែកព្នៅ", "ខណ្ឌច្បារអំពៅ", "ខណ្ឌបឹងកេងកង", "ខណ្ឌកំបូល"),
    "ខេត្តបន្ទាយមានជ័យ" to listOf("ក្រុងសិរីសោភ័ណ", "ក្រុងប៉ោយប៉ែត", "ស្រុកមង្គលបុរី", "ស្រុកភ្នំស្រុក", "ស្រុកព្រះនេត្រព្រះ", "ស្រុកអូរជ្រៅ", "ស្រុកថ្មពួក", "ស្រុកស្វាយចេក", "ស្រុកម៉ាឡៃ"),
    "ខេត្តបាត់ដំបង" to listOf("ក្រុងបាត់ដំបង", "ស្រុកបាណន់", "ស្រុកថ្មគោល", "ស្រុកបវេល", "ស្រុកឯកភ្នំ", "ស្រុកមោងឫស្សី", "ស្រុករតនមណ្ឌល", "ស្រុកសង្កែ", "ស្រុកសំឡូត", "ស្រុកសំពៅលូន", "ស្រុកភ្នំព្រឹក", "ស្រុកកំរៀង", "ស្រុកគាស់ក្រឡា", "ស្រុករុក្ខគិរី"),
    "ខេត្តកំពង់ចាម" to listOf("ក្រុងកំពង់ចាម", "ស្រុកកំពង់សៀម", "ស្រុកបាធាយ", "ស្រុកចំការលើ", "ស្រុកជើងព្រៃ", "ស្រុកកងមាស", "ស្រុកកោះស៊ូទិន", "ស្រុកព្រៃឈរ", "ស្រុកស្រីសន្ធរ", "ស្រុកស្ទឹងត្រង់"),
    "ខេត្តកំពង់ឆ្នាំង" to listOf("ក្រុងកំពង់ឆ្នាំង", "ស្រុកបរិបូណ៌", "ស្រុកជលគិរី", "ស្រុកកំពង់លែង", "ស្រុកកំពង់ត្រឡាច", "ស្រុករលាប្អៀរ", "ស្រុកសាមគ្គីមានជ័យ", "ស្រុកទឹកផុស"),
    "ខេត្តកំពង់ស្ពឺ" to listOf("ក្រុងច្បារមន", "ក្រុងឧដុង្គម៉ែជ័យ", "ស្រុកបសេដ្ឋ", "ស្រុកគងពិសី", "ស្រុកភ្នំស្រួច", "ស្រុកសំរោងទង", "ស្រុកថ្ពង", "ស្រុកឱរ៉ាល់", "ស្រុកសាមគ្គីមុនីជ័យ"),
    "ខេត្តកំពង់ធំ" to listOf("ក្រុងស្ទឹងសែន", "ស្រុកបារាយណ៍", "ស្រុកកំពង់ស្វាយ", "ស្រុកស្ទោង", "ស្រុកប្រាសាទសំបូរ", "ស្រុកប្រាសាទបល្ល័ង្ក", "ស្រុកសណ្តាន់", "ស្រុកសន្ទុក", "ស្រុកតាំងគោក"),
    "ខេត្តកំពត" to listOf("ក្រុងកំពត", "ក្រុងបូកគោ", "ស្រុកអង្គរជ័យ", "ស្រុកបន្ទាយមាស", "ស្រុកឈូក", "ស្រុកជុំគិរី", "ស្រុកដងទង់", "ស្រុកកំពង់ត្រាច", "ស្រុកទឹកឈូ"),
    "ខេត្តកណ្តាល" to listOf("ក្រុងតាខ្មៅ", "ក្រុងអរិយក្សត្រ", "ស្រុកកណ្តាលស្ទឹង", "ស្រុកកៀនស្វាយ", "ស្រុកខ្សាច់កណ្តាល", "ស្រុកកោះធំ", "ស្រុកលើកដែក", "ស្រុកល្វាឯម", "ស្រុកមុខកំពូល", "ស្រុកអង្គស្នួល", "ស្រុកពញាឮ", "ស្រុកស្អាង"),
    "ខេត្តកោះកុង" to listOf("ក្រុងខេមរភូមិន្ទ", "ស្រុកបទុមសាគរ", "ស្រុកគិរីសាគរ", "ស្រុកកោះកុង", "ស្រុកមណ្ឌលសីមា", "ស្រុកស្រែអំបិល", "ស្រុកថ្មបាំង"),
    "ខេត្តក្រចេះ" to listOf("ក្រុងក្រចេះ", "ក្រុងចល្លោន", "ស្រុកឆ្លូង", "ស្រុកព្រែកប្រសប់", "ស្រុកសំបូរ", "ស្រុកស្នួល", "ស្រុកចិត្របុរី"),
    "ខេត្តមណ្ឌលគិរី" to listOf("ក្រុងសែនមនោរម្យ", "ស្រុកកែវសីមា", "ស្រុកកោះញែក", "ស្រុកអូររាំង", "ស្រុកពេជ្រាដា"),
    "ខេត្តឧត្តរមានជ័យ" to listOf("ក្រុងសំរោង", "ស្រុកអន្លង់វែង", "ស្រុកបន្ទាយអំពិល", "ស្រុកចុងកាល់", "ស្រុកត្រពាំងប្រាសាទ"),
    "ខេត្តពោធិ៍សាត់" to listOf("ក្រុងពោធិ៍សាត់", "ស្រុកបាកាន", "ស្រុកកណ្តៀង", "ស្រុកក្រគរ", "ស្រុកភ្នំក្រវាញ", "ស្រុកវាលវែង", "ស្រុកតាលោសែនជ័យ"),
    "ខេត្តព្រះវិហារ" to listOf("ក្រុងព្រះវិហារ", "ស្រុកជ័យសែន", "ស្រុកឆែប", "ស្រុកជាំក្សាន្ត", "ស្រុកគូលែន", "ស្រុករវៀង", "ស្រុកសង្គមថ្មី", "ស្រុកត្បែងមានជ័យ"),
    "ខេត្តព្រៃវែង" to listOf("ក្រុងព្រៃវែង", "ស្រុកបាភ្នំ", "ស្រុកកំចាយមារ", "ស្រុកកំពង់ត្របែក", "ស្រុកកញ្ជ្រៀច", "ស្រុកមេសាំង", "ស្រុកពាមជរ", "ស្រុកពាមរ៍", "ស្រុកពារាំង", "ស្រុកព្រះស្តេច", "ស្រុកពោធិ៍រៀង", "ស្រុកស៊ីធរកណ្តាល", "ស្រុកស្វាយអន្ទរ"),
    "ខេត្តរតនគិរី" to listOf("ក្រុងបាលុង", "ស្រុកអណ្តូងមាស", "ស្រុកបរកែវ", "ស្រុកកូនមុំ", "ស្រុកលំផាត់", "ស្រុកអូរជុំ", "ស្រុកអូរយ៉ាដាវ", "ស្រុកតាវែង", "ស្រុកវើនសៃ"),
    "ខេត្តសៀមរាប" to listOf("ក្រុងសៀមរាប", "ក្រុងរុនតាឯកតេជោសែន", "ស្រុកអង្គរជុំ", "ស្រុកអង្គរធំ", "ស្រុកបន្ទាយស្រី", "ស្រុកជីក្រែង", "ស្រុកក្រឡាញ់", "ស្រុកពួក", "ស្រុកប្រាសាទបាគង", "ស្រុកសូទ្រនិគម", "ស្រុកស្រីស្នំ", "ស្រុកស្វាយលើ", "ស្រុកវ៉ារិន"),
    "ខេត្តព្រះសីហនុ" to listOf("ក្រុងព្រះសីហនុ", "ក្រុងកោះរ៉ុង", "ស្រុកព្រៃនប់", "ស្រុកស្ទឹងហាវ", "ស្រុកកំពង់សីលា"),
    "ខេត្តស្ទឹងត្រែង" to listOf("ក្រុងស្ទឹងត្រែង", "ស្រុកសេសាន", "ស្រុកសៀមបូក", "ស្រុកសៀមប៉ាង", "ស្រុកថាឡាបរិវ៉ាត់"),
    "ខេត្តស្វាយរៀង" to listOf("ក្រុងស្វាយរៀង", "ក្រុងបាវិត", "ស្រុកចន្រ្ទា", "ស្រុកកំពង់រោទ៍", "ស្រុករំដួល", "ស្រុករមាសហែក", "ស្រុកស្វាយជ្រំ", "ស្រុកស្វាយទាប"),
    "ខេត្តតាកែវ" to listOf("ក្រុងដូនកែវ", "ស្រុកអង្គរបុរី", "ស្រុកបាទី", "ស្រុកបូរីជលសារ", "ស្រុកគិរីវង់", "ស្រុកកោះអណ្តែត", "ស្រុកព្រៃកប្បាស", "ស្រុកសំរោង", "ស្រុកត្រាំកក់", "ស្រុកទ្រាំង"),
    "ខេត្តត្បូងឃ្មុំ" to listOf("ក្រុងសួង", "ស្រុកដំបែ", "ស្រុកក្រូចឆ្មារ", "ស្រុកមេមត់", "ស្រុកអូររាំងឪ", "ស្រុកពញាក្រែក", "ស្រុកត្បូងឃ្មុំ"),
    "ខេត្តកែប" to listOf("ក្រុងកែប", "ស្រុកដំណាក់ចង្អើរ"),
    "ខេត្តប៉ៃលិន" to listOf("ក្រុងប៉ៃលិន", "ស្រុកសាលាក្រៅ")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(viewModel: MainViewModel) {
    val items by viewModel.filteredCatalogue.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val context = LocalContext.current

    val isMenuVisible by viewModel.isFeedScrollMenuVisible.collectAsState()

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

    // Dialog & BottomSheet triggers
    var showCategoryBottomSheet by remember { mutableStateOf(false) }
    var showLocationPickerDialog by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var activeDetailItem by remember { mutableStateOf<MaterialItem?>(null) }

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
                        Column {
                            Text("CMK Store", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                            Text("Import & Wholesale Materials", fontSize = 10.sp, color = CMKGoldAccent)
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { showLocationPickerDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("top_bar_location_dropdown"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = "Location",
                                tint = CMKGoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedProvince == "គ្រប់ខេត្ត/ក្រុង") "All Cambodia" else selectedProvince,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 110.dp)
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.SETTINGS) },
                            modifier = Modifier.testTag("btn_store_top_menu")
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
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // FAB for posting materials
                FloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    containerColor = CMKGoldAccent,
                    contentColor = CMKDeepBlue,
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp).testTag("fab_create_material_post")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Material Post", modifier = Modifier.size(24.dp))
                }

                // FAB for categories Bottom Sheet
                FloatingActionButton(
                    onClick = { showCategoryBottomSheet = true },
                    containerColor = CMKDeepBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(50.dp).testTag("fab_categories_sheet")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Categories Filter", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Categories", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        val targetTopPadding = if (isMenuVisible) innerPadding.calculateTopPadding() else 0.dp
        val animatedTopPadding by androidx.compose.animation.core.animateDpAsState(
            targetValue = targetTopPadding,
            animationSpec = tween(durationMillis = 600),
            label = "storeTopPadding"
        )

        Column(
            modifier = Modifier
                .padding(
                    top = animatedTopPadding,
                    bottom = innerPadding.calculateBottomPadding()
                )
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            // Slimmed compact search bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search rebar, bricks, tiling materials...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("store_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }

            // Quick Category row chips showing currently selected one
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChipCompact(
                    name = "All Materials (ទំនិញទាំងអស់)",
                    isSelected = selectedCategory == null,
                    onClick = { viewModel.selectCategory(null) }
                )
                MaterialCategory.values().forEach { category ->
                    CategoryChipCompact(
                        name = "${category.displayNameEn} (${category.displayNameKh})",
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Products Listing Grid / List
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No materials match search or location", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text("Try clearing filters or changing area.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items) { material ->
                        MaterialItemCard(
                            item = material,
                            onClick = { activeDetailItem = material },
                            onCallClick = {
                                viewModel.startCall(isVideo = true, partnerName = "Socheata (CMK Sales)")
                            },
                            onChatInquireClick = {
                                viewModel.sendChatMessage("Hello, I am interested in inquiring about: '${material.name}' currently deployed in ${material.location}. Please provide wholesale specifications.")
                                viewModel.navigateTo(AppScreen.CHAT)
                            }
                        )
                    }
                }
            }
        }

        // --- POPUP: CAMBODIA LOCATION PICKER DIALOG ---
        if (showLocationPickerDialog) {
            var tempProvince by remember { mutableStateOf(selectedProvince) }
            var tempDistrict by remember { mutableStateOf(selectedDistrict) }
            var showingDistricts by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showLocationPickerDialog = false },
                title = { 
                    Text(
                        if (showingDistricts) "Select District (ស្រុក/ខណ្ឌ)" else "Select Province (ខេត្ត/រាជធានី)",
                        fontWeight = FontWeight.Bold,
                        color = CMKDeepBlue,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                        if (!showingDistricts) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                // Dynamic trigger for selecting All Cambodia
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateLocation("គ្រប់ខេត្ត/ក្រុង", "គ្រប់ស្រុក/ខណ្ឌ")
                                                showLocationPickerDialog = false
                                                Toast.makeText(context, "Showing all locations", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("គ្រប់ខេត្ត/ក្រុង (All Cambodia)", fontWeight = FontWeight.ExtraBold, color = CMKDeepBlue)
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }

                                items(cambodiaProvinces) { (provinceName, districts) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                tempProvince = provinceName
                                                tempDistrict = districts.first()
                                                showingDistricts = true
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(provinceName, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                }
                            }
                        } else {
                            val activeDistricts = cambodiaProvinces.firstOrNull { it.first == tempProvince }?.second ?: emptyList()
                            Column {
                                TextButton(
                                    onClick = { showingDistricts = false },
                                    modifier = Modifier.align(Alignment.Start)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Back to Provinces", fontSize = 12.sp)
                                }
                                LazyColumn(modifier = Modifier.fillMaxSize().weight(1.0f)) {
                                    // Option for selecting all districts in this province
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.updateLocation(tempProvince, "គ្រប់ស្រុក/ខណ្ឌ")
                                                    showLocationPickerDialog = false
                                                    Toast.makeText(context, "Location set to: $tempProvince (All)", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.NavigateNext, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("គ្រប់ស្រុក/ខណ្ឌ (All Districts)", fontWeight = FontWeight.Bold, color = CMKDeepBlue)
                                        }
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                    }

                                    items(activeDistricts) { districtName ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    tempDistrict = districtName
                                                    viewModel.updateLocation(tempProvince, tempDistrict)
                                                    showLocationPickerDialog = false
                                                    Toast.makeText(context, "Location set to: $tempProvince, $tempDistrict", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.NavigateNext, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(districtName, color = Color(0xFF1E293B))
                                        }
                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLocationPickerDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // --- BOTTOM SHEET: CATEGORY SELECTION FILTER ---
        if (showCategoryBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategoryBottomSheet = false },
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = "Construction Materials Categories",
                        fontWeight = FontWeight.Bold,
                        color = CMKDeepBlue,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                        text = "ជ្រើសរើសប្រភេទការ៉ូ ក្បឿង គ្រឿងដែក គ្រឿងអគ្គិសនី ឬថ្នាំលាប ដើម្បីលម្អិត",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // All Materials list item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCategory(null)
                                showCategoryBottomSheet = false
                                Toast.makeText(context, "Filtered: All Materials", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CMKDeepBlue.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ViewModule, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("All Materials", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
                            Text("ទំនិញទាំងអស់", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (selectedCategory == null) {
                            Spacer(modifier = Modifier.weight(1.0f))
                            Icon(Icons.Default.Check, contentDescription = "Active", tint = SafeGreen)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // List out categories dynamically
                    MaterialCategory.values().forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectCategory(category)
                                    showCategoryBottomSheet = false
                                    Toast.makeText(context, "Filtered: ${category.displayNameEn}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val catIcon = when(category) {
                                MaterialCategory.BRICKS_TILES -> Icons.Default.GridOn
                                MaterialCategory.BATHROOM_PLUMBING -> Icons.Default.Layers
                                MaterialCategory.SAFETY_EQUIPMENT -> Icons.Default.Shield
                                MaterialCategory.PAINTS_COATINGS -> Icons.Default.FormatPaint
                                MaterialCategory.HARDWARE_ELECTRICAL -> Icons.Default.Handyman
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CMKGoldAccent.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(catIcon, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(category.displayNameEn, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
                                Text(category.displayNameKh, fontSize = 11.sp, color = Color.Gray)
                            }
                            if (selectedCategory == category) {
                                Spacer(modifier = Modifier.weight(1.0f))
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = SafeGreen)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        // --- FULL SCREEN POPUP: CREATE PRODUCT MATERIAL POST ---
        if (showCreatePostDialog) {
            var matName by remember { mutableStateOf("") }
            var matPrice by remember { mutableStateOf("") }
            var matDetails by remember { mutableStateOf("") }
            var matAvailability by remember { mutableStateOf("In Stock") }
            var matCategory by remember { mutableStateOf(MaterialCategory.BRICKS_TILES) }
            var selectedImagesUris by remember { mutableStateOf<List<String>>(emptyList()) }
            var isCompilingPost by remember { mutableStateOf(false) }

            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    if (selectedImagesUris.size >= 5) {
                        Toast.makeText(context, "You can select up to 5 images only.", Toast.LENGTH_LONG).show()
                    } else {
                        selectedImagesUris = selectedImagesUris + uri.toString()
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showCreatePostDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Scaffold(
                        containerColor = Color.White,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Post Construction Material", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CMKDeepBlue)
                                        Text("បង្ហោះលក់ទំនិញគ្រឿងសំណង់", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC5A059))
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { showCreatePostDialog = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF0F172A))
                                    }
                                },
                                actions = {
                                    Button(
                                        onClick = {
                                            if (matName.isBlank()) {
                                                Toast.makeText(context, "សូមបញ្ចូលឈ្មោះទំនិញ (Material Name)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (matPrice.isBlank()) {
                                                Toast.makeText(context, "សូមបញ្ចូលតម្លៃ (Wholesale Price)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (matDetails.isBlank()) {
                                                Toast.makeText(context, "សូមបញ្ចូលព័ត៌មានលម្អិតទំនិញ (Specifications & Details)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            isCompilingPost = true
                                            viewModel.createMaterialPost(
                                                name = matName,
                                                category = matCategory,
                                                imageUrls = selectedImagesUris.ifEmpty { listOf("https://images.unsplash.com/photo-1590069261209-f8e9b8642343?auto=format&fit=crop&q=80&w=400") },
                                                specDetails = matDetails,
                                                bulkPrice = matPrice,
                                                availability = matAvailability,
                                                location = if (selectedProvince == "គ្រប់ខេត្ត/ក្រុង") "រាជធានីភ្នំពេញ • ខណ្ឌដូនពេញ" else "$selectedProvince • $selectedDistrict"
                                            )
                                            isCompilingPost = false
                                            showCreatePostDialog = false
                                            Toast.makeText(context, "🎉 ទំនិញត្រូវបានបង្ហោះជាសាធារណៈដោយជោគជ័យ! Posted successfully.", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CMKDeepBlue,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                        modifier = Modifier.height(34.dp).padding(end = 12.dp)
                                    ) {
                                        Text("POST", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                            )
                        },
                        bottomBar = {
                            Surface(
                                shadowElevation = 12.dp,
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (matName.isBlank()) {
                                                Toast.makeText(context, "សូមបញ្ចូលឈ្មោះទំនិញ (Material Name)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (matPrice.isBlank()) {
                                                Toast.makeText(context, "សូមបញ្ចូលតម្លៃ (Wholesale Price)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            if (matDetails.isBlank()) {
                                                Toast.makeText(context, "សូមបញ្ចូលព័ត៌មានលម្អិតទំនិញ (Specifications & Details)", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            isCompilingPost = true
                                            viewModel.createMaterialPost(
                                                name = matName,
                                                category = matCategory,
                                                imageUrls = selectedImagesUris.ifEmpty { listOf("https://images.unsplash.com/photo-1590069261209-f8e9b8642343?auto=format&fit=crop&q=80&w=400") },
                                                specDetails = matDetails,
                                                bulkPrice = matPrice,
                                                availability = matAvailability,
                                                location = if (selectedProvince == "គ្រប់ខេត្ត/ក្រុង") "រាជធានីភ្នំពេញ • ខណ្ឌដូនពេញ" else "$selectedProvince • $selectedDistrict"
                                            )
                                            isCompilingPost = false
                                            showCreatePostDialog = false
                                            Toast.makeText(context, "🎉 ទំនិញត្រូវបានបង្ហោះជាសាធារណៈដោយជោគជ័យ! Posted successfully.", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CMKDeepBlue,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, tint = CMKGoldAccent, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "ផុសចេញជាសាធារណៈ (Publish Post Publicly)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerMatPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .padding(innerMatPadding)
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text("Product Details & Descriptions (ព័ត៌មានទំនិញ)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                            }

                            item {
                                OutlinedTextField(
                                    value = matName,
                                    onValueChange = { matName = it },
                                    label = { Text("Material Name (ឈ្មោះទំនិញ)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                    placeholder = { Text("e.g. Clay Red Bricks", color = Color(0xFF64748B)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedBorderColor = CMKDeepBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = CMKDeepBlue,
                                        unfocusedLabelColor = Color(0xFF475569)
                                    )
                                )
                            }

                            item {
                                // Category Dropdown
                                var showCatDropdown by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = "${matCategory.displayNameEn} (${matCategory.displayNameKh})",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Category (ប្រភេទ)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showCatDropdown = true },
                                        trailingIcon = {
                                            IconButton(onClick = { showCatDropdown = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF0F172A))
                                            }
                                        },
                                        enabled = false,
                                        textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = Color(0xFF0F172A),
                                            disabledBorderColor = Color(0xFFCBD5E1),
                                            disabledLabelColor = Color(0xFF475569),
                                            disabledContainerColor = Color(0xFFF8FAFC),
                                            disabledTrailingIconColor = Color(0xFF0F172A)
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = showCatDropdown,
                                        onDismissRequest = { showCatDropdown = false },
                                        modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                                    ) {
                                        MaterialCategory.values().forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text("${cat.displayNameEn} (${cat.displayNameKh})", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                                                onClick = {
                                                    matCategory = cat
                                                    showCatDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = matPrice,
                                    onValueChange = { matPrice = it },
                                    label = { Text("Wholesale Price (តម្លៃបោះដុំ)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                    placeholder = { Text("e.g. $0.38 / Piece or $4.50 / Bag", color = Color(0xFF64748B)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedBorderColor = CMKDeepBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = CMKDeepBlue,
                                        unfocusedLabelColor = Color(0xFF475569)
                                    )
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = matDetails,
                                    onValueChange = { matDetails = it },
                                    label = { Text("Specifications & Details (ព័ត៌មានលម្អិតទំនិញ)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                    placeholder = { Text("e.g. Quality standard, dimensions, compressive strength", color = Color(0xFF64748B)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF8FAFC),
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedBorderColor = CMKDeepBlue,
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = CMKDeepBlue,
                                        unfocusedLabelColor = Color(0xFF475569)
                                    )
                                )
                            }

                            item {
                                // Availability Selection
                                Text("Availability Status (ស្ថានភាពទំនិញ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("In Stock", "Limited", "Pre-Order").forEach { status ->
                                        val isSel = matAvailability == status
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(
                                                    width = if (isSel) 2.dp else 1.dp,
                                                    color = if (isSel) CMKDeepBlue else Color(0xFFCBD5E1),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .background(
                                                    color = if (isSel) CMKDeepBlue else Color(0xFFF8FAFC),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { matAvailability = status }
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                status,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSel) Color.White else Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text("Attached Images (${selectedImagesUris.size}/5)", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                                Text("Images are automatically compressed for fast loading.", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                            }

                            item {
                                // Image thumbnails container
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    selectedImagesUris.forEachIndexed { idx, uri ->
                                        Box(
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                        ) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    "1.4MB • OK",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .padding(vertical = 2.dp)
                                                        .align(Alignment.Center)
                                                )
                                            }
                                            IconButton(
                                                onClick = { selectedImagesUris = selectedImagesUris.filterIndexed { i, _ -> i != idx } },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    if (selectedImagesUris.size < 5) {
                                        Box(
                                            modifier = Modifier
                                                .size(84.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .clickable {
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(22.dp))
                                                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CMKDeepBlue, modifier = Modifier.padding(top = 4.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Selected Location (ទីតាំង)", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                            Text("$selectedProvince • $selectedDistrict", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                        }
                                    }
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Detail View Sheet Dialog
        activeDetailItem?.let { material ->
            MaterialDetailDialog(
                item = material,
                onDismiss = { activeDetailItem = null },
                onCall = {
                    viewModel.startCall(isVideo = true, partnerName = "Socheata (CMK Sales)")
                    activeDetailItem = null
                },
                onChat = {
                    viewModel.sendChatMessage("Hello, I am interested in inquiring about: '${material.name}' with spec details: '${material.specDetails}'. Please let me know bulk delivery options.")
                    viewModel.navigateTo(AppScreen.CHAT)
                    activeDetailItem = null
                }
            )
        }
    }
}

@Composable
fun CategoryChipCompact(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CMKDeepBlue else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else Slate600,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MaterialItemCard(
    item: MaterialItem,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onChatInquireClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("material_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Availability Badge
                val badgeColor = when (item.availability) {
                    "In Stock" -> SafeGreen
                    "Limited" -> Color(0xFFD97706)
                    else -> Slate400
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        item.availability,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Location badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            item.location,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.specDetails,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BULK DEALER PRICE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(item.bulkPrice, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CMKDeepBlue)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onChatInquireClick,
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Inquire via Chat", tint = CMKDeepBlue, modifier = Modifier.size(16.dp))
                        }

                        Button(
                            onClick = onCallClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CMKGoldAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call Rep", color = CMKDeepBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailDialog(
    item: MaterialItem,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onChat: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Material Specifications", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CMKDeepBlue, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(item.category.displayNameEn, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(item.availability, color = SafeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1.0f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = CMKDeepBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(item.location, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(item.name, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(16.dp))

                Text("PRODUCT OVERVIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
                Text(
                    item.specDetails,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("LOGISTICS INFO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, letterSpacing = 1.sp)
                Text(
                    item.deliveryInfo,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Map Preview Card
                val context = LocalContext.current
                Card(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(item.location)}"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(item.location)}"))
                            context.startActivity(browserIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("https://images.unsplash.com/photo-1526778548025-fa2f459cd5c1?auto=format&fit=crop&q=80&w=600")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Map Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.85f
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(CMKDeepBlue.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "បើកផែនទី (Open Map): ${item.location}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Price Card & Action buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("WHOLESALE DEALS PRICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                            Text(item.bulkPrice, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = CMKDeepBlue)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onChat,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Inquire", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onCall,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CMKGoldAccent),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = CMKDeepBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Sales", color = CMKDeepBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl, // បញ្ចូល Link https://res.cloudinary.com/... ពី Firestore
        contentDescription = "Product Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentScale = ContentScale.Crop
    )
}
