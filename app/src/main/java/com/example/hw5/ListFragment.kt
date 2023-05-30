package com.example.hw5

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hw5.databinding.CardViewBinding
import com.example.hw5.databinding.FragmentListBinding
import java.io.File
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding
    private val vm: MyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false)

        val recyclerView = binding.pointListView
        val adapter = PointListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        vm.pointList.observe(
            viewLifecycleOwner,
            Observer<List<Point>> { points -> adapter.setPoint(points) })

        binding.captureButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        return binding.root
    }

    inner class PointListAdapter : RecyclerView.Adapter<PointListAdapter.PointViewHolder>() {
        private var points = emptyList<Point>()
        fun setPoint(points: List<Point>) {
            this.points = points
            notifyDataSetChanged()
        }

        inner class PointViewHolder(val binding: CardViewBinding) :
            RecyclerView.ViewHolder(binding.root) {
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {

            val binding =
                CardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)


            return PointViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return points.size
        }

        override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
            Glide.with(requireActivity())
                .load(points[position].imageUrl)
                .into(holder.binding.imageView)

            holder.binding.imageView.setOnClickListener {
                findNavController().navigate(R.id.action_listFragment_to_imageFragment, bundleOf("imagePath" to points[position].imageUrl))
            }

            holder.binding.deleteButton.setOnClickListener { vm.removePoint(points[position]) }

            holder.binding.coordsTxt.text = buildString {
                append(points[position].lat)
                append(", ")
                append(points[position].long)
            }
            holder.binding.addTxt.text = buildString {
                append("Address: ")
                append(points[position].address)}
            holder.binding.timeTxt.text = buildString{
                append("Time Stamp: ")
                append(points[position].time)
            }

            holder.itemView.setOnClickListener {
                val markerInfo = bundleOf("lat" to points[position].lat.toDouble(), "long" to points[position].long.toDouble())
                findNavController().navigate(R.id.action_listFragment_to_mapsFragment,markerInfo)
            }
        }

    }

    private val REQUEST_IMAGE_CAPTURE = 1
    private var photoURI : Uri? = null
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    vm. createImageFile()
                } catch (ex: IOException) {
                    Log.i(MainActivity.TAG, "Error occurred while creating file")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.hw5.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            vm.capturePoint()
            findNavController().navigate(R.id.action_listFragment_to_captureFragment)
        }
    }
}