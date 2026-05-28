package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.AuthUiState
import com.example.presentation.viewmodel.AuthViewModel
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val isFormValid = email.isNotBlank() && password.length >= 6
    val isLoading = uiState is AuthUiState.Loading

    // Clear previous errors when visiting/navigating
    LaunchedEffect(Unit) {
        authViewModel.clearError()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("login_screen")
    ) {
        // Decorative rich layout background sphere
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryBlue.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Brand Logo design
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Budget Joy",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 0.5.sp,
                    fontSize = 32.sp
                )
            )

            val t = com.example.ui.localization.LocalAppStrings.current
            Text(
                text = t.onboardingTagline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp, start = 12.dp, end = 12.dp)
            )

            // Auth Error state Banner
            AnimatedVisibility(
                visible = uiState is AuthUiState.Error,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val errorMessage = (uiState as? AuthUiState.Error)?.message ?: ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .background(ColorExpense.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .border(1.dp, ColorExpense.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error icon",
                        tint = ColorExpense,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ColorExpense,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { authViewModel.clearError() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = t.close,
                            tint = ColorExpense,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Input Fields Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = t.loginTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.clearError()
                        },
                        label = { Text(t.email) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            authViewModel.clearError()
                        },
                        label = { Text(t.password) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Log In Action button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    authViewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("login_button"),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = DarkBackground,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.3f),
                    disabledContentColor = DarkBackground.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 3.dp,
                        color = DarkBackground
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t.loginButton,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp), tint = DarkBackground)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch option to register UI
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t.noAccount + " ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.testTag("go_to_register_button")
                ) {
                    Text(
                        text = t.registerButton,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val isFormValid = email.isNotBlank() && password.length >= 6 && password == confirmPassword
    val isLoading = uiState is AuthUiState.Loading

    // Clear previous errors when visiting/navigating
    LaunchedEffect(Unit) {
        authViewModel.clearError()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("register_screen")
    ) {
        // Decorative rich layout background sphere
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ColorSaving.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.1f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.2f, size.height * 0.1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Logo design
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorSaving),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AppRegistration,
                    contentDescription = null,
                    tint = DarkBackground,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val t = com.example.ui.localization.LocalAppStrings.current
            Text(
                text = t.registerTitle,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    fontSize = 28.sp
                )
            )

            Text(
                text = t.onboardingTagline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp, start = 12.dp, end = 12.dp)
            )

            // Auth Error state Banner
            AnimatedVisibility(
                visible = uiState is AuthUiState.Error,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val errorMessage = (uiState as? AuthUiState.Error)?.message ?: ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .background(ColorExpense.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .border(1.dp, ColorExpense.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error icon",
                        tint = ColorExpense,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ColorExpense,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { authViewModel.clearError() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = t.close,
                            tint = ColorExpense,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Input Fields Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = t.registerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.clearError()
                        },
                        label = { Text(t.email) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = TextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = ColorSaving,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input")
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            authViewModel.clearError()
                        },
                        label = { Text(t.password) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = ColorSaving,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    // Confirm Password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            authViewModel.clearError()
                        },
                        label = { Text(t.confirmPassword) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary)
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = ColorSaving,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirm_password_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Join/Register Action button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    authViewModel.register(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("register_button"),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorSaving,
                    contentColor = DarkBackground,
                    disabledContainerColor = ColorSaving.copy(alpha = 0.3f),
                    disabledContentColor = DarkBackground.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 3.dp,
                        color = DarkBackground
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t.registerButton,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = DarkBackground)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch option to log in UI
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t.hasAccount + " ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.testTag("go_to_login_button")
                ) {
                    Text(
                        text = t.loginButton,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ColorSaving,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
