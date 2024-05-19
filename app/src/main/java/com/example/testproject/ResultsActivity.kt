package com.example.testproject

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity: BaseActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val test_id = intent.getStringExtra("test_id")
        val db_tests = DBtests(this, null)
        val right_answers: List<String> = db_tests.getTestRightAnswer(test_id.toString())
        val students_ans_correct: TextView = findViewById(R.id.StudentsAnsCorrect)
        val students_ans_incorrect: TextView = findViewById(R.id.StudentsAnsIncorrect)

        val keys = intent.getStringArrayListExtra("keys") ?: return
        val values = intent.getStringArrayListExtra("values") ?: return
        val aruco_ids = intent.getStringArrayListExtra("aruco_id") ?: return
        val student_names = intent.getStringArrayListExtra("student_name") ?: return

        val answer_results = keys.zip(values).toMap()
        val student_list_dict: Map<String, String> = aruco_ids.zip(student_names).toMap()

        for (aruco_id in answer_results.keys){
            val student_name = student_list_dict[aruco_id]

            if (answer_results[aruco_id] in right_answers){
                students_ans_correct.setText(students_ans_correct.text.toString() + "\n" + student_name)
            } else {
                students_ans_incorrect.setText(students_ans_incorrect.text.toString() + "\n" + student_name)
            }

        }
    }
}