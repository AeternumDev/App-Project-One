package com.example.appprojectone

import android.os.Bundle
import android.renderscript.ScriptGroup.Input
import android.text.InputType
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.appprojectone.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var lvtodolist: ListView
    private lateinit var fab: FloatingActionButton
    private lateinit var shoppingItems: ArrayList<String>
    private lateinit var itemadapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lvtodolist = findViewById(R.id.lvtodolist)
        fab = findViewById(R.id.floatingActionButton)
        shoppingItems = ArrayList()

        shoppingItems.add("Apfel")
        shoppingItems.add("Birne")

        itemadapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shoppingItems)
        lvtodolist.adapter = itemadapter

        // Set an item long click listener for deletion
        lvtodolist.setOnItemLongClickListener { _, _, position, _ ->
            // AlertDialog for confirmation on deletion
            AlertDialog.Builder(this).apply {
                setTitle("Delete Item")
                setMessage("You are about to delete an item. Are you sure?")
                setPositiveButton("Yes") { dialog, _ ->
                    shoppingItems.removeAt(position) // remove the item from the list
                    itemadapter.notifyDataSetChanged() // refresh the adapter
                    Toast.makeText(this@MainActivity, "Item deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()

            true // indicates that the long click was consumed
        }



        fab.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Add a ToDo Item")

            var input = EditText(this)
            input.hint = "create something wonderful"
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            builder.setPositiveButton("Lets go!") { dialog, which ->
                shoppingItems.add(input.text.toString())
                itemadapter.notifyDataSetChanged()
            }

            builder.setNegativeButton("No :(") { dialog, which ->
                dialog.cancel()

            }
            builder.show()

        }

    }
}



