package com.example.hw5

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File


class Database {
    private val _pointList: MutableLiveData<MutableMap<String, Point>> =
        MutableLiveData(mutableMapOf())
    val pointList: LiveData<MutableMap<String, Point>> = _pointList

    private val database: DatabaseReference = Firebase.database.reference.also {
        it.child("points").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val point = deserialize(snapshot)
                _pointList.value?.put(snapshot.key!!, point)
                _pointList.postValue(_pointList.value)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val point = deserialize(snapshot)
                _pointList.value?.remove(snapshot.key!!)
                _pointList.value?.put(snapshot.key!!, point)
                _pointList.postValue(_pointList.value)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                _pointList.value?.remove(snapshot.key!!)
                _pointList.postValue(_pointList.value)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(MainActivity.TAG, "postPoints:onCancelled", error.toException())
            }
        })
    }

    private val storageRef: StorageReference = Firebase.storage.reference.also {
    }

    fun removePoint(pointIndex: Int) {
        val key = _pointList.value?.keys?.toList()?.get(pointIndex)
        val ref = key?.let { database.child("points").child(it) }
        val imagePath = _pointList.value?.get(key)?.imageUrl
        if (ref != null) {
            ref.removeValue()
        }
        val imageRef = imagePath?.let { storageRef.storage.getReferenceFromUrl(it) }
        if (imageRef != null) {
            imageRef.delete()
        }
    }


    fun addPoint(point: Point, photoPath: String) {
        var file = Uri.fromFile(File(photoPath))
        val imagesRef = storageRef.child("images/${file.lastPathSegment}")
        val uploadTask = imagesRef.putFile(file)


        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                point.imageUrl = task.result.toString()
                val newPointRef = database.child("points").push()
                newPointRef.setValue(point)
            } else {
                Log.i(MainActivity.TAG, "Could not upload image. Point not added.")
            }
        }

    }

    private fun deserialize(snapshot: DataSnapshot): Point {
        val pointMap = snapshot.value as Map<String, String>
        val lat = pointMap["lat"].toString()
        val long = pointMap["long"].toString()
        val address = pointMap["address"].toString()
        val time = pointMap["time"].toString()
        val url = pointMap["imageUrl"].toString()

        return Point(lat, long, address, time, url)

    }
}