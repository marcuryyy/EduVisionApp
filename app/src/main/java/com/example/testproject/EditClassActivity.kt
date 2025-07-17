package com.example.testproject

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText


class EditClassActivity : AppCompatActivity() {

    private var currentClassName: String = ""
    private var classId: Long = -1 // ID класса в базе данных

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_class)

        // Получаем данные из Intent
        classId = intent.getLongExtra("class_id", -1)
        currentClassName = intent.getStringExtra("class_title") ?: ""

        val classNameEditText: TextInputEditText = findViewById(R.id.classNameEditText)
        val saveButton: Button = findViewById(R.id.saveButton)
        val deleteButton: Button = findViewById(R.id.deleteButton)

        // Устанавливаем текущее название класса в поле ввода
        classNameEditText.setText(currentClassName)

        // Обработчик кнопки сохранения
        saveButton.setOnClickListener {
            val newClassName = classNameEditText.text.toString().trim()
            if (newClassName.isEmpty()) {
                classNameEditText.error = "Введите название класса"
            }
            saveClassName(newClassName)
        }

        // Обработчик кнопки удаления
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun saveClassName(newClassName: String) {
        // Проверка, изменилось ли название
        if (newClassName == currentClassName) {
            showToast("Название не изменено")
            return
        }

        // Здесь должна быть логика сохранения через API
        // ...
        setResult(RESULT_OK)
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить класс \"$currentClassName\"? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ ->
                deleteClass()
            }
            .setNegativeButton("Отмена", null)
            .create()
            .show()
    }

    private fun deleteClass() {
        // Здесь должна быть логика удаления через API
        // ...
        setResult(RESULT_OK)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}