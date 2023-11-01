package com.example.weatherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=eqJ5LjzG8JZdN0Y0Twk9abyZ71fxgC12SEyRmu3iwUhnxDgz3ehroJUAtuA5WlO7vtH3V4p0rTRR1TNNv8tINg%3D%3D&pageNo=1&numOfRows=400&dataType=json")
    fun getVillageForecast(
        // @Query("serviceKey") serviceKey: String = Resources.getSystem().getString(R.string.SERVICE_KEY),
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int,
    ): Call<WeatherEntity>

}