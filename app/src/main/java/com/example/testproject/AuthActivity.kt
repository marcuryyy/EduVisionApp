package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class AuthActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val userLogin: EditText = findViewById(R.id.editName_auth)
        val userPass: EditText = findViewById(R.id.editPassword_auth)
        val ButtonEndAuth: Button = findViewById(R.id.button_auth)
        val linkToReg: TextView = findViewById(R.id.to_reg)

        linkToReg.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("Source", "Authentication")
            startActivity(intent)
        }

        ButtonEndAuth.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPass.text.toString().trim()
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(R.layout.wrong_auth_layout)
            if(login == "" || password == "")
                bottomSheetDialog.show()
            else {
                val db = DBuser(this, null)
                val isAuth = db.getUser(login, password)
                if(isAuth) {
                    Toast.makeText(this, "Успешно!", Toast.LENGTH_LONG).show()
                    userLogin.text.clear()
                    userPass.text.clear()

                    val intent = Intent(this, MyClasses::class.java)
                    intent.putExtra("SOURCE", "RegistrationActivity")
                    startActivity(intent)
                } else
                    bottomSheetDialog.show()


            }

        }
    }
}