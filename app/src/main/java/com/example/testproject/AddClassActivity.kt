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
data class AddClassRequest(
    val title: String,
)


class AddClassActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_class)
        val className: EditText = findViewById(R.id.class_name)
        val add_button: Button = findViewById(R.id.button_create_class)
        val back_button: TextView = findViewById(R.id.backButton)

        add_button.setOnClickListener {
            val className: String = className.text.toString()
            if(className != "") {
                lifecycleScope.launch {
                    val classNames = fetchClassNames()
                    if(className in classNames){
                        Toast.makeText(this@AddClassActivity, "Такой класс уже существует!", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        println("Putting class to db ---")
                        println(className)
                        putClass(className)
                    }
                }
            }
            else Toast.makeText(this, "Нет названия класса!", Toast.LENGTH_LONG).show()

        }

        back_button.setOnClickListener{
            onBackPressed()
        }
    }


    suspend fun putClass(title: String) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.post("https://eduvision.na4u.ru/api/api/classes") {
                contentType(ContentType.Application.Json)
                setBody(AddClassRequest(title))
                headers{
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Response after put class in db")
            println(response.status)
            println(response.bodyAsText())
        }
        finally {
            client.close()
            val nextIntent = Intent(this, MyClasses::class.java)
            startActivity(nextIntent)
        }
    }

    suspend fun fetchClassNames(): List<String> {
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
            var classNames: List<String> = classes.map { it.title }

            println("Getting class names for check ---")
            println(classNames)

            return classNames
        }
        finally {
            client.close()
        }
    }

}