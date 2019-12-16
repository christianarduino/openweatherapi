package com.christianarduino.openweather.screens.WeeklyPrevisionPage

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.christianarduino.openweather.OpenWeatherReceiver
import com.christianarduino.openweather.OpenWeatherResult
import com.christianarduino.openweather.OpenWeatherService
import com.christianarduino.openweather.model.WeeklyPrevisionResponse
import java.io.IOException

sealed class WeeklyPrevisionEvent {
    object onActivityOpen: WeeklyPrevisionEvent()
    data class makeRequest(val lat: Double, val long: Double) : WeeklyPrevisionEvent()
    data class showData(val list: List<WeeklyPrevisionResponse>) : WeeklyPrevisionEvent()
}

sealed class WeeklyPrevisionState {
    object InProgress : WeeklyPrevisionState()
    data class Error(val error: IOException) : WeeklyPrevisionState()
    data class Success(val list: WeeklyPrevisionResponse) : WeeklyPrevisionState()
}

class WeeklyPrevisionActivityViewModel : ViewModel() {

    private lateinit var weeklyService: OpenWeatherService
    private val weeklyState = MutableLiveData<WeeklyPrevisionState>()

    fun observe(owner: LifecycleOwner, observer: (WeeklyPrevisionState) -> Unit) {
        weeklyState.observe(owner, Observer { it?.let(observer::invoke) })
    }

    fun send(event: WeeklyPrevisionEvent) {
        when (event) {
            is WeeklyPrevisionEvent.onActivityOpen -> weeklyState.postValue(WeeklyPrevisionState.InProgress)
            is WeeklyPrevisionEvent.makeRequest -> {
                weeklyService = OpenWeatherService(event.lat, event.long)
                weeklyService.loadWeatherData(
                    true,
                    object : OpenWeatherReceiver {
                        override fun receive(result: OpenWeatherResult) {
                            when (result) {
                                is OpenWeatherResult.SuccessWeeklyPrevision -> weeklyState.postValue(
                                    WeeklyPrevisionState.Success(result.weather)
                                )
                                is OpenWeatherResult.Error -> weeklyState.postValue(
                                    WeeklyPrevisionState.Error(result.error)
                                )
                            }
                        }
                    })
            }
        }
    }

}