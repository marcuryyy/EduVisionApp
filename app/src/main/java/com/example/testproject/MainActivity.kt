package com.example.testproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout


class MainActivity : BaseActivity()  {
    private fun requestInternetPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 101)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), 101)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 101)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val userEmail: EditText = findViewById(R.id.editEmail)
        val userLogin: EditText = findViewById(R.id.editName)
        val userPass: EditText = findViewById(R.id.editPassword)
        val confirmation_field: TextInputLayout = findViewById(R.id.confirmationCodeLayout)
        val confirm_button: Button = findViewById(R.id.button_confirm_code)
        val timer_text: TextView = findViewById(R.id.timer_text)


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
                    editor.apply()
                    val user = User(login, password)

                    val db = DBuser(this, null)
                    db.addUser(user)
                    runBlocking { // Создает блокирующую корутину
                        registerUser( // Вызов suspend-функции
                            apiUrl = "https://araka-project.onrender.com",
                            login = login,
                            email = email,
                            password = password
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

            confirmation_field.visibility = View.VISIBLE // Показываем поле для ввода кода

        }

        confirm_button.setOnClickListener {
            change_confirmation_button_state(confirm_button)
            val email = userEmail.text.toString().trim()

            lifecycleScope.launch {
                sendVerificationCode(
                    apiUrl = "https://araka-project.onrender.com",
                    email = email,
                    startTimer = { startResendTimer(timer_text, confirm_button) } // possible problems
                )
            }

        }

    }
    // https://araka-project.onrender.com




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

    @Serializable
    data class VerificationRequest(val email: String)


    // Функция отправки кода подтверждения
    suspend fun sendVerificationCode(
        apiUrl: String,
        email: String,
        startTimer: () -> Unit
    ) {
        // Проверка на пустую почту
        if (email.isBlank()) {
            println("Пожалуйста, заполните почту")
            return
        }

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.post("$apiUrl/registration/send-code") {
                contentType(ContentType.Application.Json)
                setBody(VerificationRequest(email))
            }

            println("Код отправлен: ${response.bodyAsText()}")

            startTimer() // Запускаем таймер

        } catch (e: Exception) {
            println("Ошибка при отправке кода: ${e.message}")
        } finally {
            client.close()
        }
    }


    // Функция таймера (60 секунд)
    fun startResendTimer(timer_text: TextView, confirm_button: Button): () -> Unit {
        var timeLeft = 60
        return {
            // Периодический цикл, который будет уменьшать timeLeft каждую секунду
            while (timeLeft > 0) {
                // Пауза на 1 секунду (имитация, без асинхронности)
                Thread.sleep(1000)  // Блокирует текущий поток на 1 секунду
                timeLeft--
                timer_text.text = "Осталось: $timeLeft сек."
            }
            // Вызываем функцию, когда таймер завершится
            change_confirmation_button_state(confirm_button)
        }
    }


    fun change_confirmation_button_state(confirm_button: Button): Unit {
        if(confirm_button.isEnabled){
            confirm_button.isEnabled = false
            confirm_button.alpha = 0.5f
        }
        else{
            confirm_button.isEnabled = true
            confirm_button.alpha = 1.0f
        }

    }

}




