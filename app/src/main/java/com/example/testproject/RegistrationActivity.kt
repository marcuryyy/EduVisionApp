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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope


@Serializable
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

@Serializable
data class RegisterRequest(
    val login: String,
    val email: String,
    val password: String
)

@Serializable
data class VerificationRequest(val email: String)



class RegistrationActivity : BaseActivity()  {
    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 101)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), 101)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registration)

        val userEmail: EditText = findViewById(R.id.editEmail)
        val userLogin: EditText = findViewById(R.id.editName)
        val userPass: EditText = findViewById(R.id.editPassword)


        val ButtonEndReg: Button = findViewById(R.id.button_reg)
        val linkToAuth: TextView = findViewById(R.id.to_auth)

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        if(intent.getStringExtra("Source")!="Authentication") {
            if ((sharedPref.getString("username", "") != "") && (sharedPref.getBoolean("authorized", false) == true)) {
                val intent = Intent(this, UserClassesActivity::class.java)
                startActivity(intent)
            }
        }

        val editor = sharedPref.edit()
        requestInternetPermission()

        linkToAuth.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        ButtonEndReg.setOnClickListener {
            val login = userLogin.text.toString().trim() // 123
            val password = userPass.text.toString().trim() //123aA123
            val email = userEmail.text.toString().trim() // a@a.com

            println(email)
            println(password)
            println(login)

            if(login == "" || password == "" || email == ""){
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(R.layout.wrong_registration_layout)
                bottomSheetDialog.show()
            }
            else {
                if(validateEmail(email)) {
                    editor.putString("username", login)
                    editor.putString("email", email)
                    editor.apply()

                    lifecycleScope.launch { // Создает блокирующую корутину
                        registerUser( // Вызов suspend-функции
                            login = login,
                            email = email,
                            password = password,
                        )
                    }
                }
                else {
                    Toast.makeText(this, "Введите корректный адрес почты!", Toast.LENGTH_SHORT).show()
                }

            }


        }

    }
    // https://eduvision.na4u.ru/api




    fun validateEmail(email: String): Boolean {
        val regex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        return regex.matches(email)
    }


    suspend fun registerUser(
        login: String,
        email: String,
        password: String
    ) {

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json() // Включаем JSON-сериализацию
            }
        }

        try {
            println("$API_URL/auth/register")

            val response = client.post("$API_URL/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(login, email, password))
            }

            println("Status: ${response.status}")
            println("Response: ${response.bodyAsText()}")
            if(response.status.isSuccess()){

                lifecycleScope.launch {
                    sendVerificationCode(
                        context = this@RegistrationActivity,
                        email = email,
                    )
                }
            }

        } finally {
            client.close()
        }
    }


    // Функция перехода на страницу подтверждения
    suspend fun sendVerificationCode(context: Context, apiUrl: String, email: String) {
        // Проверка на пустую почту
        if (email.isBlank()) {
            println("Пожалуйста, заполните почту")
            return
        }

        val intent = Intent(context, CodeConfirmActivity::class.java)
        intent.putExtra("source", "registration")
        context.startActivity(intent)
    }
}





