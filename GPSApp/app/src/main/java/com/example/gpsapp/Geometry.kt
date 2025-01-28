package com.example.gpsapp

import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("location") val location: UserLocation
)

