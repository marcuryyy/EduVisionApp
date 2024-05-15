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


class AddClassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_class)
        val className: EditText = findViewById(R.id.class_name)
        val add_button: Button = findViewById(R.id.button_create_class)

        add_button.setOnClickListener {
            val class_label: String = className.text.toString()
            if(class_label != "") {
                val db = DBclass(this, null)
                db.addClass(ClassCreator(class_label))
                val intent = Intent(this, MyClasses::class.java)
                startActivity(intent)
            } else Toast.makeText(this, "Нет названия класса!", Toast.LENGTH_LONG).show()
        }
    }
}