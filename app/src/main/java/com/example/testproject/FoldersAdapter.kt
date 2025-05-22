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

class FoldersAdapter(
    private var surveys: List<Folder>,
    private val context: Context
) : RecyclerView.Adapter<FoldersAdapter.SurveyViewHolder>() {

    class SurveyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.folder_name)
        val infoButton: ImageButton = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_layout, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val survey = surveys[position]
        holder.titleTextView.text = survey.name

        holder.infoButton.setOnClickListener {
            val intent = Intent(context, QuizActivity::class.java).apply {
                println(survey.id)
                putExtra("folder_id", survey.id)
                putExtra("survey_title", survey.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = surveys.size

}
