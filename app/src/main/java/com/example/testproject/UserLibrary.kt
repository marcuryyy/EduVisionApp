package com.example.testproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

@Serializable
data class Folder(
    val id: Int,
    val user_id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val surveys: List<Survey>
)


class UserLibrary : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerContainer: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_library)

        shimmerContainer = findViewById(R.id.shimmer_container)
        shimmerContainer.startShimmer() // Запускаем анимаци



        lifecycleScope.launch {
            val folders = fetchFoldersFromAPI()
            val surveys = fetchSurveysFromAPI()

            showContent(folders, surveys)
        }

    }

    suspend fun fetchFoldersFromAPI(): List<Folder> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")
        println("---------")
        println(token)

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("$API_URL/api/folders") {

                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }


            val folders = response.body<List<Folder>>()
            return folders
        }
        finally {
            client.close()
        }
    }


    suspend fun fetchSurveysFromAPI(): List<Survey> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")
        println("---------")
        println(token)

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("$API_URL/api/folders/unfoldered/surveys") {

                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }


            val surveys = response.body<List<Survey>>()
            return surveys
        }
        finally {
            client.close()
        }
    }


    private fun showContent(folders: List<Folder>, surveys:List<Survey>){
        setContentView(R.layout.activity_user_quiz_folders)

        val add_folder_button: Button = findViewById(R.id.add_folder_button)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_classes -> {
                    startActivity(Intent(this, UserClassesActivity::class.java))
                    true
                }
                R.id.nav_folders -> {
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_folders

        recyclerView = findViewById(R.id.my_folders_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = FoldersAdapter(folders, surveys, this@UserLibrary)
        recyclerView.adapter = adapter

        add_folder_button.setOnClickListener {
            val nextIntent = Intent(this, AddQuizFolderActivity::class.java)
            startActivity(nextIntent)
        }
    }
}

