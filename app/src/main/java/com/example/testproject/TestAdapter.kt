package com.example.testproject

import NotificationPopup
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TestAdapter(var TestsInAdapter: MutableList<String>, var context: Context) : RecyclerView.Adapter<TestAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var questionName: TextView = view.findViewById(R.id.survey_name)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.question_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return TestsInAdapter.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        holder.questionName.text = TestsInAdapter[position]
//        val test_id: String = returned_bundle.getString("test_id").toString()
        holder.deleteButton.setOnClickListener{
            val notificationPopup = NotificationPopup(context) {
                // Удаление теста из папки через API
                // ...
                notifyDataSetChanged()
            }
            notificationPopup.show()
        }
    }

}