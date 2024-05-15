package com.example.testproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testproject.R


class AddTestsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tests)
        val testNameText: EditText = findViewById(R.id.test_name)
        val add_button: Button = findViewById(R.id.button_create_test)


        add_button.setOnClickListener {
            val testName: String = testNameText.text.toString()
            if(testName != "") {
                val db = DBtests(this, null)
                db.addTest(TestCreator(testName))
                val intent = Intent(this, MyTestsActivity::class.java)
                startActivity(intent)
            } else Toast.makeText(this, "Нет названия теста!", Toast.LENGTH_LONG).show()
        }
    }
}