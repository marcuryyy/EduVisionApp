package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val accountExit: Button = findViewById(R.id.exitFromAccount)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_classes -> {
                    startActivity(Intent(this, UserClassesActivity::class.java))
                    true
                }
                R.id.nav_folders -> {
                    startActivity(Intent(this, UserLibrary::class.java))
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
        }

        // Чтобы текущий пункт был выделен
        bottomNav.selectedItemId = R.id.nav_settings

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        accountExit.setOnClickListener {
            editor.remove("authorized")
            editor.putBoolean("authorized", false)
            editor.apply()
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)

        }

    }
}