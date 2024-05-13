package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val userLogin: EditText = findViewById(R.id.editName)
        val userPass: EditText = findViewById(R.id.editPassword)
        val ButtonEndReg: Button = findViewById(R.id.button_reg)
        val linkToAuth: Button = findViewById(R.id.to_auth)
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        if(sharedPref.getString("username", "") != ""){
            val intent = Intent(this, MyClasses::class.java)
            startActivity(intent)
        }
        val editor = sharedPref.edit()

        linkToAuth.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
        ButtonEndReg.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPass.text.toString().trim()

            if(login == "" || password == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show()
            else {
                editor.putString("username", login)
                editor.apply()
                val user = User(login, password)

                val db = DBuser(this, null)
                db.addUser(user)
                Toast.makeText(this, "Успешно!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)

                userLogin.text.clear()
                userPass.text.clear()

            }

        }
        }
    }
