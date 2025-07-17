package com.example.testproject

import NotificationPopup
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch


class StudentAdapter(
    var students: MutableList<GetStudent>,
    private val context: Context,
    private val onStudentDeleted: suspend (Int) -> Unit
) : RecyclerView.Adapter<StudentAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val arucoNum: TextView = view.findViewById(R.id.aruco_num)
        val studentName: TextView = view.findViewById(R.id.student_name)
        val deleteButton: ImageButton = view.findViewById(R.id.del_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = students.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val student = students[holder.adapterPosition]
        holder.studentName.text = student.name
        holder.arucoNum.text = student.aruco_num.toString()
        holder.deleteButton.setOnClickListener {
            val popup = NotificationPopup(context) {
                val arucoNum = student.aruco_num
                students.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
                notifyItemRangeChanged(holder.adapterPosition, itemCount - holder.adapterPosition)

                (context as? ClassInfoActivity)?.lifecycleScope?.launch {
                    onStudentDeleted(arucoNum)
                }
            }
            popup.show()
        }

    }
}

