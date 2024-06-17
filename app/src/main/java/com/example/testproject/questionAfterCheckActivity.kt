package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
class questionAfterCheckActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_after_check)
        val questionList: ListView = findViewById(R.id.questionList)
        val all_question: ArrayList<String> =
            intent.getStringArrayListExtra("questions") ?: ArrayList()
        val adapterQuestions = ArrayAdapter(this, android.R.layout.simple_list_item_1, all_question)
        val aruco_ids = intent.getStringArrayListExtra("aruco_id")
        val student_names = intent.getStringArrayListExtra("student_name")
        val bundle: Bundle? = intent.extras
        val questionsResults: ArrayList<Map<String, String>>? = bundle?.getSerializable("questionResults") as? ArrayList<Map<String, String>>

        questionList.adapter = adapterQuestions
        val test_db =  DBtests(this, null)

        questionList.setOnItemClickListener{adapterView, view, i, l ->
            val test_id = test_db.getTestId(all_question[i])
            val ids: ArrayList<String> = ArrayList(questionsResults?.get(i)?.keys?.toList() ?: listOf())
            val answers: ArrayList<String> = ArrayList(questionsResults?.get(i)?.values?.toList() ?: listOf())
            val intent = Intent(this, ResultsActivity::class.java)
            intent.putStringArrayListExtra("keys", ids)
            intent.putStringArrayListExtra("values", answers)
            intent.putStringArrayListExtra("aruco_id", aruco_ids)
            intent.putStringArrayListExtra("student_name", student_names)
            intent.putExtra("test_id", test_id.getString("test_id").toString())
            startActivity(intent)
        }
    }
}