package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryBlue

@Composable
fun MainScreen(
    viewModel: BudgetViewModel,
    authViewModel: com.example.presentation.viewmodel.AuthViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var isRegistering by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val currencySelected by viewModel.currencySelected.collectAsState()

    if (!onboardingCompleted) {
        OnboardingScreen(
            currentLanguage = currentLanguage,
            onLanguageSelected = { viewModel.selectLanguage(it) },
            onComplete = { viewModel.completeOnboarding() }
        )
    } else if (currentUser == null) {
        AnimatedContent(
            targetState = isRegistering,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "AuthScreenSwitch"
        ) { registering ->
            if (registering) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { isRegistering = false }
                )
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { isRegistering = true }
                )
            }
        }
    } else if (!currencySelected) {
        CurrencySelectionScreen(
            currentLanguage = currentLanguage,
            onCurrencySelected = { viewModel.selectCurrency(it) }
        )
    } else {
        val t = com.example.ui.localization.LocalAppStrings.current
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = DarkBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = PrimaryBlue,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = t.home
                            )
                        },
                        label = { Text(t.home) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = t.add
                            )
                        },
                        label = { Text(t.add) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_add")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = t.history
                            )
                        },
                        label = { Text(t.history) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_history")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = t.analysis
                            )
                        },
                        label = { Text(t.analysis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_analysis")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = t.profile
                            )
                        },
                        label = { Text(t.profile) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardTab(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToAdd = { selectedTab = 1 },
                            onNavigateToProfile = { selectedTab = 4 },
                            currentUser = currentUser
                        )
                        1 -> AddTransactionTab(
                            viewModel = viewModel,
                            onSuccess = {
                                // After successful adding, transition to dashboard view
                                selectedTab = 0
                            }
                        )
                        2 -> HistoryTab(
                            viewModel = viewModel
                        )
                        3 -> AnalysisTab(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        4 -> ProfileTab(
                            viewModel = viewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}
