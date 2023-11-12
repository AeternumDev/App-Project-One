package com.aeternumindustries.taskmaker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.example.taskmaker.MainActivity
import android.text.TextWatcher
import android.text.Editable
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager


// This class is like a helper for the main screen to display the list of tasks

class TaskAdapter(private val context: MainActivity, private val taskList: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, taskList) {

    // This function decides how each task in the list should look and behave
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        // Get the task for this specific list position
        val currentTask = taskList[position]

        // Connect to the text and checkbox for this task in the visual design
        val taskEditText = listItem!!.findViewById<EditText>(R.id.taskDescription)
        val checkBox = listItem.findViewById<CheckBox>(R.id.taskCheckbox)
        val deleteButton = listItem.findViewById<Button>(R.id.deleteButton)

        // Set the task's text
        taskEditText.setText(currentTask)

        taskEditText.inputType = InputType.TYPE_CLASS_TEXT
        taskEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        taskEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide the keyboard
                val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                // Clear focus from the EditText
                v.clearFocus()

                true // Return true to consume the action
            } else {
                false // Return false to allow other listeners to process the action
            }
        }

        taskEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // No-op
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // Update the taskList with the new content
                    taskList[position] = s.toString()
                }

                override fun afterTextChanged(s: Editable) {
                    // No-op
                }
            },
        )

        // If the checkbox is checked, remove the task from the list
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                taskList.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(context, "Task done", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            taskList.removeAt(position)
            notifyDataSetChanged()
            context.saveTasks() // Save the updated list
            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
        }

        return listItem
    }
}