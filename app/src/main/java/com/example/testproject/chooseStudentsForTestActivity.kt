package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testproject.SelectClassForQuizActivity.StartSessionResponse
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
data class StudentToChoose(
    val aruco_num: Int,
    val name: String,
    var isSelected: Boolean
)


class chooseStudentsForTestActivity : BaseActivity() {
    private lateinit var adapter: ChooseStudentsAdapter
    private lateinit var selectAllCheckBox: CheckBox
    private lateinit var studentsRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_students_for_test)

        selectAllCheckBox = findViewById(R.id.selectAllCheckBox)
        val confirmButton: TextView = findViewById(R.id.confirmButton)
        val class_id = intent.getIntExtra("class_id", -1)
        val quiz_id = intent.getIntExtra("quiz_id", -1)
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        studentsRecyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val students = fetchStudentsFromClass(class_id)
            setupAdapter(students)
        }

        selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (selectAllCheckBox.isPressed) {
                adapter.setAllStudentsSelected(isChecked)
            }
        }

        confirmButton.setOnClickListener {
            val selectedStudents = adapter.students.filter { it.isSelected == true }
            if (selectedStudents.isEmpty()) {
                Toast.makeText(this, "Выберите хотя бы одного студента", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val response = startSession(quiz_id, class_id)
                    println(selectedStudents.map{ it.name })
                    val intent = Intent(this@chooseStudentsForTestActivity, CheckQuestionActivity::class.java).apply {
                        putExtras(Bundle().apply {
                            putStringArrayList(
                                "students",
                                ArrayList(selectedStudents.map { it.name } ?: emptyList()))
                            putIntegerArrayList(
                                "aruco_ids",
                                ArrayList(selectedStudents.map { it.aruco_num } ?: emptyList()))
                            putInt("quiz_id", quiz_id)
                            putInt("class_id", class_id)
                            putInt("taken_survey_id", response.data.taken_survey_id)
                            putInt("taken_question_id", response.data.taken_question_id)
                            putString("question_text", response.data.question_text)
                        })
                    }
                    startActivity(intent)
            }

            }
        }
    }

    private fun setupAdapter(students: MutableList<StudentToChoose>) {
        adapter = ChooseStudentsAdapter(students) {
            updateSelectAllCheckBox()
        }
        studentsRecyclerView.adapter = adapter
    }

    private fun updateSelectAllCheckBox() {
        val allSelected = adapter.areAllStudentsSelected()

        selectAllCheckBox.setOnCheckedChangeListener(null)
        selectAllCheckBox.isChecked = allSelected

        selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (selectAllCheckBox.isPressed) {
                adapter.setAllStudentsSelected(isChecked)
            }
        }
    }

    suspend fun fetchStudentsFromClass(classId: Int): MutableList<StudentToChoose> {
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

            return students.map { student ->
                StudentToChoose(
                    name = student.name,
                    aruco_num = student.aruco_num,
                    isSelected = false
                )
            }.toMutableList()
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
            val response = client.post("$API_URL/api/conducting/start") {
                contentType(ContentType.Application.Json)
                setBody(SessionInfo(quiz_id, class_id))
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            println(response)
            response.body<StartSessionResponse>()
        } finally {
            client.close()
        }
    }

}