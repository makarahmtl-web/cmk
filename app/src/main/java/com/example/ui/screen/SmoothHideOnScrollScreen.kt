package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

/**
 * Reusable and optimized Smooth Hide on Scroll Screen template.
 * Uses NestedScrollConnection with threshold buffering to ensure smooth, responsive transitions
 * for both TopAppBar and NavigationBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmoothHideOnScrollScreen() {
    // State សម្រាប់គ្រប់គ្រងការបង្ហាញ ឬលាក់ Top និង Bottom Bar
    var isVisible by remember { mutableStateOf(true) }

    // មុខងារចាប់សញ្ញាការអូសយ៉ាងរលូន (Optimized NestedScrollConnection)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -10f && isVisible) {
                    // អូសចុះក្រោម (Scroll Down) -> លាក់ Bar
                    isVisible = false
                } else if (delta > 10f && !isVisible) {
                    // អូសឡើងលើ (Scroll Up) -> បង្ហាញ Bar វិញ
                    isVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection), // ភ្ជាប់មុខងារអូសទីនេះ
        topBar = {
            // Top Bar ដែលមាន Animation លាក់/បង្ហាញឡើងយ៉ាងរលូន
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = { Text("Developer Demo") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        },
        bottomBar = {
            // Bottom Bar ដែលមាន Animation លាក់/បង្ហាញឡើងយ៉ាងរលូនដូចគ្នា
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Text("🏠") },
                        label = { Text("ទំព័រដើម") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Text("⚙️") },
                        label = { Text("ការកំណត់") }
                    )
                }
            }
        }
    ) { innerPadding ->
        // បញ្ជីទិន្នន័យសម្រាប់សាកល្បងអូស (LazyColumn)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(20) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(20.dp)) {
                        Text(text = "ប្រកាស ឬអត្ថបទទី ${index + 1}")
                    }
                }
            }
        }
    }
}
