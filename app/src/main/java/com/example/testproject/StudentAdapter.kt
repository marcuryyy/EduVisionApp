package com.example.testproject

import NotificationPopup
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class StudentAdapter(var students: MutableList<GetStudent>, var context: Context) : RecyclerView.Adapter<StudentAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var student_name: TextView = view.findViewById(R.id.student_name)
        val delete_button: ImageButton = view.findViewById(R.id.del_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return students.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        holder.student_name.text = students[position].name
        holder.delete_button.setOnClickListener{
            println(position) // debug
            val notificationPopup = NotificationPopup(context) {
                students.removeAt(position)
                // delete student from class WIP
            }
            notificationPopup.show()
        }
    }
}
