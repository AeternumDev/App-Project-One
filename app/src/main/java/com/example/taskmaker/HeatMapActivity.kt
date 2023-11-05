/* // package com.example.taskmaker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.aeternumindustries.taskmaker.R
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarImpl
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState

class HeatMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heat_map)

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            HeatMapCalendarView()
        }
    }
}

@Composable
fun HeatMapCalendarView() {
    val heatmapState = rememberHeatMapCalendarState(/* necessary arguments here, if any */)
    HeatMapCalendarImpl(state = heatmapState)
}

*/