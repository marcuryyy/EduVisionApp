package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text


class AddClassActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_class)
        val className: EditText = findViewById(R.id.class_name)
        val add_button: Button = findViewById(R.id.button_create_class)
        val back_button: TextView = findViewById(R.id.backButton)

        add_button.setOnClickListener {
            val class_label: String = className.text.toString()
            if(class_label != "") {
                val db = DBclass(this, null)
                if(db.findClass(class_label)){
                    Toast.makeText(this, "Такой класс уже существует!", Toast.LENGTH_SHORT).show()
                }
                else {
                    db.addClass(ClassCreator(class_label))
                    val intent = Intent(this, MyClasses::class.java)
                    startActivity(intent)
                }
            } else Toast.makeText(this, "Нет названия класса!", Toast.LENGTH_LONG).show()
        }

        back_button.setOnClickListener{
            onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

}