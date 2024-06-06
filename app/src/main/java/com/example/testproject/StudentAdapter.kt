package com.example.testproject

import NotificationPopup
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class StudentAdapter(var StudentInAdapter: MutableList<String>, var context: Context) : RecyclerView.Adapter<StudentAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var student_name: TextView = view.findViewById(R.id.student_name)
        val delete_button: ImageButton = view.findViewById(R.id.del_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return StudentInAdapter.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        val student_name = StudentInAdapter[position].split(":")[1]
        val sliced_name = student_name.substring(0, student_name.length-2).trim()
        holder.student_name.text = StudentInAdapter[position]
        holder.delete_button.setOnClickListener{
            println(position)
            val notificationPopup = NotificationPopup(context) {
                val db = DBstudent(context, null)
                val writeable_db = db.writableDatabase
                writeable_db.delete("students", "name = ?", arrayOf(sliced_name))
                StudentInAdapter.removeAt(position)
                notifyDataSetChanged()
            }
            notificationPopup.show()
        }
    }

}
