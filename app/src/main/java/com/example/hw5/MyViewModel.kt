package com.example.hw5

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val locationProvider: LocationService
    private val database: Database
    private val _pointList: MutableLiveData<List<Point>>
    val pointList: LiveData<List<Point>>
    private val observer: Observer<MutableMap<String, Point>>

    private lateinit var currentPhotoPath: String
    private lateinit var currentThumbnail: String
    private lateinit var currentPoint: Point

    init {
        locationProvider = LocationService(application)
        database = Database()

        _pointList = MutableLiveData(emptyList())
        observer = Observer { this._pointList.value = it.values.toList() }
        database.pointList.observeForever(observer)
        pointList = _pointList
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =
            getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun getCurrentPath(): String {
        return currentPhotoPath
    }

    fun updateImage(newImage: Bitmap) {
        val imageFile = File(currentPhotoPath)
        val outStream = imageFile.outputStream()
        newImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
        currentPhotoPath = imageFile.absolutePath
    }


    private fun deleteImageFile(path: String) {
        val file = File(path)
        file.delete()
    }

    fun removePoint(point: Point) {
        pointList.value?.indexOf(point)?.let { database.removePoint(it) }
    }

    fun uploadPoint() {
        database.addPoint(currentPoint, currentPhotoPath)
        deleteImageFile(currentPhotoPath)
    }

    fun capturePoint() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val location = locationProvider.getLocation()
                val lat = location?.latitude.toString()
                val long = location?.longitude.toString()
                val time = location?.time.toString()
                val address = location?.let { locationProvider.addressCoder(it) }.toString()

                currentPoint = Point(lat, long, address, time, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        database.pointList.removeObserver(observer)
    }


    /*
    Use an inner class to group all the location related tasks together
     */
    private inner class LocationService(application: Application) {
        private var fusedLocationClient: FusedLocationProviderClient
        private val locationRequest: LocationRequest
        private val locationCallback: LocationCallback

        //Geocoder will handling the address lookup
        private var geocoder: Geocoder

        //this will be used to pass the current coordinates to Geocoder
        private var latlong: Location? = null

        //this will be used to accommodate the results returned by geocoder
        private var addresses: MutableList<Address> = mutableListOf()

        //Get the last known location and paint it to the textView
        init {
            //Geocoder will handling the address lookup
            geocoder = Geocoder(application, Locale.getDefault())

            //Create a location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)


            //A LocationRequest controls the process of asking for a new location.
            //the timings are in milliseconds

            locationRequest = LocationRequest.create().apply {
                interval = 1000
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            //setting up the callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult ?: return
                    for (location in locationResult.locations) {
                        latlong = location
                    }
                }
            }

            initializer()
        }

        @SuppressLint("MissingPermission")
        fun initializer() {
            //get the last location identified. Also, set a listener that updates the
            //R.id.coordinates text when the location is found (on Success)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got the last known location. In some rare situations this can be null.
                    //we are using the let scope to avoid writing the if statements for this type of assignment
                    location?.let { latlong = it }
                }

            //This will let the user know when the location was not able to be found.
            fusedLocationClient.lastLocation
                .addOnFailureListener {
                    latlong = null
                    Log.d("location request", "Failed to Get Location")
                }

            //passing the `locationCallback` object and the `locationRequest` to the location client
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }

        fun getLocation(): Location? {
            return latlong
        }

        fun addressCoder(location: Location): String {

            //adapted from https://developer.android.com/training/location/display-address.html
            //the geocoder can convert a lat/long location to an address
            //We get the location from the fusedLocationClient instance
            try {
                addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ) as MutableList<Address>
            } catch (ioException: IOException) {
                // Catch network or other I/O problems.
                return "Error: Service Not Available --$ioException"

            } catch (illegalArgumentException: IllegalArgumentException) {
                // Catch invalid latitude or longitude values.
                return "Error: Invalid lat long used--$illegalArgumentException"
            }

            if (addresses.isEmpty())
                return "No address found :("

            return addresses[0].getAddressLine(0)

        }
    }
}