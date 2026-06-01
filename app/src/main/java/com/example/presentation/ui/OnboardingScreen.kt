// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class IntroStep {
    CHOOSE_LANGUAGE,
    SWIPE_ONBOARDING
}

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String,
    val type: com.example.ui.localization.AppLanguageSupported
)

class OnboardingTranslation(
    val title: String,
    val description: String,
    val buttonText: String,
    val skipText: String,
    val loginText: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    onLanguageSelected: (com.example.ui.localization.AppLanguageSupported) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(IntroStep.CHOOSE_LANGUAGE) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    // Background beautiful gradient pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "BgPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgAlphaPulse"
    )

    val languages = listOf(
        LanguageOption("fr", "Français", "Français", "🇫🇷", com.example.ui.localization.AppLanguageSupported.FRANCAIS),
        LanguageOption("en", "English", "English", "🇬🇧", com.example.ui.localization.AppLanguageSupported.ENGLISH),
        LanguageOption("es", "Spanish", "Español", "🇪🇸", com.example.ui.localization.AppLanguageSupported.ESPANOL),
        LanguageOption("de", "German", "Deutsch", "🇩🇪", com.example.ui.localization.AppLanguageSupported.DEUTSCH),
        LanguageOption("it", "Italian", "Italiano", "🇮🇹", com.example.ui.localization.AppLanguageSupported.ITALIANO),
        LanguageOption("pt", "Portuguese", "Português", "🇵🇹", com.example.ui.localization.AppLanguageSupported.PORTUGUES),
        LanguageOption("ar", "Arabic", "العربية", "🇸🇦", com.example.ui.localization.AppLanguageSupported.ARABIC),
        LanguageOption("ru", "Russian", "Русский", "🇷🇺", com.example.ui.localization.AppLanguageSupported.RUSSIAN),
        LanguageOption("zh", "Chinese", "中文", "🇨🇳", com.example.ui.localization.AppLanguageSupported.CHINESE),
        LanguageOption("ja", "Japanese", "日本語", "🇯🇵", com.example.ui.localization.AppLanguageSupported.JAPANESE),
        LanguageOption("ko", "Korean", "한국어", "🇰🇷", com.example.ui.localization.AppLanguageSupported.KOREAN)
    )

    var selectedLangOption by remember {
        mutableStateOf(languages.find { it.type == currentLanguage } ?: languages[0])
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("onboarding_container")
    ) {
        // Aesthetic layered background decorations
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Top-left soft glowing sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryBlue.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = canvasWidth * 0.7f
                ),
                radius = canvasWidth * 0.7f,
                center = Offset(0f, 0f)
            )

            // Bottom-right soft glowing sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ColorSaving.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(canvasWidth, canvasHeight * 0.9f),
                    radius = canvasWidth * 0.6f
                ),
                radius = canvasWidth * 0.6f,
                center = Offset(canvasWidth, canvasHeight * 0.9f)
            )
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) + slideInHorizontally { it } togetherWith
                        fadeOut(animationSpec = tween(400)) + slideOutHorizontally { -it }
            },
            label = "IntroStepTransition"
        ) { currentStep ->
            when (currentStep) {
                IntroStep.CHOOSE_LANGUAGE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header Title
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.2f))
                                    .border(1.2.dp, PrimaryBlue, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Budget Joy",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sélectionne ta langue / Choose your language",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // List of languages grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 24.dp)
                                .testTag("language_option_grid")
                        ) {
                            items(languages) { option ->
                                val isSelected = selectedLangOption.type == option.type
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else DarkSurface
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PrimaryBlue else BorderColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .clickable {
                                            selectedLangOption = option
                                            onLanguageSelected(option.type)
                                        }
                                        .testTag("lang_${option.code}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = option.flag,
                                            fontSize = 24.sp
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = option.nativeName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = TextWhite,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            )
                                            Text(
                                                text = option.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextSecondary
                                                )
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Continue button
                        Button(
                            onClick = {
                                step = IntroStep.SWIPE_ONBOARDING
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("language_continue_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = DarkBackground
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedLangOption.type == com.example.ui.localization.AppLanguageSupported.FRANCAIS) "Continuer" else "Continue",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                IntroStep.SWIPE_ONBOARDING -> {
                    // Onboarding Content Container
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header Top Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Branded App Logo Look
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "J",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = DarkBackground,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Budget Joy",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            val tString = getOnboardingText(pagerState.currentPage, selectedLangOption.type)
                            TextButton(
                                onClick = onComplete,
                                modifier = Modifier.testTag("onboarding_skip")
                            ) {
                                Text(
                                    text = tString.skipText,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Horizontal pager for swipe-based navigation
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) { page ->
                            val tString = getOnboardingText(page, selectedLangOption.type)
                            when (page) {
                                0 -> OnboardingStepView(
                                    title = tString.title,
                                    description = tString.description,
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .background(ColorIncomeBg, RoundedCornerShape(32.dp))
                                                .padding(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = ColorIncome,
                                                modifier = Modifier.size(54.dp)
                                            )
                                        }
                                    }
                                )
                                1 -> OnboardingStepView(
                                    title = tString.title,
                                    description = tString.description,
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .background(ColorExpenseBg, RoundedCornerShape(32.dp))
                                                .padding(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PieChart,
                                                contentDescription = null,
                                                tint = ColorExpense,
                                                modifier = Modifier.size(54.dp)
                                            )
                                        }
                                    }
                                )
                                2 -> OnboardingStepView(
                                    title = tString.title,
                                    description = tString.description,
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(110.dp)
                                                .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                                                .padding(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(54.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        // Bottom controls: Indicators and Action Button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Pagination Dots Indicator
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { index ->
                                    val isSelected = index == pagerState.currentPage
                                    val dotWidth by animateDpAsState(
                                        targetValue = if (isSelected) 24.dp else 8.dp,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = "DotWidth"
                                    )
                                    val dotColor = if (isSelected) PrimaryBlue else BorderColor

                                    Box(
                                        modifier = Modifier
                                            .height(8.dp)
                                            .width(dotWidth)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                }
                            }

                            // Call To Action Button & Login Option
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isLastPage = pagerState.currentPage == 2
                                val tString = getOnboardingText(pagerState.currentPage, selectedLangOption.type)

                                Button(
                                    onClick = {
                                        if (!isLastPage) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        } else {
                                            onComplete()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("onboarding_next_button"),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = DarkBackground
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 4.dp
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = tString.buttonText,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        )
                                        if (!isLastPage) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Alternative login button option
                                TextButton(
                                    onClick = onComplete,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("onboarding_login_option")
                                ) {
                                    Text(
                                        text = tString.loginText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.2.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepView(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun getOnboardingText(
    page: Int,
    lang: com.example.ui.localization.AppLanguageSupported
): OnboardingTranslation {
    return when (lang) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Gère ton budget simplement",
                    description = "Suis tes dépenses, tes revenus et ton épargne en toute simplicité.",
                    buttonText = "Continuer",
                    skipText = "Passer",
                    loginText = "Se connecter"
                )
                1 -> OnboardingTranslation(
                    title = "Comprends tes finances",
                    description = "Analyse tes dépenses et améliore ta gestion au quotidien.",
                    buttonText = "Continuer",
                    skipText = "Passer",
                    loginText = "Se connecter"
                )
                else -> OnboardingTranslation(
                    title = "Ton coach financier intelligent",
                    description = "Reçois des conseils personnalisés pour mieux économiser.",
                    buttonText = "Commencer",
                    skipText = "Passer",
                    loginText = "Se connecter"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Manage your budget simply",
                    description = "Track your expenses, income, and savings with ease.",
                    buttonText = "Continue",
                    skipText = "Skip",
                    loginText = "Sign In"
                )
                1 -> OnboardingTranslation(
                    title = "Understand your finances",
                    description = "Analyze your expenses and improve your day-to-day management.",
                    buttonText = "Continue",
                    skipText = "Skip",
                    loginText = "Sign In"
                )
                else -> OnboardingTranslation(
                    title = "Your smart financial coach",
                    description = "Receive personalized advice to save more effectively.",
                    buttonText = "Get Started",
                    skipText = "Skip",
                    loginText = "Sign In"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Gestiona tu presupuesto fácilmente",
                    description = "Sigue tus gastos, ingresos y ahorros de forma sencilla.",
                    buttonText = "Continuar",
                    skipText = "Omitir",
                    loginText = "Iniciar sesión"
                )
                1 -> OnboardingTranslation(
                    title = "Entiende tus finanzas",
                    description = "Analiza tus gastos y mejora tu gestión diaria.",
                    buttonText = "Continuar",
                    skipText = "Omitir",
                    loginText = "Iniciar sesión"
                )
                else -> OnboardingTranslation(
                    title = "Tu asesor financiero inteligente",
                    description = "Recibe consejos personalizados para ahorrar más.",
                    buttonText = "Empezar",
                    skipText = "Omitir",
                    loginText = "Iniciar sesión"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Verwalte dein Budget einfach",
                    description = "Verfolge deine Ausgaben, Einnahmen und Ersparnisse mit Leichtigkeit.",
                    buttonText = "Weiter",
                    skipText = "Überspringen",
                    loginText = "Anmelden"
                )
                1 -> OnboardingTranslation(
                    title = "Verstehe deine Finanzen",
                    description = "Analysiere deine Ausgaben und verbessere dein tägliches Management.",
                    buttonText = "Weiter",
                    skipText = "Überspringen",
                    loginText = "Anmelden"
                )
                else -> OnboardingTranslation(
                    title = "Dein intelligenter Finanzcoach",
                    description = "Erhalte personalisierte Ratschläge, um klüger zu sparen.",
                    buttonText = "Loslegen",
                    skipText = "Überspringen",
                    loginText = "Anmelden"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Gestisci il tuo budget semplicemente",
                    description = "Traccia le tue spese, entrate e risparmi con facilità.",
                    buttonText = "Continua",
                    skipText = "Salta",
                    loginText = "Accedi"
                )
                1 -> OnboardingTranslation(
                    title = "Comprendi le tue finanze",
                    description = "Analizza le tue spese e migliora la tua gestione quotidiana.",
                    buttonText = "Continua",
                    skipText = "Salta",
                    loginText = "Accedi"
                )
                else -> OnboardingTranslation(
                    title = "Il tuo coach financier intelligente",
                    description = "Ricevi consigli personalizzati per risparmiare al meglio.",
                    buttonText = "Inizia",
                    skipText = "Salta",
                    loginText = "Accedi"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Gerencie seu orçamento facilmente",
                    description = "Acompanhe suas despesas, receitas e economias com simplicidade.",
                    buttonText = "Continuar",
                    skipText = "Pular",
                    loginText = "Entrar"
                )
                1 -> OnboardingTranslation(
                    title = "Entenda suas finanças",
                    description = "Analise suas despesas e melhore sua gestão no dia a dia.",
                    buttonText = "Continuar",
                    skipText = "Pular",
                    loginText = "Entrar"
                )
                else -> OnboardingTranslation(
                    title = "Seu treinador financeiro inteligente",
                    description = "Receba conselhos personalizados para poupar mais.",
                    buttonText = "Começar",
                    skipText = "Pular",
                    loginText = "Entrar"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.ARABIC -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "إدارة ميزانيتك ببساطة",
                    description = "تتبع نفقاتك وإيراداتك ومدخراتك بكل سهولة.",
                    buttonText = "استمرار",
                    skipText = "تخطي",
                    loginText = "تسجيل الدخول"
                )
                1 -> OnboardingTranslation(
                    title = "افهم شؤونك المالية",
                    description = "حلل نفقاتك وحسن إدارتك اليومية.",
                    buttonText = "استمرار",
                    skipText = "تخطي",
                    loginText = "تسجيل الدخول"
                )
                else -> OnboardingTranslation(
                    title = "مدربك المالي الذكي",
                    description = "احصل على نصائح مخصصة للادخار بشكل أفضل.",
                    buttonText = "ابدأ الآن",
                    skipText = "تخطي",
                    loginText = "تسجيل الدخول"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "Управляйте бюджетом легко",
                    description = "Следите за расходами, доходами и сбережениями в одно касание.",
                    buttonText = "Далее",
                    skipText = "Пропустить",
                    loginText = "Войти"
                )
                1 -> OnboardingTranslation(
                    title = "Поймите свои финансы",
                    description = "Анализируйте траты и улучшайте управление деньгами каждый день.",
                    buttonText = "Далее",
                    skipText = "Пропустить",
                    loginText = "Войти"
                )
                else -> OnboardingTranslation(
                    title = "Умный финансовый помощник",
                    description = "Получайте персональные советы для эффективных сбережений.",
                    buttonText = "Начать",
                    skipText = "Пропустить",
                    loginText = "Войти"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.CHINESE -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "轻松管理您的预算",
                    description = "轻松追踪支出、收入与储蓄，简单高效。",
                    buttonText = "继续",
                    skipText = "跳过",
                    loginText = "登录"
                )
                1 -> OnboardingTranslation(
                    title = "透视您的财务状况",
                    description = "分析日常开支，优化财务管理，提升效率。",
                    buttonText = "继续",
                    skipText = "跳过",
                    loginText = "登录"
                )
                else -> OnboardingTranslation(
                    title = "您的智能财务教练",
                    description = "获取定制化财务建议，科学省钱，合理储蓄。",
                    buttonText = "立即开始",
                    skipText = "跳过",
                    loginText = "登录"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "支出をシンプルに管理",
                    description = "支出、収入、貯蓄をいつでも簡単に確認・記録できます。",
                    buttonText = "次へ",
                    skipText = "スキップ",
                    loginText = "ログイン"
                )
                1 -> OnboardingTranslation(
                    title = "お金の流れを賢く理解",
                    description = "支出カテゴリーを分析し、最適な資金バランスを保ちます。",
                    buttonText = "次へ",
                    skipText = "スキップ",
                    loginText = "ログイン"
                )
                else -> OnboardingTranslation(
                    title = "AIパーソナルコーチ",
                    description = "一人ひとりに合わせた節約アドバイスをリアルタイムで提供します。",
                    buttonText = "使ってみる",
                    skipText = "スキップ",
                    loginText = "ログイン"
                )
            }
        }
        com.example.ui.localization.AppLanguageSupported.KOREAN -> {
            when (page) {
                0 -> OnboardingTranslation(
                    title = "더 손쉬운 일일 예산 관리",
                    description = "지출, 수입, 저축 추이를 직관적으로 기록하고 모니터링하세요.",
                    buttonText = "다음으로",
                    skipText = "건너뛰기",
                    loginText = "로그인"
                )
                1 -> OnboardingTranslation(
                    title = "한눈에 이해하는 금융 대시보드",
                    description = "수집된 지출 데이터를 분석하여 일상 속 개선점을 도출합니다.",
                    buttonText = "다음으로",
                    skipText = "건너뛰기",
                    loginText = "로그인"
                )
                else -> OnboardingTranslation(
                    title = "당신만을 위한 스마트 재테크 파트너",
                    description = "효과적인 자산 축적을 돕는 유용한 맞춤 조언을 받아보세요.",
                    buttonText = "시작하기",
                    skipText = "건너뛰기",
                    loginText = "로그인"
                )
            }
        }
    }
}
