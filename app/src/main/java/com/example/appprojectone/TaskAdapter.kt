import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.appprojectone.MainActivity
import com.example.appprojectone.R

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
        val taskTextView = listItem!!.findViewById<TextView>(R.id.taskDescription)
        val checkBox = listItem.findViewById<CheckBox>(R.id.taskCheckbox)

        // Set the task's text
        taskTextView.text = currentTask

        // If the checkbox is checked, remove the task from the list
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                taskList.removeAt(position)
                notifyDataSetChanged()
                Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }

        return listItem
    }
}