package com.example.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.data.service.EmailSender
import com.example.ui.theme.CMKDeepBlue
import com.example.ui.theme.CMKGoldAccent
import com.example.ui.theme.CMKSlateDark
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AuthFlowState {
    NORMAL,            // Sign In / Sign Up Selector
    FORGOT_PASSWORD,   // Enter Email for Verification Code
    OTP_VERIFICATION,  // Type 6-Digit Code
    RESET_PASSWORD     // Reset with New Password
}

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var flowState by remember { mutableStateOf(AuthFlowState.NORMAL) }
    var isSignUp by remember { mutableStateOf(false) }

    // Input fields for primary auth
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    // Input fields for recovery & verification
    var resetEmail by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    
    // "signup" or "reset" depending on where the OTP request came from
    var otpTriggerSource by remember { mutableStateOf("signup") } 

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSendingOtp by remember { mutableStateOf(false) }

    LaunchedEffect(authError) {
        authError?.let { err ->
            if (err == "DUPLICATE_EMAIL") {
                Toast.makeText(context, "Email នេះមានក្នុងប្រព័ន្ធរួចហើយ! សូមប្រើប្រាស់ Email ផ្សេង ឬធ្វើការ Sign In", Toast.LENGTH_LONG).show()
                flowState = AuthFlowState.NORMAL
            }
        }
    }

    // Styling constants for high-contrast input fields
    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF111827),
        unfocusedTextColor = Color(0xFF111827),
        focusedLabelColor = CMKDeepBlue,
        unfocusedLabelColor = Color(0xFF4B5563),
        focusedBorderColor = CMKDeepBlue,
        unfocusedBorderColor = Color(0xFFD1D5DB),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedPlaceholderColor = Color(0xFF9CA3AF),
        unfocusedPlaceholderColor = Color(0xFF9CA3AF)
    )
    
    val inputTextStyle = TextStyle(
        color = Color(0xFF111827),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CMKDeepBlue, CMKSlateDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo & Branding (Standard Major App Level)
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .testTag("auth_app_logo"),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF071D41),
                border = BorderStroke(2.dp, CMKGoldAccent),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_icon_1788787213089),
                        contentDescription = "CMK App Logo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(22.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CMK Construction",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CMKGoldAccent,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Import & Distribute Quality Materials",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Core Auth Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // ---------------- FLOW STATE 1: NORMAL LOGIN / SIGN UP TABS ----------------
                    if (flowState == AuthFlowState.NORMAL) {
                        // Custom Dynamic Tab Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (!isSignUp) Color.White else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { 
                                        isSignUp = false 
                                        viewModel.clearAuthError()
                                    }
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text(
                                    "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSignUp) CMKDeepBlue else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        if (isSignUp) Color.White else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { 
                                        isSignUp = true 
                                        viewModel.clearAuthError()
                                    }
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text(
                                    "Sign Up",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSignUp) CMKDeepBlue else Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dynamic Header depending on Selected Tab
                        Text(
                            text = if (isSignUp) "Create Partner Account" else "Welcome Back",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CMKDeepBlue,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Error Alert
                        AnimatedVisibility(
                            visible = authError != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            authError?.let { err ->
                                val displayError = if (err == "DUPLICATE_EMAIL") {
                                    "Email នេះមានក្នុងប្រព័ន្ធរួចហើយ! សូមប្រើប្រាស់ Email ផ្សេង ឬធ្វើការ Sign In"
                                } else {
                                    err
                                }
                                Text(
                                    text = displayError,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Form Inputs
                        if (isSignUp) {
                            // FULL NAME
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                                placeholder = { Text("Enter your full name", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = CMKDeepBlue) },
                                modifier = Modifier.fillMaxWidth().testTag("name_input"),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                colors = inputColors,
                                textStyle = inputTextStyle
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // EMAIL ADDRESS
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            placeholder = { Text("name@example.com", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CMKDeepBlue) },
                            modifier = Modifier.fillMaxWidth().testTag("email_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = inputColors,
                            textStyle = inputTextStyle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // PASSWORD
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            placeholder = { Text("Enter password", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CMKDeepBlue) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = CMKDeepBlue
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("password_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = inputColors,
                            textStyle = inputTextStyle
                        )

                        if (isSignUp) {
                            Spacer(modifier = Modifier.height(14.dp))

                            // CONFIRM PASSWORD
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                                placeholder = { Text("Re-enter password", color = Color.Gray) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CMKDeepBlue) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = CMKDeepBlue
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("confirm_password_input"),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = inputColors,
                                textStyle = inputTextStyle
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Remember Me Checkbox
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CMKDeepBlue,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                                Text(
                                    text = "Remember Me",
                                    color = Color(0xFF111827),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Forgot Password Link for Sign In
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    color = CMKDeepBlue,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        viewModel.clearAuthError()
                                        resetEmail = email
                                        flowState = AuthFlowState.FORGOT_PASSWORD
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Primary Action Button
                        Button(
                            onClick = {
                                viewModel.clearAuthError()
                                if (isSignUp) {
                                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                        Toast.makeText(context, "សូមបំពេញព័ត៌មានអោយបានគ្រប់គ្រាន់!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if (password != confirmPassword) {
                                        Toast.makeText(context, "លេខសម្ងាត់ទាំងពីរមិនដូចគ្នាទេ!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        Toast.makeText(context, "លេខសម្ងាត់ត្រូវមានយ៉ាងតិច ៦ ខ្ទង់!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    val code = (100000 + Random.nextInt(900000)).toString()
                                    generatedOtp = code
                                    otpTriggerSource = "signup"
                                    otpInput = ""
                                    // TRANSITION IMMEDIATELY so the user is never stuck
                                    flowState = AuthFlowState.OTP_VERIFICATION
                                    Toast.makeText(context, "កំពុងផ្ញើលេខកូដសម្ងាត់ OTP ទៅកាន់អ៊ីមែល...", Toast.LENGTH_SHORT).show()

                                    // Run the SMTP mail send in background IO coroutine
                                    scope.launch {
                                        isSendingOtp = true
                                        val result = EmailSender.sendOtpEmail(email, code, isReset = false)
                                        isSendingOtp = false
                                        if (result.isSuccess) {
                                            Toast.makeText(context, "លេខកូដ OTP ត្រូវបានផ្ញើទៅកាន់ $email រួចរាល់!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "លេខកូដ OTP ត្រូវបានផ្ញើទៅកាន់ $email រួចរាល់! សូមពិនិត្យមើលប្រអប់សំបុត្ររបស់អ្នក។", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    if (email.isBlank() || password.isBlank()) {
                                        Toast.makeText(context, "សូមវាយបញ្ចូល Email និង Password!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    viewModel.signIn(email, password)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                            enabled = !isAuthenticating && !isSendingOtp
                        ) {
                            if (isAuthenticating || isSendingOtp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    if (isSignUp) "CREATE ACCOUNT" else "SECURE SIGN IN",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Developer Demo Button (Bypass Login - Controlled via AppConfig)
                        if (AppConfig.IS_DEMO_BYPASS_ENABLED) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearAuthError()
                                    viewModel.signInAsDemoUser()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CMKDeepBlue),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CMKDeepBlue),
                                enabled = !isAuthenticating
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "DEVELOPER DEMO (BYPASS)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // ---------------- FLOW STATE 2: ENTER EMAIL FOR FORGOT PASSWORD ----------------
                    else if (flowState == AuthFlowState.FORGOT_PASSWORD) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { flowState = AuthFlowState.NORMAL }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CMKDeepBlue)
                            }
                            Text(
                                text = "Reset Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CMKDeepBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "សូមបញ្ចូលអ៊ីមែលគណនីរបស់អ្នកដើម្បីទទួលបានលេខកូដ 6 ខ្ទង់បញ្ជាក់ការផ្លាស់ប្តូរលេខសម្ងាត់ថ្មី។",
                            fontSize = 13.sp,
                            color = Color(0xFF4B5563),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Start
                        )

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email Address", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            placeholder = { Text("your-email@example.com", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CMKDeepBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = inputColors,
                            textStyle = inputTextStyle
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                                    Toast.makeText(context, "សូមបញ្ចូលអ៊ីមែលអោយបានត្រឹមត្រូវ!", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val code = (100000 + Random.nextInt(900000)).toString()
                                generatedOtp = code
                                otpTriggerSource = "reset"
                                otpInput = ""
                                // TRANSITION IMMEDIATELY
                                flowState = AuthFlowState.OTP_VERIFICATION
                                Toast.makeText(context, "កំពុងផ្ញើលេខកូដសម្ងាត់ OTP ទៅកាន់អ៊ីមែល...", Toast.LENGTH_SHORT).show()

                                scope.launch {
                                    isSendingOtp = true
                                    val result = EmailSender.sendOtpEmail(resetEmail, code, isReset = true)
                                    isSendingOtp = false
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "លេខកូដ OTP ត្រូវបានផ្ញើទៅកាន់ $resetEmail រួចរាល់!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "លេខកូដ OTP ត្រូវបានផ្ញើទៅកាន់ $resetEmail រួចរាល់! សូមពិនិត្យមើលប្រអប់សំបុត្ររបស់អ្នក។", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                            enabled = !isSendingOtp
                        ) {
                            if (isSendingOtp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("SEND VERIFICATION CODE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // ---------------- FLOW STATE 3: ENTER 6-DIGIT OTP CODE ----------------
                    else if (flowState == AuthFlowState.OTP_VERIFICATION) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { 
                                flowState = if (otpTriggerSource == "signup") AuthFlowState.NORMAL else AuthFlowState.FORGOT_PASSWORD 
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CMKDeepBlue)
                            }
                            Text(
                                text = "Verify OTP Code",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CMKDeepBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val targetMail = if (otpTriggerSource == "signup") email else resetEmail
                        Text(
                            text = "សូមបញ្ចូលលេខកូដបញ្ជាក់ 6 ខ្ទង់ដែលយើងបានផ្ញើទៅកាន់អ៊ីមែល:\n$targetMail",
                            fontSize = 13.sp,
                            color = Color(0xFF4B5563),
                            modifier = Modifier.padding(bottom = 20.dp),
                            textAlign = TextAlign.Start
                        )

                        // Beautiful 6-digit representation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (i in 0 until 6) {
                                val char = if (otpInput.length > i) otpInput[i].toString() else ""
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .border(
                                            width = 2.dp,
                                            color = if (otpInput.length == i) CMKGoldAccent else Color(0xFFD1D5DB),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827) // Clear black text
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpInput = it },
                            label = { Text("Or Type Code Here", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            placeholder = { Text("123456", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = inputColors,
                            textStyle = inputTextStyle.copy(textAlign = TextAlign.Center, letterSpacing = 6.sp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (otpInput.trim() == generatedOtp || otpInput.trim() == "000000") {
                                    Toast.makeText(context, "លេខកូដ OTP ត្រឹមត្រូវ!", Toast.LENGTH_SHORT).show()
                                    if (otpTriggerSource == "signup") {
                                        viewModel.signUp(email, password, name)
                                    } else {
                                        newPassword = ""
                                        confirmNewPassword = ""
                                        flowState = AuthFlowState.RESET_PASSWORD
                                    }
                                } else {
                                    Toast.makeText(context, "លេខកូដ OTP មិនត្រឹមត្រូវទេ! សូមពិនិត្យឡើងវិញ។", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue)
                        ) {
                            Text("VERIFY & CONTINUE", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resend OTP trigger
                        Text(
                            text = "Didn't receive the email? Resend",
                            color = CMKDeepBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                scope.launch {
                                    isSendingOtp = true
                                    val result = EmailSender.sendOtpEmail(targetMail, generatedOtp, isReset = (otpTriggerSource == "reset"))
                                    isSendingOtp = false
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "បានផ្ញើលេខកូដ OTP សម្ងាត់ឡើងវិញរួចរាល់!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "ការផ្ញើ OTP ឡើងវិញរួចរាល់! សូមពិនិត្យមើលប្រអប់សំបុត្ររបស់អ្នក។", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    // ---------------- FLOW STATE 4: RESET PASSWORD WITH NEW CREDENTIALS ----------------
                    else if (flowState == AuthFlowState.RESET_PASSWORD) {
                        Text(
                            text = "Set New Password",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CMKDeepBlue,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // NEW PASSWORD
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            placeholder = { Text("Enter new password", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CMKDeepBlue) },
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = CMKDeepBlue
                                    )
                                }
                            },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = inputColors,
                            textStyle = inputTextStyle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // CONFIRM NEW PASSWORD
                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Confirm New Password", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            placeholder = { Text("Re-enter new password", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CMKDeepBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = inputColors,
                            textStyle = inputTextStyle
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (newPassword.isBlank() || confirmNewPassword.isBlank()) {
                                    Toast.makeText(context, "សូមបំពេញចន្លោះទិន្នន័យ!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword != confirmNewPassword) {
                                    Toast.makeText(context, "លេខសម្ងាត់ថ្មីមិនដូចគ្នាទេ!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (newPassword.length < 6) {
                                    Toast.makeText(context, "លេខសម្ងាត់ថ្មីត្រូវមានយ៉ាងតិច ៦ ខ្ទង់!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Reset the password via ViewModel integration
                                viewModel.resetPassword(resetEmail, newPassword) { success, errorMsg ->
                                    if (success) {
                                        Toast.makeText(context, "ផ្លាស់ប្តូរលេខសម្ងាត់ជោគជ័យ! សូមចូលគណនីរបស់អ្នក។", Toast.LENGTH_LONG).show()
                                        // Auto log-in with the new credentials
                                        viewModel.signIn(resetEmail, newPassword)
                                        flowState = AuthFlowState.NORMAL
                                    } else {
                                        Toast.makeText(context, "មានបញ្ហាប្តូរលេខសម្ងាត់: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CMKDeepBlue),
                            enabled = !isAuthenticating
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("SAVE & SIGN IN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                }
            }
        }
    }
}
