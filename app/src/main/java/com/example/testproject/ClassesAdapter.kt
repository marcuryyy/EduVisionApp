package com.example.testproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testproject.R


class ClassesAdapter(var ClassesInAdapter: List<String>, var context: Context) : RecyclerView.Adapter<ClassesAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var class_lbl: TextView = view.findViewById(R.id.class_letter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.class_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return ClassesInAdapter.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        holder.class_lbl.text = ClassesInAdapter[position]
    }

}