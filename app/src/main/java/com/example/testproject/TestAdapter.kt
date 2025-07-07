package com.example.testproject

import NotificationPopup
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TestAdapter(var TestsInAdapter: MutableList<String>, var context: Context) : RecyclerView.Adapter<TestAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var test_name: TextView = view.findViewById(R.id.test_name)
        val del_btn: ImageButton = view.findViewById(R.id.del_test)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.survey_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return TestsInAdapter.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        holder.test_name.text = TestsInAdapter[position]
        val db = DBtests(context, null)
        val returned_bundle: Bundle = db.getTestId(holder.test_name.text.toString())
        val test_id: String = returned_bundle.getString("test_id").toString()
        holder.del_btn.setOnClickListener{
            val notificationPopup = NotificationPopup(context) {
                val writeable_db = db.writableDatabase
                writeable_db.delete("tests", "question_text = ?", arrayOf(TestsInAdapter[position]))
                TestsInAdapter.removeAt(position)
                notifyDataSetChanged()
            }
            notificationPopup.show()
        }
    }

}