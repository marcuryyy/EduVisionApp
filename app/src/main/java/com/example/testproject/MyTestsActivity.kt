package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MyTestsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TestAdapter
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    @Serializable
    data class Option(
        val id: Int,
        val text: String,
        val isCorrect: Boolean
    )

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
    data class TestResponse(
        val id: Int,
        val title: String,
        val createdAt: String,
        val questions: List<Question>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tests)

        val add_test_button: Button = findViewById(R.id.add_test_button)
        val runAllTestsButton: Button = findViewById(R.id.runAllTestsButton)
        val quiz_id: Int = intent.getIntExtra("quiz_id", -1)
        val quiz_name: String = intent.getStringExtra("survey_title").toString()

        recyclerView = findViewById(R.id.my_tests_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadTestsFromApi(quiz_id)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_classes -> {
                    startActivity(Intent(this, MyClasses::class.java))
                    true
                }
                R.id.nav_folders -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_folders

        add_test_button.setOnClickListener {
            val intent = Intent(this, AddTestsActivity::class.java)
            intent.putExtra("quiz_id", quiz_id)
            intent.putExtra("survey_title", quiz_name)
            startActivity(intent)
        }

        runAllTestsButton.setOnClickListener {
            // TODO
        }
    }

    private fun loadTestsFromApi(quizId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val questions = fetchQuestionsFromApi(quizId)
                val questionTexts = questions.map { it.text }
                adapter = TestAdapter(ArrayList(questionTexts), this@MyTestsActivity)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                Log.e("MyTestsActivity", "Error loading tests: ${e.message}")

            }
        }
    }

    private suspend fun fetchQuestionsFromApi(quizId: Int): List<Question> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        return withContext(Dispatchers.IO) {
            val response = client.get("https://araka-project.onrender.com/api/surveys/$quizId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            val testResponse = response.body<TestResponse>()

            testResponse.questions
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.close()
    }
}