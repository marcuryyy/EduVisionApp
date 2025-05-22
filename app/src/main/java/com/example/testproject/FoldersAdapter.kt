package com.example.testproject

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoldersAdapter(
    private var folders: List<Folder>,
    private val context: Context
) : RecyclerView.Adapter<FoldersAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.folder_name)
        val infoButton: ImageButton = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_layout, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.titleTextView.text = folder.name

        holder.infoButton.setOnClickListener {
            val intent = Intent(context, QuizActivity::class.java).apply {
                println(folder.id)
                putExtra("folder_id", folder.id)
                    //  putExtra("survey_title", folder.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = folders.size

}
