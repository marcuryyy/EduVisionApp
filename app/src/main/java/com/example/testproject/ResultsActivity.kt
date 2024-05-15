package com.example.testproject

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val students_ans_correct: TextView = findViewById(R.id.StudentsAnsCorrect)
        val students_ans_incorrect: TextView = findViewById(R.id.StudentsAnsIncorrect)

        val keys = intent.getStringArrayListExtra("keys") ?: return
        val values = intent.getStringArrayListExtra("values") ?: return
        val id_map = keys.zip(values).toMap()

        val answer_results: Map<String, String> = mapOf("1" to "up") // получаем после сканирования камеры
        val student_list_dict: Map<String, String> = mapOf("1" to "John Doe") // брать из базы
        val right_answers: List<String> = listOf("up","down") // брать из базы

        for (name in answer_results.keys){
            val student_id = student_list_dict[name]
            if (student_id in answer_results.keys) {
                if (answer_results[student_id] in right_answers){
                    students_ans_correct.setText(students_ans_correct.text.toString() + "\n" + name)
                } else {
                    students_ans_incorrect.setText(students_ans_incorrect.text.toString() + "\n" + name)
                }
            }
        }
    }
}