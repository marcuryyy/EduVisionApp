package com.example.testproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLData

class DBtests(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "TestStorage", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE tests (id INT PRIMARY KEY, question_text TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS tests")
        onCreate(db)
    }

    fun addTest(test_example: TestCreator){
        val values = ContentValues()
        values.put("question_text", test_example.question_text)

        val db = this.writableDatabase
        db.insert("tests", null, values)

        db.close()
    }

}