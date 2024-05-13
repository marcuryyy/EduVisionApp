package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SelectClassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_class)
        val list_view: ListView = findViewById(R.id.class_list_for_tests)
        val itemList = fetchDataFromSQLite()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemList)
        list_view.adapter = adapter

        list_view.setOnItemClickListener{adapterView, view, i, l ->
            val intent = Intent(this, CheckQuestionActivity::class.java)
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