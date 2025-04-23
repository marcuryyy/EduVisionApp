package com.example.testproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

class AuthActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val userLogin: EditText = findViewById(R.id.editName_auth)
        val userPass: EditText = findViewById(R.id.editPassword_auth)
        val ButtonEndAuth: Button = findViewById(R.id.button_auth)
        val linkToReg: TextView = findViewById(R.id.to_reg)
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
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
                editor.remove("authorized")
                editor.putBoolean("authorized", true)
                editor.apply()
                runBlocking { // Создает блокирующую корутину
                    AuthUser( // Вызов suspend-функции
                        apiUrl = "https://araka-project.onrender.com",
                        loginOrEmail = login,
                        password = password
                    )
                }
              //  val db = DBuser(this, null)
//                val isAuth = db.getUser(login, password)
//                if(isAuth) {
//                    Toast.makeText(this, "Успешно!", Toast.LENGTH_LONG).show()
//                    userLogin.text.clear()
//                    userPass.text.clear()
//
//                    val intent = Intent(this, MyClasses::class.java)
//                    intent.putExtra("SOURCE", "RegistrationActivity")
//                    startActivity(intent)
//                } else
//                    bottomSheetDialog.show()


            }

        }
    }
}

@Serializable
data class AuthRequest(
    val loginOrEmail: String,
    val password: String
)

suspend fun AuthUser(apiUrl: String, loginOrEmail: String, password: String) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json() // Включаем JSON-сериализацию
        }
    }

    try {
        println("$apiUrl/auth/register")
        val response = client.post("$apiUrl/auth/register/") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(loginOrEmail, password)) // Используем data class
        }
        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")

    } finally {
        client.close()
    }


}
