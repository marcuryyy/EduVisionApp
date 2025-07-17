package com.example.testproject

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch


class EditClassActivity : BaseActivity() {

    private var currentClassName: String = ""
    private var classId: Int = -1 // ID класса в базе данных

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_class)

        classId = intent.getIntExtra("class_id", -1)
        currentClassName = intent.getStringExtra("class_title") ?: ""

        val classNameEditText: TextInputEditText = findViewById(R.id.classNameEditText)
        val saveButton: Button = findViewById(R.id.saveButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        classNameEditText.setText(currentClassName)

        saveButton.setOnClickListener {
            val newClassName = classNameEditText.text.toString().trim()
            if (newClassName.isEmpty()) {
                classNameEditText.error = "Введите название класса"
            }
            saveClassName(newClassName)
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(classId)
        }
    }

    private fun saveClassName(newClassName: String) {

        if (newClassName == currentClassName) {
            showToast("Название не изменено")
            return
        }

        // Здесь должна быть логика сохранения через API
        // ...
        setResult(RESULT_OK)
        finish()
    }

    private fun showDeleteConfirmationDialog(classId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить класс \"$currentClassName\"? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch { deleteClass(classId) }
            }
            .setNegativeButton("Отмена", null)
            .create()
            .show()
    }

    // Проверить на работоспособность
    private suspend fun deleteClass(classId: Int) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }

        try {
            val response = client.delete("$API_URL/api/classes/$classId") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }


            println(response.bodyAsText())
            println(response.status)
        }

        finally {
            client.close()
        }
        setResult(RESULT_OK)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}