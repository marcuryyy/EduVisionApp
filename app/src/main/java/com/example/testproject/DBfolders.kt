package com.example.testproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle

class DBfolders(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "FoldersStorage", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE folders (id INTEGER PRIMARY KEY AUTOINCREMENT, folder_name TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS folders")
        onCreate(db)
    }

    fun addFolder(folder_example: FolderCreator){
        val values = ContentValues()
        values.put("folder_name", folder_example.folder_name)

        val db = this.writableDatabase
        db.insert("folders", null, values)

        db.close()
    }
    fun getFolderId(folder_title: String): Bundle {
        val db = this.readableDatabase
        val question_info = Bundle()
        val result = db.rawQuery("SELECT * FROM folders WHERE folder_name = '$folder_title'", null)
        if (result.moveToFirst()){
            question_info.putString("folder_id", result.getString(result.getColumnIndexOrThrow("folder_name")).toString())
        }
        return question_info
    }

    fun findFolder(folder_title: String): Boolean {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM folders WHERE folder_name='$folder_title'", null)
        return result.moveToFirst()
    }
}