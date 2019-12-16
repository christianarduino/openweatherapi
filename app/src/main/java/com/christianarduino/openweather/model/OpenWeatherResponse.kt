package com.christianarduino.openweather.model

data class OpenWeatherResponse(val weather: ArrayList<Weather>, val main: Main, val clouds: Clouds, val name: String) {
    data class Weather(
        val main: String,
        val description: String
    )
    data class Main(
        val temp: Double,
        val humidity: Int,
        val temp_min: Double,
        val temp_max: Double
    )
    data class Clouds(
        val all: Int
    )
}