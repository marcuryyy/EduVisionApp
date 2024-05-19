package com.example.testproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testproject.R

class MyTestsActivity : BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tests)

        val itemList = fetchDataFromSQLite()
        val my_classes_link: ImageButton = findViewById(R.id.my_classes_button)
        val add_test_button: ImageButton = findViewById(R.id.add_test_button)

        recyclerView = findViewById(R.id.my_tests_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TestAdapter(itemList, this)
        recyclerView.adapter = adapter

        my_classes_link.setOnClickListener {
            val intent = Intent(this, MyClasses::class.java)
            startActivity(intent)
        }
        add_test_button.setOnClickListener {
            val intent = Intent(this, AddTestsActivity::class.java)
            startActivity(intent)
        }


    }

    private fun fetchDataFromSQLite(): List<String> {
        val db = DBtests(this, null)
        val readableDB = db.readableDatabase
        val cursor = readableDB.rawQuery("SELECT * FROM tests", null)

        val items = mutableListOf<String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val question_text_index = cursor.getColumnIndex("question_text")
                val question_text = if (question_text_index >= 0) cursor.getString(question_text_index) else ""
                items.add(question_text)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }
}