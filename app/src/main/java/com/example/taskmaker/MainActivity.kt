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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.aeternumindustries.taskmaker.R
import com.aeternumindustries.taskmaker.databinding.ActivityMainBinding
import com.example.taskmaker.ui.theme.TaskMakerTheme
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay


class MainActivity : AppCompatActivity() {

    private var tasks = mutableStateListOf<String>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var fab: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private var showAddTaskDialog by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        val savedTasks = sharedPref.getStringSet("tasks", null)

        tasks.addAll(savedTasks ?: listOf("Apfel", "Birne"))
        showAddTaskDialog = false

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        WindowCompat.setDecorFitsSystemWindows(window, false)


        binding.composeView.setContent {
            TaskMakerTheme { // Apply Material 3 theming
                // Your existing Compose content
                TaskListScreen(tasks)
                if (showAddTaskDialog) {
                    AddTaskDialog(onAdd = { task ->
                        tasks.add(task)
                        showAddTaskDialog = false
                        saveTasks()
                    }, onDismiss = { showAddTaskDialog = false })
                }
            }
        }
    }

    @Composable
    fun AppTheme(content: @Composable () -> Unit) {
        val colorScheme = lightColorScheme(
            primary = Color(0xFFFFFFFF), // Example primary color
            onPrimary = Color.White, // Content color for primary
            // Define other colors as needed
            surfaceVariant = Color.White     // This is the color we want for the status bar
        )

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Preview(showBackground = true)
    @Composable
    fun PreviewTaskListScreen() {
        val previewTasks = remember { mutableStateListOf("Task 1", "Task 2", "Task 3") }
        TaskListScreen(previewTasks)
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
                Button(
                    onClick = {
                        onAdd(text)
                        text = "" // Optional: Clear the text field after adding a task
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    private fun saveTasks() {
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet("tasks", tasks.toSet())
            apply()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @Composable
    fun TaskListScreen(tasks: SnapshotStateList<String>) {
        val myAppColorScheme = lightColorScheme(
            primary = Color(0xFF6750A4), // Light purple color
            onPrimary = Color.White,
            background = Color.White, // Set background color to pure white
            surface = Color.White,    // Set surface color to pure white for the AppBar
            onSurface = Color.Black   // Set content color for AppBar, choose a color that contrasts well with white
            // Add other color definitions as needed
        )

        MaterialTheme(colorScheme = myAppColorScheme) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Calendo", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 10.dp)) },
                        // Set the background color of the AppBar
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.height(48.dp)
                    )
                },

                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddTaskDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0xFF6750A4),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, "Floating action button.", tint = Color.White)
                    }
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues)
                    ) {
                        CustomCalendar() // Assuming this is a custom Composable function
                        TaskList(tasks)
                    }
                }
            )
        }
    }




    @Composable
    fun TaskList(tasks: SnapshotStateList<String>) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .padding(horizontal = 8.dp)
                .padding(vertical = 10.dp)
        ) {
            items(items = tasks, key = { it }) { task ->
                val isVisible = remember { mutableStateOf(true) }
                TaskItem(task = task, isVisible = isVisible, onDelete = {
                    if (isVisible.value) {
                        isVisible.value = false
                    } else {
                        tasks.remove(task)
                        saveTasks()
                    }
                })
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }

    @Composable
    fun TaskItem(task: String, isVisible: MutableState<Boolean>, onDelete: (String) -> Unit) {
        val isVisibleValue = isVisible.value

        AnimatedVisibility(
            visible = isVisibleValue,
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .padding(vertical = 3.dp, horizontal = 0.dp)
            ) {
                val textColor = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
                Text(text = task, color = textColor, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    isVisible.value = false
                    onDelete(task)
                }, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = Color(0xFF6750A4))
                }


                if (!isVisibleValue) {
                    LaunchedEffect(task) {
                        delay(300)
                        onDelete(task)
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }
}

