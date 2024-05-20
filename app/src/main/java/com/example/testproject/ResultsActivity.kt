package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView


class ResultsActivity: BaseActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val test_id = intent.getStringExtra("test_id")
        val db_tests = DBtests(this, null)
        val right_answers: List<String> = db_tests.getTestRightAnswer(test_id.toString())
        val students_ans_correct: ListView = findViewById(R.id.StudentsAnsCorrect)
        val students_ans_incorrect: ListView = findViewById(R.id.StudentsAnsIncorrect)
        val button: Button = findViewById(R.id.Button)

        val keys = intent.getStringArrayListExtra("keys") ?: return
        val values = intent.getStringArrayListExtra("values") ?: return
        val aruco_ids = intent.getStringArrayListExtra("aruco_id") ?: return
        val student_names = intent.getStringArrayListExtra("student_name") ?: return

        val correct_students: MutableList<String> = mutableListOf()
        val incorrect_students: MutableList<String> = mutableListOf()

        val answer_results = keys.zip(values).toMap()
        val student_list_dict: Map<String, String> = aruco_ids.zip(student_names).toMap()

        val adapterCorrectAnswers = ArrayAdapter(this, android.R.layout.simple_list_item_1, correct_students)
        students_ans_correct.adapter = adapterCorrectAnswers

        val adapterIncorrectAnswers = ArrayAdapter(this, android.R.layout.simple_list_item_1, incorrect_students)
        students_ans_incorrect.adapter = adapterIncorrectAnswers

        button.setOnClickListener{
            val intent = Intent(this, MyClasses::class.java)
            startActivity(intent)
        }

        for (aruco_id in answer_results.keys){
            val student_name = student_list_dict[aruco_id]
            println(student_name)
            if (answer_results[aruco_id] in right_answers){
                adapterCorrectAnswers.add(student_name.toString())
            } else {
                adapterIncorrectAnswers.add(student_name.toString())
            }


        }
    }
}