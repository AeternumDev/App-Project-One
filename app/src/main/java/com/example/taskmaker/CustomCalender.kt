package com.example.taskmaker

import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*



// This annotation specifies that the following function requires a minimum API level of 34 to work.
@RequiresApi(34)
// This annotation marks a function as a Composable, which means it can define UI elements in Jetpack Compose.
@Preview
// enables Composable preview
@Composable
fun CustomCalendar() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column {
        MonthNavigation(currentMonth) { direction ->
            currentMonth = currentMonth.plusMonths(direction.toLong())
        }

        CalendarGrid(currentMonth) { direction ->
            currentMonth = currentMonth.plusMonths(direction.toLong())
        }
    }
}

// GRID

@Composable
fun MonthNavigation(currentMonth: YearMonth, onMonthChange: (Int) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val textColor = if (isSystemInDarkTheme()) {
        Color.White // Use white color in dark theme
    } else {
        MaterialTheme.colors.onBackground // Use default color in light theme
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyledButton(onClick = { onMonthChange(-1) }) {
            Text("<", fontSize = 15.sp)
        }

        Text(
            text = currentMonth.format(formatter),
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        StyledButton(onClick = { onMonthChange(1) }) {
            Text(">", fontSize = 15.sp)
        }
    }
}

@Composable
fun StyledButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(40.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        )
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
fun CalendarGrid(month: YearMonth, onMonthChange: (Int) -> Unit) {
    val daysInMonth = getDaysInMonth(month)
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val dragAmount = remember { mutableStateOf(0f) }

    val textColor = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        MaterialTheme.colors.onBackground
    }

    val dragGesture = Modifier.draggable(
        orientation = Orientation.Horizontal,
        state = rememberDraggableState { delta ->
            dragAmount.value += delta
        },
        onDragStopped = {
            if (Math.abs(dragAmount.value) > 100) { // Adjust the threshold based on your needs
                if (dragAmount.value > 0) {
                    onMonthChange(-1)
                } else {
                    onMonthChange(1)
                }
            }
            dragAmount.value = 0f
        }
    )
    AnimatedContent(
        targetState = month,
        modifier = dragGesture,
        transitionSpec = {
            // Determine the direction of the month change
            val direction = if (targetState > initialState) 1 else -1

            // Configure slide in and out animations based on direction
            slideInHorizontally(initialOffsetX = { direction * it }) + fadeIn() with
                    slideOutHorizontally(targetOffsetX = { -direction * it }) + fadeOut()
        }, label = ""
    ) { targetMonth ->
        val daysInTargetMonth = getDaysInMonth(targetMonth)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        Column {
            // Display weekdays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.width(40.dp)) // Space for week numbers
                val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
                val orderedWeekdays = weekdays.rotate(firstDayOfWeek.ordinal)

                orderedWeekdays.forEach { day ->
                    Text(text = day, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(end = 25.dp))


                }
            }

            // Custom grid for calendar
            Row {
                // Column for week numbers
                Column(
                    modifier = Modifier.padding(horizontal = 15.dp)
                ) {
                    daysInTargetMonth.chunked(7).forEach { week ->
                        Text(
                            text = "W" + week.first()
                                .get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                                .toString(),
                            modifier = Modifier
                                .height(40.dp) // Match the height of day cells
                                .align(Alignment.CenterHorizontally)// Fixed width
                        )
                    }
                }

                // Calendar days grid
                Column(
                    modifier = Modifier
                        .padding(end = 25.dp)


                ) {
                    daysInTargetMonth.chunked(7).forEachIndexed { index, week ->
                        Row {
                            // Add spacers if the first week
                            if (index == 0) {
                                repeat(week.first().dayOfWeek.ordinal - firstDayOfWeek.ordinal) {
                                    Spacer(modifier = Modifier
                                        .weight(1f))
                                }
                            }

                            week.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(40.dp)
                                        .padding(0.dp)
                                        .background(
                                            if (day == selectedDate) MaterialTheme.colors.primary else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { selectedDate = day }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.dayOfMonth.toString(),
                                        color = if (day == selectedDate) MaterialTheme.colors.onSecondary else textColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Add spacers if the last week
                            if (index == daysInTargetMonth.size / 7 - 1) {
                                repeat((7 - week.size) % 7) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

fun <T> List<T>.rotate(distance: Int): List<T> {
val listSize = this.size
val actualDistance = distance % listSize
return this.drop(actualDistance) + this.take(actualDistance)}



