package com.example.testproject

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle

class DBtests(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "TestStorage", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE tests (id INTEGER PRIMARY KEY AUTOINCREMENT, folder_id TEXT, question_text TEXT, right_answer TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS tests")
        onCreate(db)
    }

    fun addTest(test_example: TestCreator){
        val values = ContentValues()
        values.put("folder_id", test_example.folder_id)
        values.put("question_text", test_example.question_text)
        values.put("right_answer", test_example.right_answer)

        val db = this.writableDatabase
        db.insert("tests", null, values)

        db.close()
    }
    fun getTestId(test_name: String): Bundle {
        val db = this.readableDatabase
        val test_info = Bundle()
        val result = db.rawQuery("SELECT * FROM tests WHERE question_text = '$test_name'", null)
        if (result.moveToFirst()){
            test_info.putString("test_id", result.getInt(result.getColumnIndexOrThrow("id")).toString())
        }
        return test_info
    }
    fun getTestRightAnswer(test_id: String): List<String> {
        val db = this.readableDatabase
        var test_info: List<String> = listOf()
        val result = db.rawQuery("SELECT * FROM tests WHERE id = '$test_id'", null)
        if (result.moveToFirst()){
            test_info += result.getString(result.getColumnIndexOrThrow("right_answer"))
        }
        return test_info
    }

}