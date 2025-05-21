package com.example.testproject

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data class Student(
    val aruco_num: Int,
    val name: String
)


class ClassInfoPage : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_info)

        val student_name_label: EditText = findViewById(R.id.addStudentCell)
        val student_id_label: EditText = findViewById(R.id.addStudentIDcell)
        val button: Button = findViewById(R.id.addStudentButton)
        val class_title: TextView = findViewById(R.id.class_title)

        val class_name = intent.getStringExtra("class_name")
        val class_id: Int = intent.getIntExtra("class_id", -1)

        class_title.text = "Ученики " + class_name

        val list_view: RecyclerView = findViewById(R.id.listView)
        list_view.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val students = fetchStudentsFromClass(class_id)
            val adapter = StudentAdapter(students, this@ClassInfoPage)
            list_view.adapter = adapter
        }


        button.setOnClickListener{
            // add student func WIP
        }
    }

    suspend fun fetchStudentsFromClass(classId: Int): MutableList<Student> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://araka-project.onrender.com/api/students/${classId}") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Response after get students in db")
            println(response.status)
            println(response.bodyAsText())


            val students = response.body<MutableList<Student>>()

            return students
        }
        finally {
            client.close()
        }
    }
}
