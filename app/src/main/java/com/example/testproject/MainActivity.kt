package com.example.testproject

import android.Manifest
import android.app.Activity
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
                    lifecycleScope.launch { // Создает блокирующую корутину
                        registerUser( // Вызов suspend-функции
                            apiUrl = "https://araka-project.onrender.com",
                            login = login,
                            email = email,
                            password = password,
                            regButton = ButtonEndReg,
                            timer_text = timer_text
                        )
                    }
                }
                else {
                    Toast.makeText(this, "Введите корректный адрес почты!", Toast.LENGTH_SHORT).show()
                }

            }

            confirmation_field.visibility = View.VISIBLE // Показываем поле для ввода кода
            confirm_button.setOnClickListener {
                val code = confirmation_field.editText?.text.toString().trim()

                if (code.isBlank()) {
                    Toast.makeText(this, "Введите код подтверждения", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    verifyCode(
                        context = this@MainActivity,
                        apiUrl = "https://araka-project.onrender.com",
                        email = email,
                        code = code
                    )
                }
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

    suspend fun registerUser(
        timer_text: TextView,
        regButton: Button,
        apiUrl: String,
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
            println("$apiUrl/auth/register")

            val response = client.post("$apiUrl/auth/register/") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(login, email, password)) // Используем data class
            }

            println("Status: ${response.status}")
            println("Response: ${response.bodyAsText()}")
            if(response.status.isSuccess()){
                change_reg_button_state(regButton)

                lifecycleScope.launch {
                    sendVerificationCode(
                        apiUrl = "https://araka-project.onrender.com",
                        email = email,
                        startTimer = startResendTimer(timer_text, regButton)
                    )
                }
            }

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
    fun startResendTimer(timer_text: TextView, regButton: Button): () -> Unit {
        var timeLeft = 7

        timer_text.visibility = View.VISIBLE
        return {
            lifecycleScope.launch {
                while (timeLeft > 0) {
                    delay(1000)
                    timeLeft--
                    timer_text.text = "Осталось: $timeLeft сек."
                }
                // Вызываем функцию, когда таймер завершится
                timer_text.visibility = View.INVISIBLE
                change_reg_button_state(regButton)
            }
        }
    }


    fun change_reg_button_state(regButton: Button): Unit {
        if(regButton.isEnabled){
            regButton.isEnabled = false
            regButton.alpha = 0.5f
        }
        else{
            regButton.isEnabled = true
            regButton.alpha = 1.0f
        }

    }

}
    @Serializable
    data class VerifyCodeRequest(
        val email: String,
        val code: String
    )

    // Функция проверки кода
    suspend fun verifyCode(
        context: Context,
        apiUrl: String,
        email: String,
        code: String,
    ) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) { json() }
        }

        try {
            val response = client.post("$apiUrl/auth/registration/verify-code") {
                contentType(ContentType.Application.Json)
                setBody(VerifyCodeRequest(email, code))
            }

            if (response.status.isSuccess()) {
                // Переход на AuthActivity в UI-потоке
                withContext(Dispatchers.Main) {
                    val intent = Intent(context, AuthActivity::class.java)
                    client.close()
                    context.startActivity(intent)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Неверный код подтверждения", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } finally {
            client.close()
        }
    }




