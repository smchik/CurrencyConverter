package com.example.currencyconverter

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

interface CurrencyApiService {

    @GET("tasks/api/currency-exchange-rates")
    suspend fun getCurrencyRates(): CurrencyExchangeResponse
}

data class CurrencyExchangeResponse(
    @SerializedName("base") val base: String,
    @SerializedName("date") val date: String,
    @SerializedName("rates") val rates: Map<String, Float>,
)

