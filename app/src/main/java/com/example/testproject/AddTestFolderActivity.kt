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
data class AddFolderRequest(
        val name: String,
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
                    val survey_titles = fetchFolderNames()
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
            val response = client.post("https://eduvision.na4u.ru/api/api/folders") {
                contentType(ContentType.Application.Json)
                setBody(AddFolderRequest(title))
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


    suspend fun fetchFolderNames(): List<String> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://eduvision.na4u.ru/api/api/folders") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            println(response.bodyAsText())
            val folders = response.body<List<Folder>>()
            var titles: List<String> = folders.map { it.name }

            println("Getting titles for check ---")
            println(titles)

            return titles
        }
        finally {
            client.close()
        }
    }

}