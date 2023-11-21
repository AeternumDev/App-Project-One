package com.example.taskmaker

import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// This annotation specifies that the following function requires a minimum API level of 34 to work.
@RequiresApi(34)
// This annotation marks a function as a Composable, which means it can define UI elements in Jetpack Compose.
@Preview
// enables Composable preview
@Composable
fun CustomCalendar() {
    // This variable holds the current month, and 'remember' is used to retain its value across recompositions.
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    // This calculates the days to be displayed in the current month.
    val daysInMonth = getDaysInMonth(currentMonth)

    // Column is a Composable that places its children vertically.
    Column {
        // MonthNavigation is a custom Composable function defined below to navigate between months.
        MonthNavigation(currentMonth) { direction ->
            // Changes the current month when navigation buttons are clicked.
            currentMonth = currentMonth.plusMonths(direction.toLong())
        }

        // CalendarGrid is a custom Composable function defined below that displays the days of the month in a grid.
        CalendarGrid(currentMonth)
    }
}

// GRID

@Composable
fun MonthNavigation(currentMonth: YearMonth, onMonthChange: (Int) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledButton(onClick = { onMonthChange(-1) }) {
            Text("◀", fontSize = 20.sp)
        }

        Text(
            text = currentMonth.format(formatter),
            color = MaterialTheme.colors.onBackground,
            fontSize = 20.sp,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        StyledButton(onClick = { onMonthChange(1) }) {
            Text("▶", fontSize = 20.sp)
        }
    }
}

@Composable
fun StyledButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(50.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF5F30DB),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        ),
        border = BorderStroke(1.dp, Color(0xFF5F30DB))
    ) {
        content()
    }
}

// Function to get all days in a month as a list of LocalDate
@RequiresApi(34)
fun getDaysInMonth(currentMonth: YearMonth): List<LocalDate> {
    // Calculate the first day to be displayed (might be in the previous month).
    val firstOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var firstVisibleDay = firstOfMonth
    while (firstVisibleDay.dayOfWeek != firstDayOfWeek) {
        firstVisibleDay = firstVisibleDay.minusDays(1)
    }

    // Calculate the last day to be displayed (might be in the next month).
    val lastOfMonth = currentMonth.atEndOfMonth()
    var lastVisibleDay = lastOfMonth
    while (lastVisibleDay.dayOfWeek != firstDayOfWeek.minus(1)) {
        lastVisibleDay = lastVisibleDay.plusDays(1)
    }

    // Return the list of days from the first to the last visible day.
    val days = mutableListOf<LocalDate>()
    var currentDate = firstVisibleDay
    while (!currentDate.isAfter(lastVisibleDay)) {
        days.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }

    return days
}

// GRID DISPLAYS DAYS
@RequiresApi(34)
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarGrid(month: YearMonth) {
    val daysInMonth = getDaysInMonth(month)
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }


    // Updated AnimatedContent with only slide animation
    AnimatedContent(
        targetState = month,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { 300 }) + fadeIn() with
                    slideOutHorizontally(targetOffsetX = { -300 })+ fadeOut(animationSpec = tween(durationMillis = 150))) // Slide out to the left
                .using(SizeTransform(clip = false))
        }, label = ""
    ) { targetMonth ->
        val daysInTargetMonth = getDaysInMonth(targetMonth) // Calculate days for the target month

        Box(
            modifier = Modifier
                .fillMaxWidth() // Fill the maximum width
                .padding(horizontal = 20.dp), // Adjust horizontal padding for centering
            contentAlignment = Alignment.Center // Align the content to the center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7), // 7 columns for 7 days
                contentPadding = PaddingValues(5.dp) // Padding around the grid
            ) {
                items(daysInMonth) { day ->
                    Box(
                        modifier = Modifier
                            .size(40.dp) // Fixed size for each cell
                            .background(
                                if (day == selectedDate) MaterialTheme.colors.primary else Color.Transparent,
                                CircleShape
                            )
                            .clickable { selectedDate = day }
                            .padding(8.dp), // Padding inside each cell
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            color = if (day == selectedDate) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onBackground,
                            fontSize = 12.sp) // Adjust font size as needed

                }
            }
        }
    }
}

// CALCULATION OF DAYS OF THE MONTH
@RequiresApi(34)
fun getDaysInMonth(currentMonth: YearMonth) {
    // Calculate the first day to be displayed (might be in the previous month).
    val firstOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    var firstVisibleDay = firstOfMonth
    while (firstVisibleDay.dayOfWeek != firstDayOfWeek) {
        firstVisibleDay = firstVisibleDay.minusDays(1)
    }

    // Calculate the last day to be displayed (might be in the next month).
    val lastOfMonth = currentMonth.atEndOfMonth()
    var lastVisibleDay = lastOfMonth
    while (lastVisibleDay.dayOfWeek != firstDayOfWeek.minus(1))
        lastVisibleDay = lastVisibleDay.plusDays(1)
    }
}



    // Return the list of days from the first to the last visible day.

