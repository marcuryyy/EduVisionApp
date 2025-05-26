package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class SessionInfo(
    val survey_id: Int,
    val class_id: Int
)
class SelectClassActivity : BaseActivity() {
    private lateinit var classes: List<Class>
    private lateinit var dbTests: DBtests // Объявляем базу данных
    @Serializable
    data class Question(
        val id: Int,
        val text: String,
        val file_url: String? = null,
        val file_type: String? = null,
        val correct_option_id: Int,
        val options: List<Option>
    )

    @Serializable
    data class Option(
        val id: Int,
        val text: String,
        val isCorrect: Boolean
    )

    @Serializable
    data class TestResponse(
        val id: Int,
        val title: String,
        val createdAt: String,
        val questions: List<Question>
    )

    @Serializable
    data class StartSessionResponse(
        val status: String,
        val data: SessionData
    )

    @Serializable
    data class SessionData(
        val taken_survey_id: Int,
        val taken_question_id: Int,
        val question_id: Int,
        val question_text: String,
        val options: Map<String, String>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_class)

        // Инициализация базы данных
        dbTests = DBtests(this, null)

        val list_view: ListView = findViewById(R.id.class_list_for_tests)
        lifecycleScope.launch {
            classes = fetchClasses()
            val names = classes.map { it.title }
            val adapter = ArrayAdapter(this@SelectClassActivity, android.R.layout.simple_list_item_1, names)
            list_view.adapter = adapter
        }

        val questionList = intent.getStringArrayListExtra("questionsArray")
        val test_id = intent.getStringExtra("test_id")
        val quiz_id = intent.getIntExtra("quiz_id", -1)

        list_view.setOnItemClickListener { adapterView, view, i, l ->
            val selectedClass = classes[i]
            val classId = selectedClass.id

            lifecycleScope.launch {
                // 1. Запускаем сессию и получаем данные
                val sessionResponse = startSession(quiz_id, classId)
                val takenSurveyId = sessionResponse.data.taken_survey_id
                val takenQuestionId = sessionResponse.data.taken_question_id
                val question_text = sessionResponse.data.question_text

                val questions = fetchTestQuestions(quiz_id)
                val (names, arucoIds) = fetchStudents(classId)

                // 2. Сохраняем вопросы в БД
                saveQuestionsToDatabase(questions)

                // 3. Создаём Intent с новыми параметрами
                val intent = Intent(this@SelectClassActivity, CheckQuestionActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putBoolean("allTests", intent.getBooleanExtra("allTests", true))
                        putString("test_id", test_id)
                        putStringArrayList("questionsArray", questionList)
                        putStringArrayList("students", ArrayList(names ?: emptyList()))
                        putIntegerArrayList("aruco_ids", ArrayList(arucoIds ?: emptyList()))
                        putInt("quiz_id", quiz_id)
                        putInt("class_id", classId)
                        putInt("taken_survey_id", takenSurveyId)
                        putInt("taken_question_id", takenQuestionId)
                        putString("question_text", question_text)
                    })
                }

                // 4. Запускаем активность
                startActivity(intent)
            }
        }
    }

    private suspend fun fetchTestQuestions(quizId: Int): List<Question> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

            try {
                val response = client.get("https://eduvision.na4u.ru/api/api/surveys/$quizId") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
                return response.body<TestResponse>().questions
            } finally {
                client.close()
            }

    }

    private fun saveQuestionsToDatabase(questions: List<Question>) {
        dbTests.clearQuestions()

        questions.forEach { question ->
            dbTests.addQuestion(
                question.text,
                question.correct_option_id.toString(),
                question.id
            )
        }
    }


    suspend fun fetchClasses(): List<Class> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://eduvision.na4u.ru/api/api/classes/user/my") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }


            val classes = response.body<List<Class>>()
            return classes
        }
        finally {
            client.close()
        }

    }

    suspend fun startSession(quiz_id: Int, class_id: Int): StartSessionResponse {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        return try {
            val response = client.post("https://eduvision.na4u.ru/api/api/conducting/start") {
                contentType(ContentType.Application.Json)
                setBody(SessionInfo(quiz_id, class_id))
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            // Парсим ответ
            response.body<StartSessionResponse>()
        } finally {
            client.close()
        }
    }

    suspend fun fetchStudents(class_id: Int): Pair<List<String>, List<Int>> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://eduvision.na4u.ru/api/api/students/$class_id") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }


            val students = response.body<List<GetStudent>>()
            val names = students.map{ it.name }
            val aruco_ids = students.map{ it.aruco_num }

            return Pair(names, aruco_ids)
        }
        finally {
            client.close()
        }

    }

}
