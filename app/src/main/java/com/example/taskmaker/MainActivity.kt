package com.example.taskmaker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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

        val priorityColor = when (priority) {
            2 -> Color.Yellow
            3 -> Color(0xFFFFA500) // Orange
            4 -> Color(0xFF800000) // Vine Red
            else -> Color.Gray
        }

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
                    Text(text = "Task hinzufügen", color = Color(0xFF6200EE)) // Purple color
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
                        Text("Priority:", color = Color(0xFF6200EE)) // Purple color
                        Row {
                            PriorityButton(
                                priorityLevel = 2,
                                currentPriority = priority
                            ) { priority = 2 }
                            PriorityButton(
                                priorityLevel = 3,
                                currentPriority = priority
                            ) { priority = 3 }
                            PriorityButton(
                                priorityLevel = 4,
                                currentPriority = priority
                            ) { priority = 4 }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val taskWithPriority = text + if (priority > 1) " ($priority)" else ""
                            if (taskWithPriority.isNotBlank()) {
                                onAdd(taskWithPriority)
                                text = "" // Clear the text field after adding a task
                                priority = 1 // Reset to default (normal priority)
                            }
                            keyboardController?.hide()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6200EE)) // Purple color
                    ) {
                        Text("Hinzufügen")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6200EE)) // Purple color
                    ) {
                        Text("Abbrechen")
                    }
                },
                shape = RoundedCornerShape(16.dp) // More rounded corners
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    @Composable
    fun PriorityButton(priorityLevel: Int, currentPriority: Int, setPriority: () -> Unit) {
        val isSelected = currentPriority == priorityLevel
        val flagColor = when (priorityLevel) {
            2 -> Color(color = 0xFF008080)// For priority "!"
            3 -> Color(0xFFFFA500) // For priority "!!"
            4 -> Color.Red // For priority "!!!"
            else -> Color.Gray // No priority or default
        }

        OutlinedButton(
            onClick = setPriority,
            modifier = Modifier.padding(end = 8.dp),
            shape = RoundedCornerShape(50), // More rounded corners
            colors = ButtonDefaults.outlinedButtonColors(
                backgroundColor = if (isSelected) Color.LightGray else Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFF6200EE)) // Purple border
        ) {
            if (priorityLevel > 1) { // Show flag only for priorities "!", "!!", and "!!!"
                Icon(
                    imageVector = rememberFlag(),
                    contentDescription = "Priority Flag",
                    tint = flagColor,
                    modifier = Modifier.size(20.dp) // Smaller icon size
                )
            }
        }
    }

    @Composable
    fun rememberFlag(): ImageVector {
        return remember {
            ImageVector.Builder(
                name = "flag",
                defaultWidth = 40.0.dp,
                defaultHeight = 40.0.dp,
                viewportWidth = 40.0f,
                viewportHeight = 40.0f
            ).apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1.0f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Miter,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(9.875f, 34.792f)
                    quadToRelative(-0.542f, 0f, -0.937f, -0.375f)
                    quadToRelative(-0.396f, -0.375f, -0.396f, -0.917f)
                    verticalLineTo(8.208f)
                    quadToRelative(0f, -0.541f, 0.375f, -0.916f)
                    reflectiveQuadToRelative(0.958f, -0.375f)
                    horizontalLineToRelative(11.833f)
                    quadToRelative(0.459f, 0f, 0.813f, 0.291f)
                    quadToRelative(0.354f, 0.292f, 0.437f, 0.75f)
                    lineToRelative(0.542f, 2.417f)
                    horizontalLineToRelative(8.292f)
                    quadToRelative(0.583f, 0f, 0.958f, 0.375f)
                    reflectiveQuadToRelative(0.375f, 0.958f)
                    verticalLineToRelative(12.834f)
                    quadToRelative(0f, 0.541f, -0.375f, 0.916f)
                    reflectiveQuadToRelative(-0.958f, 0.375f)
                    horizontalLineTo(23.5f)
                    quadToRelative(-0.5f, 0f, -0.854f, -0.271f)
                    quadToRelative(-0.354f, -0.27f, -0.438f, -0.77f)
                    lineToRelative(-0.541f, -2.417f)
                    horizontalLineTo(11.208f)
                    verticalLineTo(33.5f)
                    quadToRelative(0f, 0.542f, -0.396f, 0.917f)
                    quadToRelative(-0.395f, 0.375f, -0.937f, 0.375f)
                    close()
                    moveToRelative(10.958f, -18.417f)
                    close()
                    moveToRelative(3.792f, 6.833f)
                    horizontalLineToRelative(5.833f)
                    verticalLineTo(13.042f)
                    horizontalLineToRelative(-9.166f)
                    lineToRelative(-0.75f, -3.5f)
                    horizontalLineToRelative(-9.334f)
                    verticalLineTo(19.75f)
                    horizontalLineToRelative(12.667f)
                    close()
                }
            }.build()
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
    fun TaskItem(task: String, onDelete: (String) -> Unit, onTaskCompleted: (String) -> Unit) {
        // Define the isChecked state here
        val isChecked = remember { mutableStateOf(false) }

        // Initialize variables for description and priority level
        var description = task
        var priorityLevel = 1

        // Check if the task contains priority information and extract it
        if (task.contains("(") && task.endsWith(")")) {
            val splitTask = task.split("(", limit = 2)
            description = splitTask[0]
            priorityLevel = splitTask[1].dropLast(1).toIntOrNull() ?: 1
        }

        val flagColor = when (priorityLevel) {
            2 -> Color(0xFF008080) // Teal for priority "!"
            3 -> Color(0xFFFFA500) // Orange for priority "!!"
            4 -> Color.Red         // Red for priority "!!!"
            else -> Color.Transparent // No priority
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp, horizontal = 0.dp)
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

            if (flagColor != Color.Transparent) {
                Icon(
                    imageVector = rememberFlag(),
                    contentDescription = "Priority Flag",
                    tint = flagColor,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            }

            Text(
                text = description,
                color = if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurface,
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