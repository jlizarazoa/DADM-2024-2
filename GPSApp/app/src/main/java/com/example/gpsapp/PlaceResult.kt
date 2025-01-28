package com.example.gpsapp

import com.google.gson.annotations.SerializedName

data class PlaceResult(
    val place_id: String,
    val name: String,
    val vicinity: String, // Dirección aproximada o vecindario
    val geometry: Geometry,
    val rating: Double? = null,
    val types: List<String>
)