package com.example.testproject

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data class GetStudent(
    val aruco_num: Int,
    val name: String
)

@Serializable
data class Student(
    val name: String
)

@Serializable
data class AddStudentRequest(
    val class_id: Int,
    val students: List<Student>
)


class ClassInfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_info)

        val student_name_label: EditText = findViewById(R.id.addStudentCell)
        val button: Button = findViewById(R.id.addStudentButton)
        val class_title: TextView = findViewById(R.id.class_title)

        val class_name = intent.getStringExtra("class_title")
        val class_id: Int = intent.getIntExtra("class_id", -1)

        class_title.text = "Ученики " + class_name

        val list_view: RecyclerView = findViewById(R.id.listView)
        list_view.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            list_view.adapter = getStudentList(class_id)
        }


        button.setOnClickListener {
            val studentName = student_name_label.text.toString().trim()

            if (studentName.isEmpty()) {
                // Показываем сообщение об ошибке, если поле пустое
                student_name_label.error = "Введите имя ученика"
                return@setOnClickListener
            }

            val student = listOf<Student>(Student(studentName))
            lifecycleScope.launch {
                student_name_label.text.clear()
                putStudent(class_id, student)
                list_view.adapter = getStudentList(class_id)
            }
        }
    }


    suspend fun getStudentList(class_id: Int): StudentAdapter {
        val students = fetchStudentsFromClass(class_id)
        val adapter = StudentAdapter(students,this@ClassInfoActivity) { arucoNum ->
            deleteStudent(class_id, arucoNum)
        }
        return adapter
    }


    suspend fun putStudent(class_id: Int, students: List<Student>) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        try {
            val response = client.post("$API_URL/api/students") {
                    contentType(ContentType.Application.Json)
                    setBody(AddStudentRequest(class_id, students))
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }

            println(response.bodyAsText())
        }
        finally {
            client.close()
        }
    }


    suspend fun fetchStudentsFromClass(classId: Int): MutableList<GetStudent> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("$API_URL/api/students/${classId}") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Response after get students in db")
            println(response.status)
            println(response.bodyAsText())


            val students = response.body<MutableList<GetStudent>>()

            return students
        }
        finally {
            client.close()
        }
    }


    suspend fun deleteStudent(class_id: Int, aruco_num: Int) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response: HttpResponse = client.delete("$API_URL/api/students/${class_id}/${aruco_num}") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println("Ответ сервера ${response.bodyAsText()}")

        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
        } finally {
            client.close()
        }
    }
}
