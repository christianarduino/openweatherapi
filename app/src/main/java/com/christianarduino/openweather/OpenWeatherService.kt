package com.christianarduino.openweather

import com.christianarduino.openweather.model.OpenWeatherResponse
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

sealed class OpenWeatherResult {
    data class Error(val error: IOException) : OpenWeatherResult()
    data class Success(val weather: OpenWeatherResponse) : OpenWeatherResult()
}

interface OpenWeatherReceiver {
    fun receive(result: OpenWeatherResult)
}

class OpenWeatherService(private var lat: String, private var long: String) {
    private val baseURL = "https://api.openweathermap.org/data/2.5/weather?"
    private val appId = "9b71735de964547d642787f42de580f8"
    private val ID = "OpenWeatherService"

    fun loadData(receiver: OpenWeatherReceiver) {
        val url = baseURL + "lat=$lat&lon=$long&units=metric&APPID=$appId"
        println(url)
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                receiver.receive(OpenWeatherResult.Error(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val data: OpenWeatherResponse = parseJson(response.body?.string())
                receiver.receive(OpenWeatherResult.Success(data))
            }
        })
    }

    fun parseJson(response: String?): OpenWeatherResponse {
        return Gson().fromJson(response, OpenWeatherResponse::class.java)
    }
}
