package com.example.testproject

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val accountExit: Button = findViewById(R.id.exitFromAccount)
        val classes_button: ImageButton = findViewById(R.id.my_classes_button_settings)
        val folders_button: ImageButton = findViewById(R.id.my_tests_button_settings)

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        accountExit.setOnClickListener {
            editor.remove("authorized")
            editor.putBoolean("authorized", false)
            editor.apply()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        folders_button.setOnClickListener{
            val intent = Intent(this, MyFoldersActivity::class.java)
            startActivity(intent)
        }

        classes_button.setOnClickListener{
            val intent = Intent(this, MyClasses::class.java)
            startActivity(intent)
        }

    }
}