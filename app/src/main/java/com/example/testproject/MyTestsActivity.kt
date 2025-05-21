package com.example.testproject


import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView


class MyTestsActivity : BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tests)

        val add_test_button: Button = findViewById(R.id.add_test_button)
        val folder_name = intent.getStringExtra("folder_name").toString()
        val folder_db = DBfolders(this, null)
        val returned_bundle: Bundle = folder_db.getFolderId(folder_name)
        val folder_id: String = returned_bundle.getString("folder_id").toString()
        val folders_from_database = fetchDataFromSQLite(folder_id)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_classes -> {
                    startActivity(Intent(this, MyClasses::class.java))
                    true
                }
                R.id.nav_folders -> {
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Чтобы текущий пункт был выделен
        bottomNav.selectedItemId = R.id.nav_folders
        val runAllTestsButton: Button = findViewById(R.id.runAllTestsButton)
        folder_db.close()
        recyclerView = findViewById(R.id.my_tests_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TestAdapter(folders_from_database, this)
        recyclerView.adapter = adapter


        add_test_button.setOnClickListener {
            val intent = Intent(this, AddTestsActivity::class.java)
            intent.putExtra("folder_name", folder_name)
            startActivity(intent)
        }


        runAllTestsButton.setOnClickListener{
            val intent = Intent(this, SelectClassActivity::class.java)
            intent.putExtra("allTests", true)
            intent.putStringArrayListExtra("questionsArray", folders_from_database)
            startActivity(intent)
        }


    }

    private fun fetchDataFromSQLite(folder_name:String): ArrayList<String> {

        val db = DBtests(this, null)
        val readableDB = db.readableDatabase
        val cursor = readableDB.rawQuery("SELECT * FROM tests WHERE folder_id = '$folder_name'", null)

        val items = ArrayList<String>()
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