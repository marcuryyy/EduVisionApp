package com.example.testproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast


class AddTestFolderActivity : BaseActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_folder)
        val folderName: EditText = findViewById(R.id.folder_name)
        val add_button: Button = findViewById(R.id.button_create_folder)
        val back_button: TextView = findViewById(R.id.backButton)
        add_button.setOnClickListener {
            val folder_name: String = folderName.text.toString()
            if(folder_name != "") {
                val db = DBfolders(this, null)
                if(db.findFolder(folder_name)){
                    Toast.makeText(this, "Такая папка уже существует!", Toast.LENGTH_SHORT).show()
                }
                else {
                    db.addFolder(FolderCreator(folder_name))
                    val intent = Intent(this, MyFoldersActivity::class.java)
                    startActivity(intent)
                }
            } else Toast.makeText(this, "Нет названия папки!", Toast.LENGTH_LONG).show()
        }

        back_button.setOnClickListener{
            onBackPressed()
        }
    }

}