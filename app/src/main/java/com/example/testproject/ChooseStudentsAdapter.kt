package com.example.testproject

import NotificationPopup
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch


class ChooseStudentsAdapter(
    var students: MutableList<StudentToChoose>,
    private val onSelectionChanged: () -> Unit // Добавляем callback
) : RecyclerView.Adapter<ChooseStudentsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val arucoNum: TextView = view.findViewById(R.id.aruco_num)
        val studentName: TextView = view.findViewById(R.id.studentNameTextView)
        val studentCheckBox: CheckBox = view.findViewById(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student_checkbox_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = students.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val student = students[position]
        holder.studentName.text = student.name
        holder.arucoNum.text = student.aruco_num.toString()
        holder.studentCheckBox.isChecked = student.isSelected

        // Удаляем предыдущий слушатель, чтобы избежать множественных вызовов
        holder.studentCheckBox.setOnCheckedChangeListener(null)
        holder.studentCheckBox.isChecked = student.isSelected

        holder.studentCheckBox.setOnCheckedChangeListener { _, isChecked ->
            student.isSelected = isChecked
            onSelectionChanged() // Уведомляем об изменении
        }

        holder.itemView.setOnClickListener {
            holder.studentCheckBox.toggle()
        }
    }

    fun setAllStudentsSelected(selected: Boolean) {
        students.forEach { it.isSelected = selected }
        notifyDataSetChanged()
    }

    fun areAllStudentsSelected(): Boolean {
        return students.all { it.isSelected }
    }

}

