package com.example.testproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import java.sql.SQLData

class DBclass(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "ClassStorage", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE classes (id INTEGER PRIMARY KEY AUTOINCREMENT, class_label TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS classes")
        onCreate(db)
    }

    fun addClass(class_example: ClassCreator){
        val values = ContentValues()
        values.put("class_label", class_example.class_label)

        val db = this.writableDatabase
        db.insert("classes", null, values)

        db.close()
    }
    fun getClassId(class_title: String): Bundle {
        val db = this.readableDatabase
        val student_info = Bundle()
        val result = db.rawQuery("SELECT * FROM classes WHERE class_label = '$class_title'", null)
        if (result.moveToFirst()){
            student_info.putString("class_id", result.getInt(result.getColumnIndexOrThrow("id")).toString())
        }
        return student_info
    }

    fun findClass(class_title: String): Boolean {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM classes WHERE class_label='$class_title'", null)
        return result.moveToFirst()
    }
    }