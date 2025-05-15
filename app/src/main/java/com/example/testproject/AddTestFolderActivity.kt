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

@Serializable
data class AddSurveyRequest(
        val user_id: Long,
        val title: String,
        val questions: List<Question>
        )

@Serializable
data class Question(
    val text: String,
    val correct_option: Int,
    val options: List<String>
)


class AddTestFolderActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_folder)

        val folderName: EditText = findViewById(R.id.folder_name)
        val add_button: Button = findViewById(R.id.button_create_folder)
        val back_button: TextView = findViewById(R.id.backButton)

        add_button.setOnClickListener {
            val folder_name: String = folderName.text.toString()
            if(folder_name != "") {
                lifecycleScope.launch {
                    val survey_titles = fetchSurveyNames()
                    if (folder_name in survey_titles) {
                        Toast.makeText(this@AddTestFolderActivity, "Такая папка уже существует!", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        println("Putting title to db ---")
                        println(folder_name)
                        putSurvey(folder_name)
                    }
                }
            }
            else Toast.makeText(this, "Нет названия папки!", Toast.LENGTH_LONG).show()
        }

        back_button.setOnClickListener{
            onBackPressed()
        }
    }

    suspend fun putSurvey(title: String){
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
                setBody(AddSurveyRequest(user_id, title, emptyList()))
                headers{
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Response after put survey in db")
            println(response.status)
            println(response.bodyAsText())
        }
        finally {
            client.close()
            val nextIntent = Intent(this, MyFoldersActivity::class.java)
            startActivity(nextIntent)
        }
    }


    suspend fun fetchSurveyNames(): List<String> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

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
            var titles: List<String> = surveys.map { it.title }

            println("Getting titles for check ---")
            println(titles)

            return titles
        }
        finally {
            client.close()
        }
    }

}