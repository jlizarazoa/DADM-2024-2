package com.example.gpsapp

import com.google.gson.annotations.SerializedName

data class PlaceResponse(
    val results: List<PlaceResult>,
    val status: String
)
