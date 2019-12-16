package com.christianarduino.openweather.model

data class WeeklyPrevisionResponse(val list: ArrayList<Data>, val city: City) {
    data class Data(
        val dt: Int,
        val main: Main,
        val clouds: OpenWeatherResponse.Clouds,
        val wind: Wind
    ) {
        data class Main(
            val temp: Double,
            val feels_like: Double,
            val temp_min: Double,
            val temp_max: Double,
            val humidity: Int
        )
    }

    data class City(val name: String, val sunrise: Int, val sunset: Int)

    data class Wind(val speed: Double)
}