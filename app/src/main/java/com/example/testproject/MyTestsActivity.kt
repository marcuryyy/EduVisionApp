package com.example.testproject


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyTestsActivity : BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tests)

        val my_classes_link: ImageButton = findViewById(R.id.my_classes_button)
        val add_test_button: ImageButton = findViewById(R.id.add_test_button)
        val folder_name = intent.getStringExtra("folder_name").toString()
        val folder_db = DBfolders(this, null)
        val returned_bundle: Bundle = folder_db.getFolderId(folder_name)
        val folder_id: String = returned_bundle.getString("folder_id").toString()
        val folders_from_database = fetchDataFromSQLite(folder_id)
        val folder_btn: ImageButton = findViewById(R.id.my_tests_button)
        println(folder_id)
        folder_db.close()
        recyclerView = findViewById(R.id.my_tests_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TestAdapter(folders_from_database, this)
        recyclerView.adapter = adapter

        my_classes_link.setOnClickListener {
            val intent = Intent(this, MyClasses::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        add_test_button.setOnClickListener {
            val intent = Intent(this, AddTestsActivity::class.java)
            intent.putExtra("folder_name", folder_name)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        folder_btn.setOnClickListener{
            val intent = Intent(this, MyFoldersActivity::class.java)
            startActivity(intent)
        }


    }

    private fun fetchDataFromSQLite(folder_name:String): MutableList<String> {

        val db = DBtests(this, null)
        val readableDB = db.readableDatabase
        val cursor = readableDB.rawQuery("SELECT * FROM tests WHERE folder_id = '$folder_name'", null)

        val items = mutableListOf<String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val question_text = cursor.getString(cursor.getColumnIndexOrThrow("question_text"))
                items.add(question_text)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }
}