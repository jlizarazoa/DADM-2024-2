package com.example.catalogo

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("codigo_dane") val code: String,
    @SerializedName("municipio") val municipality: String,
    @SerializedName("nombre") val name: String,
    @SerializedName("pap") val pap: String,
    @SerializedName("descripcio") val description: String,

    )
