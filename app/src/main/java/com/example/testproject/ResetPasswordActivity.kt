package com.example.testproject

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var newPasswordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Правильная инициализация с корректными ID
        newPasswordLayout = findViewById(R.id.new_password_field) // Layout
        confirmPasswordLayout = findViewById(R.id.confirm_password_field) // Layout
        newPasswordInput = findViewById(R.id.new_password_edit_text) // EditText внутри Layout
        confirmPasswordInput = findViewById(R.id.confirm_password_edit_text) // EditText внутри Layout
        saveButton = findViewById(R.id.confirm_button)

        saveButton.setOnClickListener {
            resetPassword()
        }
    }

    // Остальной код без изменений
    private fun resetPassword() {
        val newPassword = newPasswordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // Валидация
        if (newPassword.isEmpty()) {
            newPasswordLayout.error = "Введите новый пароль"
            return
        } else {
            newPasswordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Подтвердите пароль"
            return
        } else {
            confirmPasswordLayout.error = null
        }

        if (newPassword != confirmPassword) {
            confirmPasswordLayout.error = "Пароли не совпадают"
            return
        } else {
            confirmPasswordLayout.error = null
        }

        if (newPassword.length < 8) {
            newPasswordLayout.error = "Пароль должен содержать минимум 8 символов"
            return
        }

        val email = intent.getStringExtra("email") ?: ""
        val code = intent.getStringExtra("code") ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = sendResetPasswordRequest(email, code, newPassword)
                println(response)
                withContext(Dispatchers.Main) {
                    if (response == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Пароль успешно изменён",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Ошибка при изменении пароля",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Ошибка сети: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun sendResetPasswordRequest(
        email: String,
        code: String,
        newPassword: String
    ): Int {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        return try {
            println("Пароль: ${newPassword}")
            println("Email: ${email}")
            println("Code: ${code}")
            val response: HttpResponse = client.post("https://eduvision.na4u.ru/api/api/auth/login/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(ResetPasswordRequest(email, code, newPassword))
            }

            println("Ответ сервера: ${response.bodyAsText()}") // Логирование ответа
            response.status.value
        } catch (e: Exception) {
            println("Ошибка при сбросе пароля: ${e.message}")
            -1 // Возвращаем -1 в случае ошибки
        } finally {
            client.close()
        }
    }

    // DTO для запроса
    @Serializable
    data class ResetPasswordRequest(
        val email: String,
        val code: String,
        val newPassword: String
    )
}
