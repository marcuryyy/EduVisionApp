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


class ClassesAdapter(var classes: List<Class>, var context: Context) : RecyclerView.Adapter<ClassesAdapter.MyViewClass>() {

    class MyViewClass(view: View): RecyclerView.ViewHolder(view) {
        var class_title: TextView = view.findViewById(R.id.class_letter)
        val info_button: ImageButton = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewClass {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.class_layout, parent, false)
        return MyViewClass(view)
    }

    override fun onBindViewHolder(holder: MyViewClass, position: Int) {
        val cls = classes[position]
        holder.class_title.text = cls.title

        holder.info_button.setOnClickListener{
            val intent = Intent(context, ClassInfoPage::class.java)
            intent.putExtra("class_title", cls.title)
            intent.putExtra("class_id", cls.id)
            if (context is Activity){
                val activity = context as Activity
                activity.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return classes.size
    }

}