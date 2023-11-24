package com.example.taskmaker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.aeternumindustries.taskmaker.databinding.ActivityMainBinding
import com.example.taskmaker.ui.theme.TaskMakerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class MainActivity : AppCompatActivity() {

    private var tasks = mutableStateListOf<String>()
    private lateinit var binding: ActivityMainBinding
    private var showAddTaskDialog by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(binding.root)

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

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        val savedTasks = sharedPref.getStringSet("tasks", null)
        if (savedTasks != null) {
            tasks = mutableStateListOf<String>().apply { addAll(savedTasks) }
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
            title = {
                Text(
                    "Task hinzufügen"

                )
            },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Äpfel kaufen...") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp) // Rounded corners for the TextField
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAdd(text)
                        text = "" // Optional: Clear the text field after adding a task
                    }
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Abbrechen")
                }
            },
            shape = RoundedCornerShape(16.dp), // Rounded corners for the dialog
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
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
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope() // Create a coroutine scope

            Scaffold(

                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Calendo", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface) },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(25.dp))
                            }
                        },
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

                drawerContent = {
                    DrawerContent(drawerState)
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
    fun DrawerContent(drawerState: DrawerState) {
        // State variables to trigger the LaunchedEffect
        var archivClicked by remember { mutableStateOf(false) }
        var einstellungenClicked by remember { mutableStateOf(false) }

        // LaunchedEffect for Archiv
        LaunchedEffect(archivClicked) {
            if (archivClicked) {
                drawerState.close()
                archivClicked = false // Reset the trigger
            }
        }

        // LaunchedEffect for Einstellungen
        LaunchedEffect(einstellungenClicked) {
            if (einstellungenClicked) {
                drawerState.close()
                einstellungenClicked = false // Reset the trigger
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Text("Archiv", modifier = Modifier.padding(16.dp).clickable {
                archivClicked = true // Set the trigger for Archiv
            })
            Text("Einstellungen", modifier = Modifier.padding(16.dp).clickable {
                einstellungenClicked = true // Set the trigger for Einstellungen
            })
        }
    }

    private fun saveTasks() {
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putStringSet("tasks", tasks.toSet())
            apply()
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
        val isChecked =
            remember { mutableStateOf(false) } // State to keep track of the checkmark status

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
                // Checkbox with a circle shape
                Checkbox(
                    checked = isChecked.value,
                    onCheckedChange = { isChecked.value = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(end = 8.dp).align(Alignment.CenterVertically)

                )

                val textColor =
                    if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface
                Text(
                    text = task,
                    color = textColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    isVisible.value = false
                    onDelete(task)
                }, modifier = Modifier.align(Alignment.CenterVertically)) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF6750A4)
                    )
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

}

