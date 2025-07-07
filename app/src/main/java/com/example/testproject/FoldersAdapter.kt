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
    private var surveys: List<Survey>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FOLDER = 0
        private const val TYPE_SURVEY = 1
    }

    private val items = mutableListOf<Any>()

    init {
        // Объединяем элементы в один список
        items.addAll(folders)
        items.addAll(surveys)
    }

    // ViewHolder для папки
    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.folder_name)
        val infoButton: ImageButton = view.findViewById(R.id.button)
    }

    // ViewHolder для опроса
    class SurveyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.survey_name)
        val infoButton: ImageButton = view.findViewById(R.id.button)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Folder -> TYPE_FOLDER
            is Survey -> TYPE_SURVEY
            else -> throw IllegalArgumentException("Invalid type of data at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FOLDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.folder_layout, parent, false)
                FolderViewHolder(view)
            }
            TYPE_SURVEY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.unfoldered_survey_layout, parent, false)
                SurveyViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FolderViewHolder -> {
                val folder = items[position] as Folder
                holder.titleTextView.text = folder.name
                holder.infoButton.setOnClickListener {
                    val intent = Intent(context, UserQuizzesActivity::class.java).apply {
                        println(folder.id)
                        putExtra("folder_id", folder.id)
                    }
                    context.startActivity(intent)
                }
            }
            is SurveyViewHolder -> {
                val survey = items[position] as Survey
                holder.titleTextView.text = survey.title
                // Обработка клика на опрос
                holder.itemView.setOnClickListener {
                    // Открытие активности для опроса
                    val intent = Intent(context, UserTestQuestions::class.java).apply {
                        putExtra("quiz_id", survey.id)
                        putExtra("survey_title", survey.title)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

}