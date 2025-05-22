package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
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
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Int,
    val text: String,
    val correct_option: Int,
    val file_url: String?,
    val file_folder: String?,
    val file_name: String?,
    val file_type: String?,
    val options: List<String>
)

@Serializable
data class QuestionToAdd(
    val text: String,
    val correct_option: Int,
    val file_url: String?,
    val file_folder: String?,
    val file_name: String?,
    val file_type: String?,
    val options: List<String>
)

@Serializable
data class TestResponse(
    val id: Int,
    val title: String,
    val createdAt: String,
    val questions: List<Question>
)

@Serializable
data class AddQuestionRequest(
    val user_id: Int,
    val title: String,
    val questions: List<QuestionToAdd>
)
class AddTestsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tests)
        val testNameText: EditText = findViewById(R.id.test_name)
        val add_button: Button = findViewById(R.id.button_create_test)
        var right_answer: Int = 0
        val checkbox_one: CheckBox = findViewById(R.id.checkBoxVar1)
        val checkbox_two: CheckBox = findViewById(R.id.checkBoxVar2)
        val checkbox_three: CheckBox = findViewById(R.id.checkBoxVar3)
        val checkbox_four: CheckBox = findViewById(R.id.checkBoxVar4)
        val option_one: EditText = findViewById(R.id.variant1)
        val option_two: EditText = findViewById(R.id.variant2)
        val option_three: EditText = findViewById(R.id.variant3)
        val option_four: EditText = findViewById(R.id.variant4)
        val back_button: TextView = findViewById(R.id.backButton)
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val user_id = sharedPref.getLong("user_id", -1)
        val quiz_id = intent.getIntExtra("quiz_id", -1)
        add_button.setOnClickListener {
            val testName: String = testNameText.text.toString()
            if (testName != "") {
                if (checkbox_one.isChecked) {
                    right_answer = 0
                } else if (checkbox_two.isChecked) {
                    right_answer = 1
                } else if (checkbox_three.isChecked) {
                    right_answer = 2
                } else if (checkbox_four.isChecked) {
                    right_answer = 3
                }
                val option_one_text: String = option_one.text.toString()
                val option_two_text: String = option_two.text.toString()
                val option_three_text: String = option_three.text.toString()
                val option_four_text: String = option_four.text.toString()
                val options: List<String> =
                    listOf(option_one_text, option_two_text, option_three_text, option_four_text)
                lifecycleScope.launch {
                    //  val question_amount: Int = fetchTestNames(quiz_id)
                    val question_to_create = QuestionToAdd(
                        //   question_amount + 1,
                        testName,
                        right_answer,
                        "",
                        "",
                        "",
                        "",
                        options
                    )
                    putQuestion(quiz_id, testName, user_id.toInt(), listOf(question_to_create))

                }

                val db = DBtests(this, null)

                val intent = Intent(this, MyTestsActivity::class.java)
                startActivity(intent)
            } else Toast.makeText(this, "Нет названия теста!", Toast.LENGTH_LONG).show()
        }

        back_button.setOnClickListener {
            onBackPressed()
        }

    }

    suspend fun putQuestion(
        quiz_id: Int,
        title: String,
        user_id: Int,
        questions: List<QuestionToAdd>
    ) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            println(questions)
            val response = client.put("https://araka-project.onrender.com/api/surveys/$quiz_id") {
                contentType(ContentType.Application.Json)
                setBody(AddQuestionRequest(user_id, title, questions))
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Response after put question in db")
            println(response.status)
            println(response.bodyAsText())
        } finally {
            client.close()
            val nextIntent = Intent(this, MyClasses::class.java)
            startActivity(nextIntent)
        }
    }

}