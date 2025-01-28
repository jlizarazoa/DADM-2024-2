package com.example.gpsapp

import com.google.gson.annotations.SerializedName

data class UserLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)