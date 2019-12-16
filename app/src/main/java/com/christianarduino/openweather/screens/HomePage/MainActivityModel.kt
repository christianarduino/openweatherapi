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

sealed class OpenWeatherEvent {
    object OnRequestLocationPermission : OpenWeatherEvent()
    data class OnPermissionAllow(var lat: String, var long: String): OpenWeatherEvent()
}

sealed class OpenWeatherState {
    object InProgress : OpenWeatherState()
    data class Error(val error: IOException): OpenWeatherState()
    data class Success(val weather: OpenWeatherResponse): OpenWeatherState()
}

class MainActivityModel: ViewModel() {

    private lateinit var openWeatherService: OpenWeatherService
    private val openWeatherState = MutableLiveData<OpenWeatherState>()

    fun observe(owner: LifecycleOwner, observer: (OpenWeatherState) -> Unit) {
        openWeatherState.observe(owner, Observer { it?.let(observer::invoke) })
    }

    fun send(event: OpenWeatherEvent) {
        when (event) {
            is OpenWeatherEvent.OnRequestLocationPermission -> openWeatherState.postValue(OpenWeatherState.InProgress)
            is OpenWeatherEvent.OnPermissionAllow -> {
                openWeatherService =
                    OpenWeatherService(
                        event.lat,
                        event.long
                    )
                openWeatherService.loadData(object :
                    OpenWeatherReceiver {
                    override fun receive(result: OpenWeatherResult) {
                        when(result) {
                            is OpenWeatherResult.Error -> {
                                openWeatherState.postValue(
                                    OpenWeatherState.Error(
                                        error = result.error
                                    )
                                )
                            }
                            is OpenWeatherResult.Success -> {
                                openWeatherState.postValue(
                                    OpenWeatherState.Success(
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