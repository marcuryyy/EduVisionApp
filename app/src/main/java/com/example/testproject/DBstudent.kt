package com.example.testproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBstudent(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "StudentStorage", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE students (class_id TEXT, name TEXT, aruco TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS students")
        onCreate(db)
    }

    fun addStudent(student: StudentCreator) {
        val values = ContentValues()
        values.put("class_id", student.class_id)
        values.put("name", student.student_name)
        values.put("aruco", student.aruco_id)

        val db = this.writableDatabase
        db.insert("students", null, values)

        db.close()
    }
}