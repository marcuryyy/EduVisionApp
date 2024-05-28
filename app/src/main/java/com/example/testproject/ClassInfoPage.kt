package com.example.testproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ClassInfoPage : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_info)

        val list_view: RecyclerView = findViewById(R.id.listView)

        val student_name_label: EditText = findViewById(R.id.addStudentCell)
        val student_id_label: EditText = findViewById(R.id.addStudentIDcell)
        val button: Button = findViewById(R.id.addStudentButton)
        val class_title: TextView = findViewById(R.id.class_title)
        class_title.text = intent.getStringExtra("class_name")
        val class_db = DBclass(this, null)
        val returned_bundle: Bundle = class_db.getClassId(class_title.text.toString())
        val class_id: String = returned_bundle.getString("class_id").toString()
        val class_students = fetchDataFromSQLite(class_id)
        class_db.close()
        list_view.layoutManager = LinearLayoutManager(this)
        val adapter = StudentAdapter(class_students, this)
        list_view.adapter = adapter

        button.setOnClickListener{
            val db = DBstudent(this, null)
            val student_name: String = student_name_label.text.toString().trim()
            val student_id: String = student_id_label.text.toString().trim().trimStart('0')
            if(db.findStudent(student_name, student_id)){
                Toast.makeText(this, "Такой ученик либо id уже существует!", Toast.LENGTH_SHORT).show()
            }
            else {
                db.addStudent(
                    StudentCreator(
                        class_id,
                        student_name,
                        student_id
                    )
                )
                class_students.add("Ученик: " + student_name + "\nID:" + student_id)
                adapter.notifyDataSetChanged()
                student_name_label.text.clear()
                student_id_label.text.clear()

            }
        }

        }
    private fun fetchDataFromSQLite(class_title:String): MutableList<String> {

        val db = DBstudent(this, null)
        val readableDB = db.readableDatabase
        val cursor = readableDB.rawQuery("SELECT * FROM students WHERE class_id = '$class_title'", null)

        val items = mutableListOf<String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("aruco")).toString()
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                items.add("Ученик: " + name + "\nID:" + id)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }
    }
