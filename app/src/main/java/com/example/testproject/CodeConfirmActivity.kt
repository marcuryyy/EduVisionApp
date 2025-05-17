package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class CodeConfirmActivity : BaseActivity() {
    private var timerJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_code)

        val confirmation_field: TextInputLayout = findViewById(R.id.confirmation_field)
        val confirm_button: Button = findViewById(R.id.confirm_button)
        val timer_text: TextView = findViewById(R.id.timer_text)
        val resend_code_text: TextView = findViewById(R.id.resend_code_text)
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("email", "").toString()

        println(email)
        // Запускаем таймер сразу
        startResendTimer(timer_text)

        // Обработка повторной отправки кода
        resend_code_text.setOnClickListener {
            if (timer_text.visibility != View.VISIBLE) {
                lifecycleScope.launch {
                    sendVerificationCode(this@CodeConfirmActivity, URL, email)
                }
                Toast.makeText(this, "Код отправлен повторно", Toast.LENGTH_SHORT).show()
                startResendTimer(timer_text)
            }
        }

        confirm_button.setOnClickListener {
            val code = confirmation_field.editText?.text.toString().trim()

            if (code.isBlank()) {
                Toast.makeText(this, "Введите код подтверждения", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                verifyCode(
                    context = this@CodeConfirmActivity,
                    apiUrl = URL,
                    email = email,
                    code = code
                )
            }
        }
    }

    private fun startResendTimer(timer_text: TextView) {
        // Отменяем предыдущий таймер, если он был
        timerJob?.cancel()

        var timeLeft = 60
        timer_text.visibility = View.VISIBLE

        timerJob = lifecycleScope.launch {
            while (timeLeft > 0) {
                timer_text.text = "Повторная отправка через: $timeLeft сек."
                delay(1000)
                timeLeft--
            }
            timer_text.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel() // Важно отменять таймер при уничтожении Activity
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
        val source = intent.getStringExtra("source")
        println(source)
        println(email)

        try {

            if (source == "registration") {

                val response = client.post("$apiUrl/auth/registration/verify-code") {
                    contentType(ContentType.Application.Json)
                    setBody(VerifyCodeRequest(email, code))
                }

                if (response.status.isSuccess()){
                    withContext(Dispatchers.Main) {
                        val intent = Intent(context, AuthActivity::class.java)
                        client.close()
                        context.startActivity(intent)
                    }
                }  else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Неверный код подтверждения", Toast.LENGTH_SHORT).show()
                    }
                }
            }else if (source == "auth_forgot_password") {

                val response = client.post("$apiUrl/auth/login/verify-reset-code") {
                    contentType(ContentType.Application.Json)
                    setBody(VerifyCodeRequest(email, code))
                }
                println(response.bodyAsText())
                if (response.status.isSuccess()) {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(context, ResetPasswordActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("code", code)
                        client.close()
                        context.startActivity(intent)
                    }
                }   else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Неверный код подтверждения", Toast.LENGTH_SHORT).show()
                        }
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
    suspend fun sendVerificationCode(context: Context, apiUrl: String, email: String) {
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

            val response: HttpResponse = client.post("$apiUrl/auth/registration/send-code") {
                contentType(ContentType.Application.Json)
                setBody(VerificationRequest(email))
            }

            println("Код отправлен: ${response.bodyAsText()}")


        } catch (e: Exception) {
            println("Ошибка при отправке кода: ${e.message}")
        } finally {
            client.close()
        }
    }
}