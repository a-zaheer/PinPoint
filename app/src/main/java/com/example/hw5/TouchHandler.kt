package com.example.hw5

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat

class TouchHandler(var fragment: CaptureFragment) : View.OnTouchListener {

    //we will need to this to interpret onLongPress and onDoubleTap
    var gestureDetectorCompat: GestureDetectorCompat =
        GestureDetectorCompat(fragment.requireContext(), MyGestureListener(fragment))

    //Handle the onTouch event
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val maskedAction = event.actionMasked
        gestureDetectorCompat.onTouchEvent(event)
        when (maskedAction) {
            //Start drawing a new line (path)
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                var i = 0
                //number of points
                val size = event.pointerCount
                while (i < size) {
                    //finger ID
                    val id = event.getPointerId(i)
                    //updating path: MainActivity->MyView->(invalidate)->XML-> your eyes
                    fragment.addNewPath(id, event.getX(i), event.getY(i))
                    i++
                }

            }
            //Continuing drawing the path as the finger moves
            MotionEvent.ACTION_MOVE -> {
                var i = 0
                val size = event.pointerCount
                while (i < size) {
                    val id = event.getPointerId(i)
                    fragment.updatePath(id, event.getX(i), event.getY(i))
                    i++
                }
            }
            //Remove the path the finger creates when the finger is lifted.
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                var i = 0
                val size = event.pointerCount
                while (i < size) {
                    val id = event.getPointerId(i)
                    fragment.removePath(id)
                    i++
                }
            }
        }

        return true
    }

    /**
     * inner class definition for the gesture listener that will
     * help us interpret onDoubleTap and onLongPress
     *
     * @property mainActivity
     */
    private class MyGestureListener(var fragment: CaptureFragment) : GestureDetector.SimpleOnGestureListener() {
    }
}