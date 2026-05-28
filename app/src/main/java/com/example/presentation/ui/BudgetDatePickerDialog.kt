package com.example.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class DatePickerMode {
    YEAR, MONTH, DAY
}

@Composable
fun BudgetDatePickerDialog(
    initialDateTimestamp: Long,
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    language: com.example.ui.localization.AppLanguageSupported,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    
    // Parse initial timestamp safely
    val initialCalendar = remember(initialDateTimestamp) {
        Calendar.getInstance().apply {
            timeInMillis = initialDateTimestamp
        }
    }
    
    var tempYear by remember { mutableStateOf(initialCalendar.get(Calendar.YEAR)) }
    var tempMonth by remember { mutableStateOf(initialCalendar.get(Calendar.MONTH)) }
    var tempDay by remember { mutableStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }
    
    var currentMode by remember { mutableStateOf(DatePickerMode.YEAR) }
    
    // Safety check for day boundaries when month/year changes
    val maxDaysInMonth = remember(tempYear, tempMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, tempYear)
            set(Calendar.MONTH, tempMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (tempDay > maxDaysInMonth) {
        tempDay = maxDaysInMonth
    }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, BorderColor),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DIALOG TITLE
                Text(
                    text = when (language) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Datum auswählen"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Seleccionar fecha"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Seleziona data"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Selecionar data"
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Select Date"
                        com.example.ui.localization.AppLanguageSupported.CHINESE -> "选择日期"
                        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "日付を選択"
                        com.example.ui.localization.AppLanguageSupported.KOREAN -> "날짜 선택"
                        com.example.ui.localization.AppLanguageSupported.ARABIC -> "تحديد التاريخ"
                        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Выберите дату"
                        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Choisir une date"
                        else -> "Select Date"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // HEADER SEGMENTS: YEAR | MONTH | DAY
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val monthsList = remember(language) { getLocalizedMonths(language) }
                    val monthAbbrev = monthsList.getOrNull(tempMonth) ?: ""
                    
                    // Year Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentMode == DatePickerMode.YEAR) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { currentMode = DatePickerMode.YEAR }
                            .padding(vertical = 10.dp)
                            .testTag("picker_mode_year"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tempYear.toString(),
                            color = if (currentMode == DatePickerMode.YEAR) PrimaryBlue else TextPrimary,
                            fontWeight = if (currentMode == DatePickerMode.YEAR) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Month Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentMode == DatePickerMode.MONTH) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { currentMode = DatePickerMode.MONTH }
                            .padding(vertical = 10.dp)
                            .testTag("picker_mode_month"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthAbbrev,
                            color = if (currentMode == DatePickerMode.MONTH) PrimaryBlue else TextPrimary,
                            fontWeight = if (currentMode == DatePickerMode.MONTH) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    
                    // Day Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentMode == DatePickerMode.DAY) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { currentMode = DatePickerMode.DAY }
                            .padding(vertical = 10.dp)
                            .testTag("picker_mode_day"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tempDay.toString(),
                            color = if (currentMode == DatePickerMode.DAY) PrimaryBlue else TextPrimary,
                            fontWeight = if (currentMode == DatePickerMode.DAY) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // SELECTOR PANES
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentMode) {
                        DatePickerMode.YEAR -> {
                            YearSelectorPane(
                                selectedYear = tempYear,
                                onYearSelected = {
                                    tempYear = it
                                    // Step 2: Auto switch to Month
                                    currentMode = DatePickerMode.MONTH
                                }
                            )
                        }
                        DatePickerMode.MONTH -> {
                            MonthSelectorPane(
                                tempMonth = tempMonth,
                                language = language,
                                onMonthSelected = {
                                    tempMonth = it
                                    // Step 3: Auto switch to Day
                                    currentMode = DatePickerMode.DAY
                                }
                            )
                        }
                        DatePickerMode.DAY -> {
                            DaySelectorPane(
                                tempYear = tempYear,
                                tempMonth = tempMonth,
                                tempDay = tempDay,
                                language = language,
                                onDaySelected = { tempDay = it }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ACTION BUTTONS: CANCEL / VALIDATE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = t.datePickerCancel,
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resultCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, tempYear)
                                set(Calendar.MONTH, tempMonth)
                                set(Calendar.DAY_OF_MONTH, tempDay)
                                set(Calendar.HOUR_OF_DAY, 12)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDateSelected(resultCal.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = t.datePickerConfirm,
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YearSelectorPane(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    // Generate years dynamically +/- 8 years from current year
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val years = remember { (currentYear - 8..currentYear + 8).toList() }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(years) { year ->
            val isSelected = year == selectedYear
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryBlue else DarkBackground)
                    .clickable { onYearSelected(year) }
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlue else BorderColor
                        ),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = year.toString(),
                    color = if (isSelected) DarkBackground else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MonthSelectorPane(
    tempMonth: Int,
    language: com.example.ui.localization.AppLanguageSupported,
    onMonthSelected: (Int) -> Unit
) {
    val months = remember(language) { getLocalizedMonths(language) }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((0..11).toList()) { monthIndex ->
            val isSelected = monthIndex == tempMonth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryBlue else DarkBackground)
                    .clickable { onMonthSelected(monthIndex) }
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlue else BorderColor
                        ),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = months.getOrNull(monthIndex) ?: "",
                    color = if (isSelected) DarkBackground else TextPrimary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DaySelectorPane(
    tempYear: Int,
    tempMonth: Int,
    tempDay: Int,
    language: com.example.ui.localization.AppLanguageSupported,
    onDaySelected: (Int) -> Unit
) {
    val startOnSunday = remember(language) {
        when (language) {
            com.example.ui.localization.AppLanguageSupported.ENGLISH,
            com.example.ui.localization.AppLanguageSupported.ARABIC,
            com.example.ui.localization.AppLanguageSupported.CHINESE,
            com.example.ui.localization.AppLanguageSupported.JAPANESE,
            com.example.ui.localization.AppLanguageSupported.KOREAN -> true
            else -> false
        }
    }
    
    val dayLabels = remember(startOnSunday) {
        if (startOnSunday) {
            listOf("S", "M", "T", "W", "T", "F", "S")
        } else {
            listOf("L", "M", "M", "J", "V", "S", "D")
        }
    }
    
    val (firstDayOffset, maxDays) = remember(tempYear, tempMonth, startOnSunday) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, tempYear)
            set(Calendar.MONTH, tempMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val offset = if (startOnSunday) {
            when (firstDayOfWeek) {
                Calendar.SUNDAY -> 0
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 0
            }
        } else {
            when (firstDayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
        }
        Pair(offset, maxDaysInMonth)
    }
    
    // Construct the Grid of Day Cells: offsets + actual days
    val gridCellsCount = firstDayOffset + maxDays
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Week Days labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(gridCellsCount) { index ->
                if (index < firstDayOffset) {
                    // Blank space for preceding days
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val dayNum = index - firstDayOffset + 1
                    val isSelected = dayNum == tempDay
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryBlue else DarkBackground)
                            .clickable { onDaySelected(dayNum) }
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryBlue else BorderColor
                                ),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNum.toString(),
                            color = if (isSelected) DarkBackground else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

fun getLocalizedMonths(language: com.example.ui.localization.AppLanguageSupported): List<String> {
    val locale = when (language) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> Locale.FRANCE
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> Locale.US
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> Locale("es", "ES")
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> Locale.GERMANY
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> Locale.ITALY
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> Locale("pt", "PT")
        com.example.ui.localization.AppLanguageSupported.CHINESE -> Locale.CHINA
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> Locale.JAPAN
        com.example.ui.localization.AppLanguageSupported.ARABIC -> Locale("ar", "SA")
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> Locale("ru", "RU")
        com.example.ui.localization.AppLanguageSupported.KOREAN -> Locale.KOREA
    }
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMMM", locale)
    return (0..11).map { monthIndex ->
        cal.set(Calendar.MONTH, monthIndex)
        val raw = sdf.format(cal.time)
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }
}
