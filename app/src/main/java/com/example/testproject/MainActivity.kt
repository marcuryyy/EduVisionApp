package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userLogin: EditText = findViewById(R.id.editName)
        val userPass: EditText = findViewById(R.id.editPassword)
        val ButtonEndReg: Button = findViewById(R.id.button_reg)
        val linkToAuth: TextView = findViewById(R.id.to_auth)
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        if(intent.getStringExtra("Source")!="Authentication") {
            if (sharedPref.getString("username", "") != "") {
                val intent = Intent(this, MyClasses::class.java)
                startActivity(intent)
            }
        }
        val editor = sharedPref.edit()

        linkToAuth.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
        ButtonEndReg.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPass.text.toString().trim()

            if(login == "" || password == ""){
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(R.layout.wrong_registration_layout)
                bottomSheetDialog.show()
            }
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
