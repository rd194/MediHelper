package com.example.medihelper.mainapp

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medihelper.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun setTransparentStatusBar(isTransparent: Boolean) {
        when(isTransparent) {
            true -> window.statusBarColor = Color.TRANSPARENT
            false -> window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }
    }
}
