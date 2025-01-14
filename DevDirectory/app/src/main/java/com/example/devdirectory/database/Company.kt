package com.example.devdirectory.database

data class Company(
    val id: Long = 0,
    val name: String,
    val urlWeb: String?,
    val phone: String?,
    val email: String?,
    val products: String?,
    val classification: String?
)
