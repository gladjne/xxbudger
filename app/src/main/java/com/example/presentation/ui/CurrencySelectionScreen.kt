// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
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

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String,
    val localizedNames: Map<com.example.ui.localization.AppLanguageSupported, String>
)

class CurrencyTranslation(
    val title: String,
    val description: String,
    val confirmButtonText: String
)

@Composable
fun CurrencySelectionScreen(
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Beautiful layered layout animations
    val infiniteTransition = rememberInfiniteTransition(label = "CurrencyBgPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgPulseAlpha"
    )

    val currencies = listOf(
        CurrencyOption("EUR", "€", "Euro", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Euro",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "Euro"
        )),
        CurrencyOption("USD", "$", "US Dollar", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Dollar américain",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "US Dollar"
        )),
        CurrencyOption("GBP", "£", "British Pound", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Livre sterling",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "British Pound"
        )),
        CurrencyOption("JPY", "¥", "Japanese Yen", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Yen japonais",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "Japanese Yen"
        )),
        CurrencyOption("INR", "₹", "Indian Rupee", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Roupie indienne",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "Indian Rupee"
        )),
        CurrencyOption("CFA", "CFA", "CFA Franc", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Franc CFA",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "CFA Franc"
        )),
        CurrencyOption("ZAR", "R", "South African Rand", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Rand sud-africain",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "South African Rand"
        )),
        CurrencyOption("CAD", "C$", "Canadian Dollar", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Dollar canadien",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "Canadian Dollar"
        )),
        CurrencyOption("AUD", "A$", "Australian Dollar", mapOf(
            com.example.ui.localization.AppLanguageSupported.FRANCAIS to "Dollar australien",
            com.example.ui.localization.AppLanguageSupported.ENGLISH to "Australian Dollar"
        ))
    )

    var selectedCurrency by remember { mutableStateOf(currencies[0]) }
    val translation = getCurrencyText(currentLanguage)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("currency_selection_screen")
    ) {
        // Decorative background canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ColorSaving.copy(alpha = pulseAlpha), Color.Transparent),
                    center = Offset(canvasWidth * 0.5f, canvasHeight * 0.1f),
                    radius = canvasWidth * 0.8f
                ),
                radius = canvasWidth * 0.8f,
                center = Offset(canvasWidth * 0.5f, canvasHeight * 0.1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSaving.copy(alpha = 0.2f))
                        .border(1.2.dp, ColorSaving, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = ColorSaving,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = translation.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontSize = 26.sp,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translation.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Grid choice
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 24.dp)
            ) {
                items(currencies) { option ->
                    val isSelected = selectedCurrency.code == option.code
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ColorSaving.copy(alpha = 0.12f) else DarkSurface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ColorSaving else BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clickable {
                                selectedCurrency = option
                            }
                            .testTag("currency_${option.code}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.symbol,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = if (isSelected) ColorSaving else TextWhite,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ColorSaving,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val dispName = option.localizedNames[currentLanguage] ?: option.name
                            Text(
                                text = dispName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) TextWhite else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Confirm and proceed button
            Button(
                onClick = {
                    onCurrencySelected(selectedCurrency.symbol)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("currency_confirm_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorSaving,
                    contentColor = DarkBackground
                )
            ) {
                Text(
                    text = translation.confirmButtonText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

fun getCurrencyText(lang: com.example.ui.localization.AppLanguageSupported): CurrencyTranslation {
    return when (lang) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> CurrencyTranslation(
            title = "Sélectionne ta devise",
            description = "Choisis la devise principale de tes comptes. Cela servira pour tous tes rapports et graphiques d'analyse.",
            confirmButtonText = "Confirmer et commencer"
        )
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> CurrencyTranslation(
            title = "Choose your currency",
            description = "Select the primary currency for your accounts. This will be used for all your analysis reports and charts.",
            confirmButtonText = "Confirm and start"
        )
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> CurrencyTranslation(
            title = "Elige tu moneda",
            description = "Selecciona la moneda principal para tus cuentas. Se utilizará para todos tus informes de análisis y gráficos.",
            confirmButtonText = "Confirmar y empezar"
        )
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> CurrencyTranslation(
            title = "Wähle deine Währung",
            description = "Wähle die Hauptwährung für deine Konten. Diese wird für alle deine Analyseberichte und Diagramme verwendet.",
            confirmButtonText = "Bestätigen und loslegen"
        )
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> CurrencyTranslation(
            title = "Scegli la tua valuta",
            description = "Seleziona la valuta principale per i tuoi conti. Verrà utilizzata per tutti i rapporti e grafici di analisi.",
            confirmButtonText = "Conferma e inizia"
        )
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> CurrencyTranslation(
            title = "Escolha sua moeda",
            description = "Selecione a moeda principal para suas contas. Ela será usada em todos os seus relatórios de análise e gráficos.",
            confirmButtonText = "Confirmar et começar"
        )
        com.example.ui.localization.AppLanguageSupported.ARABIC -> CurrencyTranslation(
            title = "اختر عملتك",
            description = "حدد العملة الأساسية لحساباتك. سيتم استخدامها في جميع تقارير التحليل والرسوم البيانية الخاصة بك.",
            confirmButtonText = "تأكيد والبدء"
        )
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> CurrencyTranslation(
            title = "Выберите валюту",
            description = "Выберите основную валюту для ваших счетов. Она будет использоваться во всех аналитических отчетах и графиках.",
            confirmButtonText = "Подтвердить и начать"
        )
        com.example.ui.localization.AppLanguageSupported.CHINESE -> CurrencyTranslation(
            title = "选择您的货币",
            description = "选择您账户的主要货币。这将用于您的所有分析报告与图表。",
            confirmButtonText = "确认并开始"
        )
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> CurrencyTranslation(
            title = "通貨を選択してください",
            description = "アカウントの主な通貨を選択します。これはすべての分析レポートやグラフで使用されます。",
            confirmButtonText = "確認して開始する"
        )
        com.example.ui.localization.AppLanguageSupported.KOREAN -> CurrencyTranslation(
            title = "통화 선택",
            description = "계정의 기본 통화를 선택해 주세요. 모든 분석 보고서와 차트에 사용됩니다.",
            confirmButtonText = "확인하고 시작하기"
        )
    }
}
