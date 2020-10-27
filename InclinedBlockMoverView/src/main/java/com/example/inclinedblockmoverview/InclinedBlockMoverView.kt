package com.example.inclinedblockmoverview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF
import android.app.Activity
import android.content.Context

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
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawInclinedBlockMover(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val lSize : Float = Math.min(w, h) / lineSizeFactor
    val rSize : Float = Math.min(w, h) / rectSizeFactor
    save()
    translate(w / 2, h / 2)
    rotate(deg * sf.divideScale(2, parts))
    save()
    rotate(rot * sf.divideScale(4, parts))
    translate(
        -lSize * (1 - sf.divideScale(3, parts)),
        (h / 2) * (1 - sf.divideScale(1, parts))
    )
    drawRect(RectF(-rSize, -rSize, 0f, 0f), paint)
    restore()
    drawLine(0f, 0f, -lSize * sf.divideScale(0, parts), 0f, paint)
    restore()
}

fun Canvas.drawIBMNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawInclinedBlockMover(scale, w, h, paint)
}

class InclinedBlockMoverView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return false
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class IBMNode(var i : Int, val state : State = State()) {

        private var prev : IBMNode? = null
        private var next : IBMNode? =null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = IBMNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawIBMNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : IBMNode {
            var curr : IBMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class InclinedBlockMover(var i : Int) {

        private var curr : IBMNode = IBMNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : InclinedBlockMoverView) {

        private val animator : Animator = Animator(view)
        private val ibm : InclinedBlockMover = InclinedBlockMover(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            ibm.draw(canvas, paint)
            animator.animate {
                ibm.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ibm.startUpdating {
                animator.start()
            }
        }
    }
}
