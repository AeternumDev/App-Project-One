package com.example.taskmaker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.drawerlayout.widget.DrawerLayout
import com.aeternumindustries.taskmaker.R
import com.aeternumindustries.taskmaker.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay

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
    private var showAddTaskDialog by mutableStateOf(false)


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load previously saved tasks from SharedPreferences, a key-value storage
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        val savedTasks = sharedPref.getStringSet("tasks", null)
        tasks.addAll(savedTasks ?: listOf("Apfel", "Birne")) // Load saved tasks or default ones
        showAddTaskDialog = false

        // Inflate the layout defined in XML and set it as the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components by finding them in the layout

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.materialToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        // Set up the navigation drawer (side menu)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set the Compose UI content within the designated ComposeView in the XML layout
        binding.composeView.setContent {
            TaskListScreen(tasks)
            if (showAddTaskDialog) {
                AddTaskDialog(onAdd = { task ->
                    tasks.add(task)
                    showAddTaskDialog = false
                    saveTasks()
                }, onDismiss = {
                    showAddTaskDialog = false
                })
            }
        }
    }

    // This Composable function defines the UI layout for the task list screen
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Preview(showBackground = true)
    @Composable
    fun PreviewTaskListScreen() {
        val tasks = remember { mutableStateListOf("Task 1", "Task 2", "Task 3") }
        TaskListScreen(tasks = tasks)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    fun TaskListScreen(tasks: SnapshotStateList<String>) {
        // Scaffold is used to provide material design components
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFF5F30DB),
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 80.dp)) {
                    Icon(
                        Icons.Default.Add, "Floating action button.",
                        tint = Color.White)
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                ) {
                    CustomCalendar() // This is a custom Composable for showing a calendar
                    TaskList(tasks) // Composable function that displays the task list
                }
            }
        )
    }
    // This Composable function displays the tasks in a LazyColumn (like RecyclerView)

    @Composable
    fun TaskList(tasks: SnapshotStateList<String>) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .padding(horizontal = 8.dp)
                .padding(vertical = 10.dp)
        ) {
            items(items = tasks, key = { it }) { task ->
                var isVisible = remember { mutableStateOf(true) }

                TaskItem(task = task, isVisible = isVisible, onDelete = {
                    if (isVisible.value) { // Use isVisible.value instead of just isVisible
                        isVisible.value = false // Trigger exit animation
                    } else {
                        tasks.remove(task) // Remove the task after animation
                    }
                })

                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }

    // Defines the UI for each task item in the list
    @Composable
    fun TaskItem(task: String, onDelete: (String) -> Unit, isVisible: MutableState<Boolean>) {
        val isVisibleValue = isVisible.value // Extract the value of isVisible

        AnimatedVisibility(
            visible = isVisibleValue, // Pass the extracted value to AnimatedVisibility
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically() // Add fade-out and shrink animations for exit
        ) {
            Row(
                modifier = Modifier
                    .animateContentSize() // Animate size changes
                    .fillMaxWidth()
                    .padding(vertical = 3.dp, horizontal = 0.dp)
            ) {
                // Determine the text color based on the theme
                val textColor = if (isSystemInDarkTheme()) {
                    Color.White // Use white color in dark theme
                } else {
                    MaterialTheme.colors.onBackground
                    // Use default color in light theme
                }

                Text(
                    text = task,
                    color = textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(Modifier.weight(1f)) // Spacer for pushing the delete button to the end

                IconButton(
                    onClick = {
                        isVisible.value = false // Update visibility
                        onDelete(task)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF5F30DB)
                    )
                }

                if (!isVisibleValue) { // Use isVisibleValue instead of isVisible
                    LaunchedEffect(task) {
                        delay(300) // Duration for exit animation
                        onDelete(task)
                    }
                }
            }
        }
    }


    @Composable
    fun AddTaskDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
        var text by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Task") },
            text = {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Task Name") }
                )
            },
            confirmButton = {
                Button(onClick = { onAdd(text) }) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = {
                    // Check if a task has been added
                    text = "" // Reset the text field
                    showAddTaskDialog = true // Keep the dialog open
                }) {
                    Text("Cancel")
                }
            }


        )
    }
    // Function to save the current state of tasks to SharedPreferences
    private fun saveTasks() {
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
