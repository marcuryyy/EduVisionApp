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

class SurveysAdapter(
    private var surveys: List<Survey>,
    private val context: Context
) : RecyclerView.Adapter<SurveysAdapter.SurveyViewHolder>() {

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
        holder.titleTextView.text = survey.title

        holder.infoButton.setOnClickListener {
            val intent = Intent(context, MyTestsActivity::class.java).apply {
                putExtra("survey_title", survey.title)
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
