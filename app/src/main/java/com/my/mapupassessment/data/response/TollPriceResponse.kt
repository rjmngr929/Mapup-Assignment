package com.my.mapupassessment.data.response

import com.google.gson.annotations.SerializedName

class TollPriceResponse (
    @SerializedName("status") val status: String = "",
    @SerializedName("route") val routeResModel: RouteResModel = RouteResModel(),
)

class RouteResModel (
    @SerializedName("hasTolls") val hasTolls: Boolean = false,
    @SerializedName("costs") val costsDetail: CostsDetail = CostsDetail(),
)

class CostsDetail (
    @SerializedName("tag") val tagCost: Double? = 0.0,
    @SerializedName("cash") val cashCost: Double? = 0.0,
    @SerializedName("minimumTollCost") val minimumTollCost: Double? = 0.0,
    @SerializedName("maximumTollCost") val maximumTollCost: Double? = 0.0,
)

