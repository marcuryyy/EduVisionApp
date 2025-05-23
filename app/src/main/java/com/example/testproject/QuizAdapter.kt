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

class QuizAdapter(
    private var surveys: List<Survey>,
    private val context: Context
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.folder_name)
        val infoButton: ImageButton = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_layout, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val survey = surveys[position]
        holder.titleTextView.text = survey.title

        holder.infoButton.setOnClickListener {
            val intent = Intent(context, MyTestsActivity::class.java).apply {
                putExtra("survey_title", survey.title)
                putExtra("quiz_id", survey.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = surveys.size

    fun updateData(newSurveys: List<Survey>) {
        surveys = newSurveys
        notifyDataSetChanged()
    }
}
