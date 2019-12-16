package com.christianarduino.openweather.screens.WeeklyPrevisionPage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.christianarduino.openweather.R

class WeeklyPrevisionActivity : AppCompatActivity() {

    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private lateinit var viewModel: WeeklyPrevisionActivityViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_prevision)

        viewModel = ViewModelProviders.of(this).get(WeeklyPrevisionActivityViewModel::class.java)

        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("lon", 0.0)

        setupView()
        setupObserver()
    }

    override fun onStart() {
        super.onStart()
        viewModel.send(WeeklyPrevisionEvent.onActivityOpen)
    }

    private fun setupView() {
       progressBar = findViewById(R.id.progressBar)
    }

    private fun setupObserver() {
        viewModel.observe(this) { weeklyPrevisionState ->
            when(weeklyPrevisionState) {
                is WeeklyPrevisionState.InProgress -> makeRequest()
                is WeeklyPrevisionState.Success -> Log.d("WeeklyPrevisionActivity", weeklyPrevisionState.list.toString())
            }

        }
    }

    private fun makeRequest() {
        progressBar.visibility = View.VISIBLE
        viewModel.send(WeeklyPrevisionEvent.makeRequest(lat, lon))
    }
}
