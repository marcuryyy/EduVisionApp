package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class SelectClassActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_class)
        val list_view: ListView = findViewById(R.id.class_list_for_tests)
        val itemList = fetchDataFromSQLite()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemList)
        val questionList = intent.getStringArrayListExtra("questionsArray")
        val test_id = intent.getStringExtra("test_id")
        list_view.adapter = adapter

        list_view.setOnItemClickListener{adapterView, view, i, l ->
            val bundle = Bundle()
            if (intent.getBooleanExtra("allTests", false) == true) {
                bundle.putBoolean("allTests", true)
            }
            val intent = Intent(this, CheckQuestionActivity::class.java)
            val selectedFromList: String = list_view.getItemAtPosition(i).toString()

            val db = DBclass(this, null)
            val returned_bundle: Bundle = db.getClassId(selectedFromList)
            val class_id: String = returned_bundle.getString("class_id").toString()
            db.close()

            val student_db = DBstudent(this, null)
            val student_list: MutableMap<String, String> = student_db.getStudents(class_id)
            student_db.close()


            bundle.putStringArrayList("aruco_id", ArrayList(student_list.keys))
            bundle.putStringArrayList("student_name", ArrayList(student_list.values.map { it.toString() }))
            bundle.putString("test_id", test_id)
            bundle.putStringArrayList("questionsArray", questionList)


            intent.putExtras(bundle)

            startActivity(intent)
        }
    }

    private fun fetchDataFromSQLite(): List<String> {

        val db = DBclass(this, null)
        val readableDB = db.readableDatabase
        val cursor = readableDB.rawQuery("SELECT * FROM classes", null)

        val items = mutableListOf<String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val class_name_index = cursor.getColumnIndex("class_label")
                val class_name = if (class_name_index >= 0) cursor.getString(class_name_index) else ""
                items.add(class_name)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }
}