package com.example.testproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class FoldersAdapter(var FoldersInAdapter: List<String>, var context: Context) : RecyclerView.Adapter<FoldersAdapter.MyViewFolder>() {

    class MyViewFolder(view: View): RecyclerView.ViewHolder(view) {
        var folder_name: TextView = view.findViewById(R.id.folder_name)
        val info_button: ImageButton = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewFolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.folder_layout, parent, false)
        return MyViewFolder(view)
    }

    override fun getItemCount(): Int {
        return FoldersInAdapter.count()
    }

    override fun onBindViewHolder(holder: MyViewFolder, position: Int) {
        holder.folder_name.text = FoldersInAdapter[position]
        holder.info_button.setOnClickListener{
            val intent = Intent(context, MyTestsActivity::class.java)
            intent.putExtra("folder_name", FoldersInAdapter[position])
            if (context is Activity){
                val activity = context as Activity
                activity.startActivity(intent)
            }
        }
    }

}