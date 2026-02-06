package com.my.mapupassessment.data


data class TollPriceRequest(
    val mapProvider: String,
    val polyline: String,
    val vehicle: Vehicle,
    val units: Units
)

data class Vehicle(
    val type: String
)

data class Units(
    val currency: String
)
