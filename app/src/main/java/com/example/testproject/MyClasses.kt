package com.example.testproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testproject.MyFoldersActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data class Class(
    val id: Int,
    val title: String
)


class MyClasses : BaseActivity()  {

    private lateinit var recyclerView: RecyclerView
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_classes)
        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
        }


        recyclerView = findViewById(R.id.ClassList)
        recyclerView.layoutManager = LinearLayoutManager(this)


        lifecycleScope.launch {
            val classes = fetchClasses()
            val adapter = ClassesAdapter(classes, this@MyClasses)
            recyclerView.adapter = adapter
        }

        val add_class_button: Button = findViewById(R.id.add_class)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_classes -> {
                    true
                }
                R.id.nav_folders -> {
                    startActivity(Intent(this, MyFoldersActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_classes

        add_class_button.setOnClickListener {
            val intent = Intent(this, AddClassActivity::class.java)
            startActivity(intent)
        }

        
    }

    suspend fun fetchClasses(): List<Class> {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.get("https://araka-project.onrender.com/api/classes/user/my") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            println(response.bodyAsText())


            val classes = response.body<List<Class>>()

            return classes
        }
        finally {
            client.close()
        }

    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
    }



}
