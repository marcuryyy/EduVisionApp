package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
//@Serializable
//data class Question(
//    val text: String,
//    val correct_option: Int,
//    val options: List<String>
//)
@Serializable
data class AddQuizRequest(
    val user_id: Int,
    val title: String,
    val questions: List<Question>
)




class AddQuizActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_quiz)

        val quiz_textbox: EditText = findViewById(R.id.quiz_name)
        val add_button: Button = findViewById(R.id.button_create_quiz)
        val back_button: TextView = findViewById(R.id.backButton)
        val folder_id = intent.getIntExtra("folder_id", -1)
        add_button.setOnClickListener {
        val quiz_name: String = quiz_textbox.text.toString()
        if(quiz_name != "") {
            lifecycleScope.launch {
                val survey_titles = fetchQuizNames(folder_id)
                if (quiz_name in survey_titles) {
                    Toast.makeText(this@AddQuizActivity, "Такой опрос уже существует!", Toast.LENGTH_SHORT).show()
                }
                else {
                    putQuiz(quiz_name, folder_id)
                }
            }
        }
            else Toast.makeText(this, "Нет названия опроса!", Toast.LENGTH_LONG).show()
        }

        back_button.setOnClickListener{
            onBackPressed()
        }
    }

    suspend fun putQuiz(title: String, folder_id: Int){
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val user_id = sharedPref.getLong("user_id", -1)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.post("https://araka-project.onrender.com/api/surveys") {
                contentType(ContentType.Application.Json)
                setBody(AddQuizRequest(user_id.toInt(), title, emptyList()))
                headers{
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Response after put quiz in db")
            println(response.status)
            println(response.bodyAsText())
        }
        finally {
            client.close()
            val nextIntent = Intent(this, QuizActivity::class.java)
            nextIntent.putExtra("folder_id", folder_id)
            startActivity(nextIntent)
        }
    }


    suspend fun fetchQuizNames(folder_id: Int): List<String> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://araka-project.onrender.com/api/folders/$folder_id/surveys") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            val quizes = response.body<List<Folder>>()
            var titles: List<String> = quizes.map { it.name }

            println("Getting titles for check ---")
            println(titles)

            return titles
        }
        finally {
            client.close()
        }
    }

}