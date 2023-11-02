import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.appprojectone.MainActivity
import com.example.appprojectone.R

class TaskAdapter(private val context: MainActivity, private val taskList: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, taskList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItem = convertView
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)

        val currentTask = taskList[position]
        val taskTextView = listItem!!.findViewById<TextView>(R.id.taskDescription)
        val checkBox = listItem.findViewById<CheckBox>(R.id.taskCheckbox)

        taskTextView.text = currentTask

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
