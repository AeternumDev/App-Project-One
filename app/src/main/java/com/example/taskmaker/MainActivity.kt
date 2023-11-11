package com.example.taskmaker

import com.aeternumindustries.taskmaker.TaskAdapter
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.Context
import android.os.PersistableBundle
import android.view.inputmethod.InputMethodManager
import com.aeternumindustries.taskmaker.R
import com.aeternumindustries.taskmaker.databinding.ActivityMainBinding
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.Composable
import com.google.android.material.navigation.NavigationView


// Import statements: These help the code understand where to get certain tools and definitions
// They referencing a dictionary to understand the meaning of a word.

// The main screen of the app
class MainActivity : AppCompatActivity() {

    // These are variables for different parts of the app
    // Telling the app "Hey, I'm going to use these, but I'll give details later."
    private lateinit var binding: ActivityMainBinding
    private lateinit var lvtodolist: ListView
    private lateinit var fab: FloatingActionButton
    private lateinit var shoppingItems: ArrayList<String>
    private lateinit var itemadapter: ArrayAdapter<String>
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    // This function sets up the main screen when the app starts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This connects the visual design file to this code
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Connect the visual parts of the app to the code variables
        lvtodolist = findViewById(R.id.lvtodolist)
        fab = findViewById(R.id.floatingActionButton)

        // Initialize the shopping list with two items
        shoppingItems = ArrayList()
        shoppingItems.add("Apfel")
        shoppingItems.add("Birne")

        // Setup the list adapter (a manager for the list)
        itemadapter = TaskAdapter(this, shoppingItems)
        lvtodolist.adapter = itemadapter



        // When the floating button is clicked, do the following:
        fab.setOnClickListener {
            // This will show a pop-up to add a task
            var builder = AlertDialog.Builder(this)
            builder.setMessage("Add Task")

            // This is an input box for the user to type in
            var input = EditText(this)
            input.hint = "create something wonderful"
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            // If "Add Task" is clicked on the pop-up, add the entered text to the list
            builder.setPositiveButton("Add Task") { dialog, which ->
                shoppingItems.add(input.text.toString())
                itemadapter.notifyDataSetChanged()
            }

            // If "Cancel" is clicked, just close the pop-up
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            // This makes the pop-up actually show up on the screen
            builder.show()
        }

        lvtodolist.setOnItemClickListener { _, _, position, _ ->
            val listItem = lvtodolist.getChildAt(position - lvtodolist.firstVisiblePosition)
            val taskEditText = listItem?.findViewById<EditText>(R.id.taskDescription)
            taskEditText?.requestFocus()
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(taskEditText, InputMethodManager.SHOW_IMPLICIT)
        }

            drawerLayout = findViewById(R.id.drawer_layout)
            navigationView = findViewById(R.id.nav_view)

            toggle = ActionBarDrawerToggle(this, drawerLayout, binding.materialToolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }
}