package com.example.testproject

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ClassInfoPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_class_info)

        val list_view: ListView = findViewById(R.id.listView)
        val student_name: EditText = findViewById(R.id.addStudentCell)
        val student_id: EditText = findViewById(R.id.addStudentIDcell)
        val button: Button = findViewById(R.id.addStudentButton)


        val student_list: MutableList<String> =  mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, student_list)
        list_view.adapter = adapter

        button.setOnClickListener{
            val text = student_name.text.toString().trim() + " " + student_id.text.toString().trim()
            if(text != "")
                adapter.insert(text,0)
        }
        }

    }
