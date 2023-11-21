package com.example.taskmaker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.drawerlayout.widget.DrawerLayout
import com.aeternumindustries.taskmaker.R
import com.aeternumindustries.taskmaker.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView


// Import statements: These help the code understand where to get certain tools and definitions
// They referencing a dictionary to understand the meaning of a word.
// The main screen of the app
class MainActivity : AppCompatActivity() {

    // 'tasks' holds the list of task strings. It's a state list, which means Compose will
    // automatically update the UI when this list changes.
    private var tasks = mutableStateListOf<String>()

    // lateinit vars for various UI components
    private lateinit var binding: ActivityMainBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load previously saved tasks from SharedPreferences, a key-value storage
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        val savedTasks = sharedPref.getStringSet("tasks", null)
        tasks.addAll(savedTasks ?: listOf("Apfel", "Birne")) // Load saved tasks or default ones

        // Inflate the layout defined in XML and set it as the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components by finding them in the layout
        fab = findViewById(R.id.floatingActionButton)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.materialToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        // Set up the navigation drawer (side menu)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set an onClickListener on the FloatingActionButton to add new tasks
        fab.setOnClickListener {
            // (Your existing code for adding a task would go here)
        }

        // Set the Compose UI content within the designated ComposeView in the XML layout
        binding.composeView.setContent {
            TaskListScreen(tasks) // This calls a Composable function to render the task list
        }
    }

    // This Composable function defines the UI layout for the task list screen

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    fun TaskListScreen(tasks: SnapshotStateList<String>) {
        // A Column arranges its children vertically
        Column(modifier = Modifier.fillMaxWidth()) {
            CustomCalendar() // This is a custom Composable for showing a calendar
            TaskList(tasks) // Composable function that displays the task list
        }
    }

    // This Composable function displays the tasks in a LazyColumn (like RecyclerView)
    @Composable
    fun TaskList(tasks: SnapshotStateList<String>) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp,)
                .padding(horizontal = 8.dp)
                .padding(vertical = 10.dp)
        ) {
            // Display each task in the list with TaskItem Composable
            items(items = tasks, key = { it }) { task ->
                TaskItem(task = task)
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }

    // Defines the UI for each task item in the list
    @Composable
    fun TaskItem(task: String) {
        val textColor = if (isSystemInDarkTheme()) {
            Color.White // Use white color in dark theme
        } else {
            MaterialTheme.colors.onBackground // Use default color in light theme
        }
        // A Row arranges its children horizontally
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp, horizontal = 0.dp)
        ) {
            Text(
                text = task,
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f)) // Spacer for pushing the delete button to the end

            IconButton(
                onClick = { /* Handle delete action */ },
                modifier = Modifier
                    .align(Alignment.CenterVertically)

            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF5F30DB))
            }
        }
    }

    // Function to save the current state of tasks to SharedPreferences
    fun saveTasks() {
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet("tasks", tasks.toSet()) // Convert the task list to a set and save it
            apply() // Apply the changes
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState() // Ensure the state of the toggle for the drawer is in sync
    }
}

