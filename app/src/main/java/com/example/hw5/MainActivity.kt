package com.example.hw5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hw5.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "HW5"
    }

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}