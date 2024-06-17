package com.example.testproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    val circles = ArrayList<Circle>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (circle in circles) {
            paint.color = circle.color
            canvas.drawCircle(circle.centerX, circle.centerY, circle.radius, paint)
        }
    }

    fun updateCircle(id: String, centerX: Double, centerY: Double, radius: Float) {
        val circleToUpdate = circles.find { it.id == id }
        if (circleToUpdate != null) {
            circleToUpdate.centerX = centerX.toFloat()
            circleToUpdate.centerY = centerY.toFloat()
            circleToUpdate.radius = radius
            invalidate()
        }
    }

    fun addCircle(id: String, centerX: Double, centerY: Double, radius: Float, color: Int) {
        circles.add(Circle(id, centerX.toFloat(), centerY.toFloat(), radius, color))
        invalidate()
    }

    fun updateCircleColor(id: String, color: Int) {
        val circleToUpdate = circles.find { it.id == id }
        if (circleToUpdate != null) {
            circleToUpdate.color = color
            invalidate()
        }
    }

    fun removeCircles() {
        circles.clear()
        invalidate()
    }

    data class Circle(val id: String, var centerX: Float, var centerY: Float, var radius: Float, var color: Int)
}
