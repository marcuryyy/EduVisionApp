package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable


@Serializable
data class Survey(
    val id: Int,
    val title: String,
    val created_at: String,
  //  val questionCount: Int
)

class QuizActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quizes)

        val add_quiz_button: Button = findViewById(R.id.add_test_button)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val folder_id: Int = intent.getIntExtra("folder_id", -1)
        println(folder_id)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_classes -> {
                    startActivity(Intent(this, MyClasses::class.java))
                    true
                }
                R.id.nav_folders -> {
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_folders

        recyclerView = findViewById(R.id.my_tests_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val quizes = fetchQuizes(folder_id)
            val adapter = QuizAdapter(quizes, this@QuizActivity)
            recyclerView.adapter = adapter
        }


        add_quiz_button.setOnClickListener {
            val nextIntent = Intent(this, AddQuizActivity::class.java)
            nextIntent.putExtra("folder_id", folder_id)
            startActivity(nextIntent)
        }

    }

    suspend fun fetchQuizes(folder_id: Int?): List<Survey> {
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

            val quizes = response.body<List<Survey>>()
            println(quizes)
            return quizes
        }
        finally {
            client.close()
        }
    }
}

