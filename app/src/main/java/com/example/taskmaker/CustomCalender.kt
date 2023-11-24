package com.example.taskmaker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale



@Preview
@Composable
fun CustomCalendarPreview() {
    CustomCalendar()
}

@Composable
fun CustomCalendar() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column {
        MonthNavigation(currentMonth) { direction ->
            currentMonth = currentMonth.plusMonths(direction.toLong())
        }

        Crossfade(targetState = currentMonth, animationSpec = tween(durationMillis = 500),
            label = ""
        ) { month ->
            CalendarView(month)
        }
    }
}


@Composable
fun MonthNavigation(currentMonth: YearMonth, onMonthChange: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedButton(
            onClick = { onMonthChange(-1) },
            shape = RoundedCornerShape(12.dp))

        {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Previous Month"
            )
        }

        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        OutlinedButton(
            onClick = { onMonthChange(1) },
            shape = RoundedCornerShape(12.dp) // Adjust the corner radius as needed
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next Month"
            )
        }
    }
}


@Composable
fun CalendarView(currentMonth: YearMonth) {
    val days = getDaysInMonth(currentMonth)

    LazyColumn {
        item { WeekdayHeaders() }
        items(days.chunked(7)) { week ->
            WeekRow(week, currentMonth)
        }
    }
}

@Composable
fun WeekRow(week: List<LocalDate>, currentMonth: YearMonth) {
    Row(Modifier
        .fillMaxWidth()
        .padding(start = 5.dp, end = 5.dp)) {
        week.forEach { date ->
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f) // Add weight to each cell
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val textColor = if (date.month == currentMonth.month) {
                    MaterialTheme.colors.onSurface // Use primary text color for current month dates
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.3f) // Use a lighter color for other months
                }

                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor
                )
            }
        }
    }
}



@Composable
fun WeekdayHeaders() {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val daysOfWeek = listOf("M", "D", "M", "D", "F", "S", "S")

    Row(modifier = Modifier.fillMaxWidth()
        .padding(start = 5.dp, end = 5.dp)) {
        daysOfWeek.rotate(firstDayOfWeek.ordinal).forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun <T> List<T>.rotate(distance: Int): List<T> {
    val listSize = this.size
    val actualDistance = distance % listSize
    return this.drop(actualDistance) + this.take(actualDistance)
}

fun getDaysInMonth(currentMonth: YearMonth): List<LocalDate> {
    val firstOfMonth = currentMonth.atDay(1)
    val lastOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    // Adjust startDate to the start of the first week that includes the firstOfMonth
    var startDate = firstOfMonth
    while (startDate.dayOfWeek != firstDayOfWeek) {
        startDate = startDate.minusDays(1)
    }

    val days = mutableListOf<LocalDate>()
    var currentDate = startDate
    // Continue until the end of the week that contains the last day of the month
    while (!currentDate.isAfter(lastOfMonth) || currentDate.dayOfWeek != firstDayOfWeek) {
        days.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }

    return days
}



