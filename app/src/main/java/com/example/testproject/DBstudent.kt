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

    fun getStudents(class_id: String): MutableMap<String, String>{
        val db = this.readableDatabase
        val student_list_dict: MutableMap<String, String> = mutableMapOf()
        val result = db.rawQuery("SELECT * FROM students WHERE class_id='$class_id'", null)
        if(result.moveToFirst()){
            do {
                val student_name: String = result.getString(result.getColumnIndexOrThrow("name"))
                val aruco_id: String = result.getString(result.getColumnIndexOrThrow("aruco"))

                student_list_dict[aruco_id] = student_name
            } while (result.moveToNext())
        }
        result.close()
        return student_list_dict
    }

    fun findStudent(class_id: String, aruco_id: String): Boolean{
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM students WHERE aruco='$aruco_id' AND class_id='$class_id'", null)
        return result.moveToFirst()
    }
}