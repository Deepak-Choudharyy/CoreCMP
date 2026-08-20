package com.corecmp.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlinx.datetime.todayIn

enum class CustomDatePickerMode {
    SINGLE, RANGE
}

@Composable
fun CustomSingleDatePicker(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate?,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    minDaysFromToday: Int? = null,
    maxDaysFromToday: Int? = null,
    isDateSelectable: (LocalDate) -> Boolean = { true },
    onDateSelected: (LocalDate) -> Unit,
    primaryColor: Color = Color(0xFF655CDC),
    dayTextColor: Color = Color.Black,
    selectedDayTextColor: Color = Color.White,
    disabledDayTextColor: Color = Color.LightGray,
    headerTextColor: Color = Color.Black,
    navigationIconColor: Color = Color.DarkGray,
    weekdayHeaderTextColor: Color = Color.Gray
) {
    CustomDatePicker(
        modifier = modifier,
        mode = CustomDatePickerMode.SINGLE,
        selectedDate = selectedDate,
        minDate = minDate,
        maxDate = maxDate,
        minDaysFromToday = minDaysFromToday,
        maxDaysFromToday = maxDaysFromToday,
        isDateSelectable = isDateSelectable,
        onDateSelected = onDateSelected,
        primaryColor = primaryColor,
        dayTextColor = dayTextColor,
        selectedDayTextColor = selectedDayTextColor,
        disabledDayTextColor = disabledDayTextColor,
        headerTextColor = headerTextColor,
        navigationIconColor = navigationIconColor,
        weekdayHeaderTextColor = weekdayHeaderTextColor
    )
}

@Composable
fun CustomRangeDatePicker(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    minDaysFromToday: Int? = null,
    maxDaysFromToday: Int? = null,
    isDateSelectable: (LocalDate) -> Boolean = { true },
    onRangeSelected: (LocalDate?, LocalDate?) -> Unit,
    primaryColor: Color = Color(0xFF655CDC),
    rangeBackgroundColor: Color = Color(0xFF655CDC).copy(alpha = 0.15f),
    dayTextColor: Color = Color.Black,
    selectedDayTextColor: Color = Color.White,
    rangeDayTextColor: Color = Color(0xFF655CDC),
    disabledDayTextColor: Color = Color.LightGray,
    headerTextColor: Color = Color.Black,
    navigationIconColor: Color = Color.DarkGray,
    weekdayHeaderTextColor: Color = Color.Gray
) {
    CustomDatePicker(
        modifier = modifier,
        mode = CustomDatePickerMode.RANGE,
        selectedStartDate = selectedStartDate,
        selectedEndDate = selectedEndDate,
        minDate = minDate,
        maxDate = maxDate,
        minDaysFromToday = minDaysFromToday,
        maxDaysFromToday = maxDaysFromToday,
        isDateSelectable = isDateSelectable,
        onRangeSelected = onRangeSelected,
        primaryColor = primaryColor,
        rangeBackgroundColor = rangeBackgroundColor,
        dayTextColor = dayTextColor,
        selectedDayTextColor = selectedDayTextColor,
        rangeDayTextColor = rangeDayTextColor,
        disabledDayTextColor = disabledDayTextColor,
        headerTextColor = headerTextColor,
        navigationIconColor = navigationIconColor,
        weekdayHeaderTextColor = weekdayHeaderTextColor
    )
}

