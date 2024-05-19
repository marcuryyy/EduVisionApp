package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

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
            startActivity(intent)
        }

        ButtonEndAuth.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPass.text.toString().trim()
            if(login == "" || password == "")
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, "Не все поля заполнены либо нет такого пользователя!", Toast.LENGTH_LONG).show()


            }

        }
    }
}