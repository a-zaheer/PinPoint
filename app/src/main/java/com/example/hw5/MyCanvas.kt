package com.example.hw5

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * This custom class extends View and manages drawing paths on the screen.
 * It is used by MainActivity, and is the main UI for the app.
 */
class MyCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    //stroke thickness defined in the Paint object
    val strokeWidthMedium = 10f

    // this is where we map finger ID (Int) to Path (the line drawn on screen)
    var activePaths: HashMap<Int, Path> = HashMap()

    //setting up the Paint object. Notice the use of Kotlin's '.apply{}'
    var pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthMedium
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        for (path in activePaths.values) {
            canvas?.drawPath(path, pathPaint)
        }
    }

    fun drawOnTop(canvas: Canvas?) {
        for (path in activePaths.values) {
            canvas?.drawPath(path, pathPaint)
        }
    }

    /**
     * This function is called by TouchHandler when a new finger is detected on the screen.
     * It adds a new path to the set of active paths.
     */
    fun addPath(id: Int, x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        activePaths[id] = path
        invalidate()

    }

    /**
     * This function is called by TouchHandler when a finger is moved on the screen.
     * It updates the path corresponding to the finger ID.
     * Notice the use of .lineTo(x,y)
     */
    fun updatePath(id: Int, x: Float, y: Float) {
        val path = activePaths[id]
        path?.lineTo(x, y)
        invalidate()
    }

    fun removePath(id: Int) {
        invalidate()
    }
    fun clear() {
        for(id in activePaths.keys) {
            activePaths.remove(id)
        }
        invalidate()
    }
}
