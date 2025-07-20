package com.example.testproject


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UnansweredStudentsAdapter(var studentMap: MutableMap<Int, String>, var context: Context) : RecyclerView.Adapter<UnansweredStudentsAdapter.MyViewUnansweredStudents>() {

    class MyViewUnansweredStudents(view: View): RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewUnansweredStudents {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.unanswered_students_adapter, parent, false)
        return MyViewUnansweredStudents(view)
    }

    override fun onBindViewHolder(holder: MyViewUnansweredStudents, position: Int) {
        val key = studentMap.keys.elementAt(position)
        val value = studentMap[key]

        holder.itemView.findViewById<TextView>(R.id.student_name).text = value.toString().replace(" ", "\n")
    }

    override fun getItemCount(): Int {
        return studentMap.size
    }
    fun updateData(newMap: MutableMap<Int, String>) {
        this.studentMap = newMap
        notifyDataSetChanged()
        println("Adapter data updated")
    }


}