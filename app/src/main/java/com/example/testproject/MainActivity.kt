package com.example.testproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import androidx.lifecycle.ViewModel
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable


class MainActivity : BaseActivity()  {
    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 101)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), 101)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val userLogin: EditText = findViewById(R.id.editName)
        val userPass: EditText = findViewById(R.id.editPassword)
        val ButtonEndReg: Button = findViewById(R.id.button_reg)
        val linkToAuth: TextView = findViewById(R.id.to_auth)
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        if(intent.getStringExtra("Source")!="Authentication") {
            if ((sharedPref.getString("username", "") != "") && (sharedPref.getBoolean("authorized", false) == true)) {
                val intent = Intent(this, MyClasses::class.java)
                startActivity(intent)
            }
        }
        val editor = sharedPref.edit()
        val client = HttpClient(CIO)
        requestInternetPermission()
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
                if(validateEmail(login)) {
                    editor.putString("username", login)
                    editor.apply()
                    val user = User(login, password)

                    val db = DBuser(this, null)
                    db.addUser(user)
                    runBlocking { // Создает блокирующую корутину
                        registerUser( // Вызов suspend-функции
                            apiUrl = "https://araka-project.onrender.com",
                            login = "user123",
                            email = "user@example.com",
                            password = "password123"
                        )
                    }

//                    Toast.makeText(this, "Успешно!", Toast.LENGTH_SHORT).show()
//
//                    val intent = Intent(this, AuthActivity::class.java)
//                    startActivity(intent)
//
//                    userLogin.text.clear()
//                    userPass.text.clear()

                }
                else {
                    Toast.makeText(this, "Введите корректный адрес почты!", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }
    // https://araka-project.onrender.com

}


fun validateEmail(email: String): Boolean {
        val regex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        return regex.matches(email)
    }



@Serializable
data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String
)

suspend fun registerUser(apiUrl: String, login: String, email: String, password: String) {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json() // Включаем JSON-сериализацию
        }
    }

    try {
        println("$apiUrl/auth/register")
        val response = client.post("$apiUrl/auth/register/") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(login, email, password)) // Используем data class
        }
        println("Status: ${response.status}")
        println("Response: ${response.bodyAsText()}")

    } finally {
        client.close()
    }


}


