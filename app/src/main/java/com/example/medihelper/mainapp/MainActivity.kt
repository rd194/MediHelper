package com.example.medihelper.mainapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medihelper.AppRepository
import com.example.medihelper.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            AppRepository.updatePlannedMedicinesStatuses()
        }
    }

    fun setStatusBarColor(colorResID: Int) {
        window.statusBarColor = ContextCompat.getColor(this, colorResID)
    }

    private fun setupBottomNav() {
        val navController = findNavController(R.id.nav_host_fragment)
        bottom_nav.setupWithNavController(navController)
    }
}
