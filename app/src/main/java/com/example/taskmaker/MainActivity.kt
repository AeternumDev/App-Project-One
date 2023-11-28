package com.example.taskmaker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.aeternumindustries.taskmaker.databinding.ActivityMainBinding
import com.example.taskmaker.ui.theme.TaskMakerTheme
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var tasks = mutableStateListOf<String>()
    private lateinit var binding: ActivityMainBinding
    private var showAddTaskDialog by mutableStateOf(false)
    private var completedTasks = mutableStateListOf<String>()


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.composeView.setContent {
            TaskMakerTheme { // Apply Material 3 theming
                // Your existing Compose content
                TaskListScreen(tasks)
                if (showAddTaskDialog) {
                    AddTaskDialog(
                        showAddTaskDialog = showAddTaskDialog,
                        onAdd = { task ->
                            tasks.add(task)
                            showAddTaskDialog = false
                            saveTasks()
                        }
                    ) { showAddTaskDialog = false }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences("task_preferences", Context.MODE_PRIVATE)
        val savedTasks = sharedPref.getStringSet("tasks", null)
        val savedCompletedTasks =
            sharedPref.getStringSet("completedTasks", null) // Load completed tasks

        savedTasks?.let {
            tasks.clear()
            tasks.addAll(it)
        }

        savedCompletedTasks?.let {
            completedTasks.clear()
            completedTasks.addAll(it)
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


    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun AddTaskDialog(showAddTaskDialog: Boolean, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
        var text by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(1) } // Default to 1 (normal priority)
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        val prioritySymbol = when (priority) {
            2 -> "!"
            3 -> "!!"
            4 -> "!!!"
            else -> "" // No symbol for normal priority
        }

        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = {
                    keyboardController?.hide()
                    onDismiss()
                },
                title = {
                    Text(text = "Task hinzufügen")
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Task Description") },
                            singleLine = true,
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Priority:")
                        Row {
                            PriorityButton(priorityLabel = "!", setPriority = { priority = 2 })
                            PriorityButton(priorityLabel = "!!", setPriority = { priority = 3 })
                            PriorityButton(priorityLabel = "!!!", setPriority = { priority = 4 })
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Concatenate the task description with the priority symbol
                            val taskWithPriority =
                                text + if (prioritySymbol.isNotEmpty()) " ($prioritySymbol)" else ""
                            if (taskWithPriority.isNotBlank()) {
                                onAdd(taskWithPriority)
                                text = "" // Clear the text field after adding a task
                                priority = 1 // Reset to default (normal priority)
                            }
                            keyboardController?.hide()
                        }
                    ) {
                        Text("Hinzufügen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        keyboardController?.hide()
                        onDismiss()
                    }) {
                        Text("Abbrechen")
                    }
                },
                shape = RoundedCornerShape(8.dp)
            )

            // Launch effect to request focus and show keyboard
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    @Composable
    fun PriorityButton(priorityLabel: String, setPriority: () -> Unit) {
        Button(
            onClick = setPriority,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(priorityLabel)
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

        val onTaskCompleted: (String) -> Unit = { task ->
            tasks.remove(task)
            completedTasks.add(task)
        }


        MaterialTheme(colorScheme = myAppColorScheme) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope() // Create a coroutine scope

            Scaffold(

                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Calendo",
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    modifier = Modifier.size(25.dp)
                                )
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
                        CustomCalendar() // Your custom calendar component
                        if (completedTasks.isNotEmpty()) {
                            CompletedTaskSection(completedTasks)
                        }

                        TaskList(tasks, onTaskCompleted = onTaskCompleted)
                    }
                }
            )
        }
    }

    //done/erledigt button
    @Composable
    fun CompletedTaskSection(completedTasks: SnapshotStateList<String>) {
        var expanded by remember { mutableStateOf(false) }
        val lightGreen = Color(0xFFB2F2BB)

        val animationProgress by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            // Increase the duration for a smoother transition
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing), label = ""
        )

        Card(
            modifier = Modifier
                .padding(8.dp)
                .graphicsLayer {
                    // Scale the card based on the animation progress
                    scaleX = lerp(start = 0.95f, stop = 1f, fraction = animationProgress)
                    scaleY = lerp(start = 0.95f, stop = 1f, fraction = animationProgress)
                    // Adjust the alpha value based on the animation progress
                    alpha = lerp(start = 0.7f, stop = 1f, fraction = animationProgress)
                },
            shape = RoundedCornerShape(12.dp),
            elevation = 0.dp,
            backgroundColor = lightGreen
        ) {
            Column(modifier = Modifier.clickable { expanded = !expanded }) {
                Text(
                    "Done",
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                if (expanded) {
                    Column {
                        completedTasks.forEach { task ->
                            TaskItemRow(task, onReactivateTask = {
                                completedTasks.remove(task)
                                tasks.add(task)
                            })
                        }
                    }
                }
            }
        }
    }

    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return (1 - fraction) * start + fraction * stop
    }

    @Composable
    fun TaskItemRow(task: String, onReactivateTask: () -> Unit) {
        Row {
            Text(
                text = task,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                    .weight(1f),
                color = Color.Gray
            )
            IconButton(onClick = onReactivateTask) {
                Icon(Icons.Default.Refresh, contentDescription = "Unmark as completed")
            }
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
            putStringSet("completedTasks", completedTasks.toSet()) // Save completed tasks
            apply()
        }
    }


    @Composable
    fun TaskList(tasks: SnapshotStateList<String>, onTaskCompleted: (String) -> Unit) {
        LazyColumn(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .padding(horizontal = 8.dp)
                .padding(vertical = 10.dp)
        ) {
            itemsIndexed(items = tasks) { index, task ->
                val isVisible = remember { mutableStateOf(true) }
                TaskItem(task = task, onDelete = {
                    if (isVisible.value) {
                        isVisible.value = false
                    } else {
                        tasks.remove(task)
                        saveTasks()
                    }
                }, onTaskCompleted = onTaskCompleted)
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }
    }

    //Tasklist
    @Composable
    fun TaskItem(
        task: String,
        onDelete: (String) -> Unit,
        onTaskCompleted: (String) -> Unit
    ) {
        val isChecked =
            remember { mutableStateOf(false) } // State to keep track of the checkmark status

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp, horizontal = 0.dp)
        ) {
            // Checkbox with a circle shape
            Checkbox(
                checked = isChecked.value,
                onCheckedChange = { checked ->
                    isChecked.value = checked
                    if (checked) {
                        onTaskCompleted(task)
                    }
                },
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
            IconButton(
                onClick = { onDelete(task) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF6750A4)
                )
            }
        }
    }
}
