package com.christianarduino.openweather.screens.HomePage

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.christianarduino.openweather.OpenWeatherReceiver
import com.christianarduino.openweather.OpenWeatherResult
import com.christianarduino.openweather.OpenWeatherService
import com.christianarduino.openweather.model.OpenWeatherResponse
import java.io.IOException

sealed class SingleDayEvent {
    object OnRequestLocationPermission : SingleDayEvent()
    data class OnPermissionAllow(var lat: String, var long: String) : SingleDayEvent()
}

sealed class SingleDayState {
    object InProgress : SingleDayState()
    data class Error(val error: IOException) : SingleDayState()
    data class Success(val weather: OpenWeatherResponse) : SingleDayState()
}

class MainActivityModel : ViewModel() {

    private lateinit var openWeatherService: OpenWeatherService
    private val openWeatherState = MutableLiveData<SingleDayState>()

    fun observe(owner: LifecycleOwner, observer: (SingleDayState) -> Unit) {
        openWeatherState.observe(owner, Observer { it?.let(observer::invoke) })
    }

    fun send(event: SingleDayEvent) {
        when (event) {
            is SingleDayEvent.OnRequestLocationPermission -> openWeatherState.postValue(
                SingleDayState.InProgress
            )
            is SingleDayEvent.OnPermissionAllow -> {
                openWeatherService =
                    OpenWeatherService(
                        event.lat,
                        event.long
                    )
                openWeatherService.loadWeatherData(false, object :
                    OpenWeatherReceiver {
                    override fun receive(result: OpenWeatherResult) {
                        when (result) {
                            is OpenWeatherResult.Error -> {
                                openWeatherState.postValue(
                                    SingleDayState.Error(
                                        error = result.error
                                    )
                                )
                            }
                            is OpenWeatherResult.SuccessOpenWeather -> {
                                openWeatherState.postValue(
                                    SingleDayState.Success(
                                        weather = result.weather
                                    )
                                )
                            }
                        }
                    }
                })
            }
        }
    }
}