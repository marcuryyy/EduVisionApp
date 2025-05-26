package com.example.testproject

import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    val URL = "https://eduvision.na4u.ru/api"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}