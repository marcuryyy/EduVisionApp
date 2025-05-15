package com.example.testproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Survey(
    val id: Int,
    val title: String,
    val createdAt: String,
    val questionCount: Int
)

@Serializable
data class SurveysRequest(
    val Authorization: String
)


class MyFoldersActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_folders)

        val add_folder_button: ImageButton = findViewById(R.id.add_folder_button)
        val my_classes_button: ImageButton = findViewById(R.id.my_classes_button)
        val settings_button: ImageButton = findViewById(R.id.settings_button_folder)

        recyclerView = findViewById(R.id.my_folders_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val surveys = fetchDataFromAPI()
            val adapter = SurveysAdapter(surveys, this@MyFoldersActivity)
            recyclerView.adapter = adapter
        }


        add_folder_button.setOnClickListener {
            val nextIntent = Intent(this, AddTestFolderActivity::class.java)
            startActivity(nextIntent)
        }

        my_classes_button.setOnClickListener {
            startActivity(Intent(this, MyClasses::class.java))
        }

        settings_button.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    suspend fun fetchDataFromAPI(): List<Survey> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")
        println("---------")
        println(token)

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://araka-project.onrender.com/api/surveys/user/my") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            val surveys = response.body<List<Survey>>()

            return surveys
        }
        finally {
            client.close()
        }
    }
}

