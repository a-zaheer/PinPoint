package com.example.hw5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hw5.databinding.FragmentCaptureBinding

/**
 * A simple [Fragment] subclass.
 * Use the [CaptureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CaptureFragment : Fragment() {
    private lateinit var binding: FragmentCaptureBinding

    // an instance of MyCanvas
    lateinit var myCanvas: MyCanvas

    //an instance of TouchHandler that will be set-up to listen to touch events from within MyCanvas.
    lateinit var touchHandler: TouchHandler
    private val vm: MyViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCaptureBinding.inflate(inflater, container, false)


        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val bitmap =
            BitmapFactory.decodeFile(vm.getCurrentPath()).copy(Bitmap.Config.ARGB_8888, true)

        myCanvas = binding.myCanvas
        touchHandler = TouchHandler(this)
        myCanvas.setOnTouchListener(touchHandler)

        val finalCanvas = Canvas(bitmap)
        myCanvas.background = BitmapDrawable(resources, bitmap)


        binding.submitImageButton.setOnClickListener {
            myCanvas.drawOnTop(finalCanvas)

            vm.updateImage(bitmap)
            vm.uploadPoint()
            findNavController().navigate(R.id.action_captureFragment_to_listFragment)
        }

        binding.clearImageButton.setOnClickListener {
            myCanvas.clear()
        }

        return binding.root
    }

    //touchandler-> this function relays it to -> myCanvas
    //You can also allow touchHandler to update myCanvas directly
    // (this will require passing myCanvas to touchHandler).
    fun addNewPath(id: Int, x: Float, y: Float) {
        myCanvas.addPath(id, x, y)
    }

    fun updatePath(id: Int, x: Float, y: Float) {
        myCanvas.updatePath(id, x, y)
    }

    fun removePath(id: Int) {
        myCanvas.removePath(id)
    }
}