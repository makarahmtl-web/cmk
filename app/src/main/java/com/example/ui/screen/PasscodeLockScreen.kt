package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CMKDeepBlue
import com.example.ui.theme.CMKGoldAccent
import com.example.ui.theme.Rose500
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PasscodeLockScreen(
    viewModel: MainViewModel,
    isSettingNew: Boolean = false,
    onSetupComplete: () -> Unit = {}
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var firstAttemptPin by remember { mutableStateOf<String?>(null) }

    val hasPasscode by viewModel.hasPasscode.collectAsState()

    val screenTitle = when {
        isSettingNew -> {
            if (firstAttemptPin == null) "Set Up App Passcode" else "Confirm App Passcode"
        }
        else -> "App Locked"
    }

    val screenSubtitle = when {
        isSettingNew -> {
            if (firstAttemptPin == null) "Create a 4-digit security PIN to protect your app" else "Re-enter your 4-digit security PIN"
        }
        else -> "Enter your 4-digit security PIN to access CMK Materials"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CMKDeepBlue)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Security Icon with Pulsing Effect
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(CMKGoldAccent.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (enteredPin.length == 4 && errorMessage == null) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                tint = CMKGoldAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Titles
        Text(
            text = screenTitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = screenSubtitle,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bullet indicator dots (4 positions)
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..4) {
                val isActive = enteredPin.length >= i
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = if (isActive) CMKGoldAccent else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error message text space
        Box(modifier = Modifier.height(24.dp)) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Rose500,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))

        // Keypad Layout (3x4 grid)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key.isNotEmpty()) {
                                if (key == "DEL") {
                                    IconButton(
                                        onClick = {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    // Numeric Tactile Circular Key
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .clickable {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                    errorMessage = null
                                                    
                                                    // When 4 digits are completed, process PIN verification
                                                    if (enteredPin.length == 4) {
                                                        if (isSettingNew) {
                                                            if (firstAttemptPin == null) {
                                                                // Store first attempt and ask for confirmation
                                                                firstAttemptPin = enteredPin
                                                                enteredPin = ""
                                                            } else {
                                                                // Verify confirmation matches first PIN
                                                                if (enteredPin == firstAttemptPin) {
                                                                    viewModel.setPasscode(enteredPin)
                                                                    onSetupComplete()
                                                                } else {
                                                                    errorMessage = "PINs do not match. Start over."
                                                                    firstAttemptPin = null
                                                                    enteredPin = ""
                                                                }
                                                            }
                                                        } else {
                                                            val success = viewModel.verifyPasscode(enteredPin)
                                                            if (!success) {
                                                                errorMessage = "Incorrect Passcode. Try again."
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            .testTag("keypad_btn_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
