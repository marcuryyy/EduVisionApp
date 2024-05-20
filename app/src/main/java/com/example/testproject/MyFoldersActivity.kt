package com.example.testproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MyFoldersActivity : BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoldersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_folders)

        val itemList = fetchDataFromSQLite()
        val add_folder_button: ImageButton = findViewById(R.id.add_folder_button)
        val my_classes_button: ImageButton = findViewById(R.id.my_classes_button)

        recyclerView = findViewById(R.id.my_folders_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FoldersAdapter(itemList, this)
        recyclerView.adapter = adapter

        add_folder_button.setOnClickListener {
            val intent = Intent(this, AddTestFolderActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        my_classes_button.setOnClickListener {
            val intent = Intent(this, MyClasses::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

    private fun fetchDataFromSQLite(): List<String> {
        val db = DBfolders(this, null)
        val readableDB = db.readableDatabase
        val cursor = readableDB.rawQuery("SELECT * FROM folders", null)

        val items = mutableListOf<String>()
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val folder_name_index = cursor.getColumnIndex("folder_name")
                val folder_name = if (folder_name_index >= 0) cursor.getString(folder_name_index) else ""
                items.add(folder_name)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }



}
