package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val loginOrEmail: String,
    val password: String
)

@Serializable
data class ResetPassRequest(
    val loginOrEmail: String
)

class AuthActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val userLoginOrEmail: EditText = findViewById(R.id.editName_auth)
        val userPass: EditText = findViewById(R.id.editPassword_auth)

        val ButtonEndAuth: Button = findViewById(R.id.button_auth)
        val linkToReg: TextView = findViewById(R.id.to_reg)
        val forgotPassBtn: TextView = findViewById(R.id.forgot_pass_btn)

        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        linkToReg.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("Source", "Authentication")
            startActivity(intent)
        }

        ButtonEndAuth.setOnClickListener {
            val login = userLoginOrEmail.text.toString().trim()
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

        forgotPassBtn.setOnClickListener{ // also should move to CodeConfirmActivity
            lifecycleScope.launch { sendResetPassRequest(
                apiUrl = "https://araka-project.onrender.com",
                loginOrEmail = userLoginOrEmail.text.toString().trim(),
                startTimer = startResendTimer(timer_text, requestNewCodeButton) // should start in CodeConfirmActivity
            ) }

        }
    }


    suspend fun sendResetPassRequest(apiUrl: String, loginOrEmail: String, startTimer: () -> Unit) {

        if (loginOrEmail.isBlank()) {
            println("Пожалуйста, заполните почту/логин")
            return
        }

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.post("$apiUrl/auth/login/request-password-reset") {
                contentType(ContentType.Application.Json)
                setBody(ResetPassRequest(loginOrEmail))
            }

            println("Код отправлен: ${response.bodyAsText()}")

            startTimer()

        } catch (e: Exception) {
            println("Ошибка при отправке кода: ${e.message}")
        } finally {
            client.close()
        }
    }

    fun startResendTimer(timer_text: TextView, requestNewCodeButton: Button): () -> Unit {
        var timeLeft = 7

        timer_text.visibility = View.VISIBLE
        return {

            lifecycleScope.launch {
                while (timeLeft > 0) {
                    delay(1000)
                    timeLeft--
                    timer_text.text = "Осталось: $timeLeft сек."
                }

                timer_text.visibility = View.INVISIBLE
                changeActiveState(requestNewCodeButton) // not implemented
            }
        }
    }

    suspend fun AuthUser(apiUrl: String, loginOrEmail: String, password: String) {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json() // Включаем JSON-сериализацию
            }
        }

        try {
            println("$apiUrl/auth/login")
            val response = client.post("$apiUrl/auth/login/") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(loginOrEmail, password)) // Используем data class
            }
            println("Status: ${response.status}")
            println("Response: ${response.bodyAsText()}")

        } finally {
            client.close()
        }

    }
}




