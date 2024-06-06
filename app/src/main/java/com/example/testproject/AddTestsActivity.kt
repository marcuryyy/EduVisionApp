package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class AddTestsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tests)
        val testNameText: EditText = findViewById(R.id.test_name)
        val add_button: Button = findViewById(R.id.button_create_test)
        var right_answer: String = ""
        val checkbox_one: CheckBox = findViewById(R.id.checkBoxVar1)
        val checkbox_two: CheckBox = findViewById(R.id.checkBoxVar2)
        val checkbox_three: CheckBox = findViewById(R.id.checkBoxVar3)
        val checkbox_four: CheckBox = findViewById(R.id.checkBoxVar4)
        val back_button: TextView = findViewById(R.id.backButton)
        val folder_name = intent.getStringExtra("folder_name").toString()
        add_button.setOnClickListener {
            val testName: String = testNameText.text.toString()
            if(testName != "") {
                if(checkbox_one.isChecked){
                    right_answer = "up"
                }
                else if(checkbox_two.isChecked){
                    right_answer = "right"
                }
                else if(checkbox_three.isChecked){
                    right_answer = "down"
                }
                else if(checkbox_four.isChecked){
                    right_answer = "left"
                }

                val db = DBtests(this, null)
                db.addTest(TestCreator(folder_name, testName, right_answer))
                val intent = Intent(this, MyTestsActivity::class.java)
                intent.putExtra("folder_name", folder_name)
                startActivity(intent)
            } else Toast.makeText(this, "Нет названия теста!", Toast.LENGTH_LONG).show()
        }

        back_button.setOnClickListener{
            onBackPressed()
        }

    }
}