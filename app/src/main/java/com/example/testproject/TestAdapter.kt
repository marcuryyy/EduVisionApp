package com.example.testproject

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testproject.R


class TestAdapter(var TestsInAdapter: List<String>, var context: Context) : RecyclerView.Adapter<TestAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var test_name: TextView = view.findViewById(R.id.test_name)
        val btn: Button = view.findViewById(R.id.test_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.test_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return TestsInAdapter.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        holder.test_name.text = TestsInAdapter[position]

        holder.btn.setOnClickListener {
            val intent = Intent(context, CheckQuestionActivity::class.java)
            context.startActivity(intent)
        }
    }

}