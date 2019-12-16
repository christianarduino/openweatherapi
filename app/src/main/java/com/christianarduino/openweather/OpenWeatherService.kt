package com.christianarduino.openweather

import com.christianarduino.openweather.model.OpenWeatherResponse
import com.christianarduino.openweather.model.WeeklyPrevisionResponse
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

sealed class OpenWeatherResult {
    data class Error(val error: IOException) : OpenWeatherResult()
    data class SuccessOpenWeather(val weather: OpenWeatherResponse) : OpenWeatherResult()
    data class SuccessWeeklyPrevision(val weather: WeeklyPrevisionResponse) : OpenWeatherResult()
}

interface OpenWeatherReceiver {
    fun receive(result: OpenWeatherResult)
}

class OpenWeatherService(private var lat: Double, private var long: Double) {
    private val baseUrlWeather = "https://api.openweathermap.org/data/2.5/weather?"
    private val baseUrlForecast = "https://api.openweathermap.org/data/2.5/forecast?"
    private val appId = "9b71735de964547d642787f42de580f8"
    private val ID = "OpenWeatherService"
    private lateinit var client: OkHttpClient

    fun loadWeatherData(isForecast: Boolean, receiver: OpenWeatherReceiver) {
        var baseURL = if (isForecast) baseUrlForecast else baseUrlWeather
        val url = baseURL + "lat=$lat&lon=$long&units=metric&APPID=$appId"
        client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                receiver.receive(OpenWeatherResult.Error(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (isForecast) {
                    val data: WeeklyPrevisionResponse =
                        parseJsonWeeklyPrevision(response.body?.string())
                    receiver.receive(OpenWeatherResult.SuccessWeeklyPrevision(data))
                } else {
                    val data: OpenWeatherResponse = parseJsonOpenWeather(response.body?.string())
                    receiver.receive(OpenWeatherResult.SuccessOpenWeather(data))
                }
            }
        })
    }

    private fun parseJsonOpenWeather(response: String?): OpenWeatherResponse {
        return Gson().fromJson(response, OpenWeatherResponse::class.java)
    }

    private fun parseJsonWeeklyPrevision(response: String?): WeeklyPrevisionResponse {
        return Gson().fromJson(response, WeeklyPrevisionResponse::class.java)
    }
}
