package com.example.catalogo

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("resource/9qqx-f753.json")
    fun getAllPlaces(): Call<List<Place>>

    @GET("resource/9qqx-f753.json")
    fun getFilteredPlaces(
        @Query("\$where") whereQuery: String?,
        @Query("pap") pap: String?
    ): Call<List<Place>>
}