@Composable
fun CustomDatePicker(
    modifier: Modifier = Modifier,
    mode: CustomDatePickerMode = CustomDatePickerMode.SINGLE,
    selectedDate: LocalDate? = null,
    selectedStartDate: LocalDate? = null,
    selectedEndDate: LocalDate? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    minDaysFromToday: Int? = null,
    maxDaysFromToday: Int? = null,
    isDateSelectable: (LocalDate) -> Boolean = { true },
    onDateSelected: (LocalDate) -> Unit = {},
    onRangeSelected: (LocalDate?, LocalDate?) -> Unit = { _, _ -> },
    primaryColor: Color = Color(0xFF655CDC),
    rangeBackgroundColor: Color = Color(0xFF655CDC).copy(alpha = 0.15f),
    dayTextColor: Color = Color.Black,
    selectedDayTextColor: Color = Color.White,
    rangeDayTextColor: Color = Color(0xFF655CDC),
    disabledDayTextColor: Color = Color.LightGray,
    headerTextColor: Color = Color.Black,
    navigationIconColor: Color = Color.DarkGray,
    weekdayHeaderTextColor: Color = Color.Gray
) {
    // Current viewed month and year in the calendar
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var currentMonth by remember { mutableStateOf(today.month) }
    var currentYear by remember { mutableStateOf(today.year) }

    val computedMinDate = minDate ?: minDaysFromToday?.let { today.plus(DatePeriod(days = it)) }
    val computedMaxDate = maxDate ?: maxDaysFromToday?.let { today.plus(DatePeriod(days = it)) }

    // Update month view when selectedDate or selectedStartDate changes
    LaunchedEffect(selectedDate, selectedStartDate) {
        if (mode == CustomDatePickerMode.SINGLE && selectedDate != null) {
            currentMonth = selectedDate.month
            currentYear = selectedDate.year
        } else if (mode == CustomDatePickerMode.RANGE && selectedStartDate != null) {
            currentMonth = selectedStartDate.month
            currentYear = selectedStartDate.year
        }
    }

    val daysInMonth = getDaysInMonth(currentYear, currentMonth)
    val firstDayOfMonth = LocalDate(currentYear, currentMonth, 1)
    
    // Determine the empty slots before the first day (Sunday = 0, Monday = 1 ... Saturday = 6)
    val shift = when (firstDayOfMonth.dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        // Month & Year Header Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentMonth == Month.JANUARY) {
                        currentMonth = Month.DECEMBER
                        currentYear -= 1
                    } else {
                        currentMonth = Month(currentMonth.number - 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = navigationIconColor
                )
            }

            Text(
                text = "${currentMonth.name.lowercase().replaceFirstChar { it.uppercase() }} $currentYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = headerTextColor
            )

            IconButton(
                onClick = {
                    if (currentMonth == Month.DECEMBER) {
                        currentMonth = Month.JANUARY
                        currentYear += 1
                    } else {
                        currentMonth = Month(currentMonth.number + 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = navigationIconColor
                )
            }
        }

        // Weekday Headers
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = weekdayHeaderTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Days Grid representation using Rows and Columns
        val totalSlots = shift + daysInMonth
        val rows = (totalSlots + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 7) {
                        val slotIndex = row * 7 + col
                        val dayNumber = slotIndex - shift + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = LocalDate(currentYear, currentMonth, dayNumber)
                            val isToday = date == today
                            
                            // Check date limitations
                            val isEnabled = (computedMinDate == null || date >= computedMinDate) && 
                                            (computedMaxDate == null || date <= computedMaxDate) &&
                                            isDateSelectable(date)

                            // Check selections
                            val isSelected = when (mode) {
                                CustomDatePickerMode.SINGLE -> date == selectedDate
                                CustomDatePickerMode.RANGE -> date == selectedStartDate || date == selectedEndDate
                            }

                            val isInRange = mode == CustomDatePickerMode.RANGE &&
                                    selectedStartDate != null &&
                                    selectedEndDate != null &&
                                    date > selectedStartDate &&
                                    date < selectedEndDate

                            // Selection joining background
                            val rangeJoinModifier = when {
                                isInRange -> Modifier.background(rangeBackgroundColor)
                                mode == CustomDatePickerMode.RANGE && selectedStartDate != null && selectedEndDate != null -> {
                                    when (date) {
                                        selectedStartDate -> Modifier.background(
                                            rangeBackgroundColor,
                                            shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                                        )
                                        selectedEndDate -> Modifier.background(
                                            rangeBackgroundColor,
                                            shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                                        )
                                        else -> Modifier
                                    }
                                }
                                else -> Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .then(rangeJoinModifier)
                                    .clickable(enabled = isEnabled) {
                                        if (mode == CustomDatePickerMode.SINGLE) {
                                            onDateSelected(date)
                                        } else {
                                            if (selectedStartDate == null || selectedEndDate != null) {
                                                onRangeSelected(date, null)
                                            } else if (date < selectedStartDate) {
                                                onRangeSelected(date, null)
                                            } else {
                                                onRangeSelected(selectedStartDate, date)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val circleModifier = if (isSelected) {
                                    Modifier
                                        .size(40.dp)
                                        .background(primaryColor, shape = CircleShape)
                                } else {
                                    Modifier.size(40.dp)
                                }

                                Box(
                                    modifier = circleModifier,
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = when {
                                                !isEnabled -> disabledDayTextColor
                                                isSelected -> selectedDayTextColor
                                                isInRange -> rangeDayTextColor
                                                else -> dayTextColor
                                            }
                                        )
                                        if (isToday && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(primaryColor, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Empty slot placeholder
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun getDaysInMonth(year: Int, month: Month): Int {
    return when (month) {
        Month.JANUARY -> 31
        Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
        Month.MARCH -> 31
        Month.APRIL -> 30
        Month.MAY -> 31
        Month.JUNE -> 30
        Month.JULY -> 31
        Month.AUGUST -> 31
        Month.SEPTEMBER -> 30
        Month.OCTOBER -> 31
        Month.NOVEMBER -> 30
        Month.DECEMBER -> 31
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
