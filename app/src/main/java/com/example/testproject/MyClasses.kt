package com.example.testproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MyClasses : BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClassesAdapter
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_classes)
        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
        }

        val itemList = fetchDataFromSQLite()
        val add_class_button: Button = findViewById(R.id.add_class)
        val my_tests_button: ImageButton = findViewById(R.id.my_tests_button)
        val settings_button: ImageButton = findViewById(R.id.settings_button_classes)
        recyclerView = findViewById(R.id.ClassList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ClassesAdapter(itemList, this)
        recyclerView.adapter = adapter

        add_class_button.setOnClickListener {
            val intent = Intent(this, AddClassActivity::class.java)
            startActivity(intent)
        }

        my_tests_button.setOnClickListener {
            val intent = Intent(this, MyFoldersActivity::class.java)
            startActivity(intent)
        }

        settings_button.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
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
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
    }



}
