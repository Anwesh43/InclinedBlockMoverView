package com.example.inclinedblockmoverview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF

val parts : Int = 5
val strokeFactor : Float = 90f
val rectSizeFactor : Float = 12.8f
val lineSizeFactor : Float = 3.2f
val deg : Float = 45f
val delay : Long = 20
val colors : Array<Int> = arrayOf(
    "#F44336",
    "#3F51B5",
    "#4CAF50",
    "#FF9800",
    "#009688"
).map {
    Color.parseColor(it)
}.toTypedArray()
val scGap : Float = 0.02f / parts
val rot : Float = 90f + deg

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

