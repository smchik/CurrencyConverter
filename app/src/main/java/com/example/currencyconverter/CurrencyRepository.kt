package com.example.currencyconverter

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CurrencyRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://developers.paysera.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(CurrencyApiService::class.java)

    private var ratesResponse: CurrencyExchangeResponse? = null

    private val baseCurrency: String
        get() = ratesResponse?.base ?: "EUR"

    private val rates: Map<String, Float>
        get() = ratesResponse?.rates ?: emptyMap()

    private var conversionCount = 0

    private val _availableCurrencies = mutableStateListOf<String>()
    val availableCurrencies: List<String>
        get() = _availableCurrencies.filter { restrictedCurrencyList.contains(it) }

    private val _balances = mutableStateListOf<Balance>()
    val balances: List<Balance>
        get() = _balances

    init {
        _balances.add(Balance(1000f, "EUR"))
        scope.launch {
            while (true) {
                scope.ensureActive()
                try {
                    ratesResponse = apiService.getCurrencyRates()
                    _availableCurrencies.addAll(rates.keys.sorted())
                } catch (e: Throwable) {
                    // ignore
                }
                delay(5000)
            }
        }
    }

    fun clear() {
        scope.cancel()
    }

    suspend fun exchange(
        sellAmount: Float,
        sellCurrency: String,
        receiveAmount: Float,
        receiveCurrency: String,
    ) {

        val sellBalance = _balances.first { it.currency == sellCurrency }

        val commission = calculateCommission(sellAmount)

        if ((sellAmount + commission) > sellBalance.value) {
            throw NotEnoughFundsException()
        }

        if (_balances.none { it.currency == receiveCurrency }) {
            _balances.add(Balance(0f, receiveCurrency))
        }
        _balances.replaceAll { balance ->
            when (balance.currency) {
                sellCurrency -> {
                    balance.copy(value = balance.value - sellAmount - commission)
                }

                receiveCurrency -> {
                    balance.copy(value = balance.value + receiveAmount)
                }

                else -> {
                    balance
                }
            }
        }
        exchangeCompleted()
    }

    suspend fun convertCurrency(amount: Float, fromCurrency: String, toCurrency: String): Float {
        // Convert the amount to base currency (EUR) if necessary
        val amountInBase =
            if (fromCurrency != baseCurrency) amount / rates[fromCurrency]!! else amount

        // Convert the amount from base to the target currency
        return if (toCurrency != baseCurrency) amountInBase * rates[toCurrency]!! else amountInBase
    }

    fun calculateCommission(amount: Float): Float {
        return if (conversionCount >= 5) amount * 0.007f else 0f
    }

    private fun exchangeCompleted() {
        conversionCount++
    }

    companion object {
        private val restrictedCurrencyList = listOf(
            "USD", // United States Dollar
            "EUR", // Euro
            "JPY", // Japanese Yen
            "GBP", // British Pound Sterling
            "AUD", // Australian Dollar
            "CAD", // Canadian Dollar
            "CHF", // Swiss Franc
            "CNY", // Chinese Yuan
            "SEK", // Swedish Krona
            "NZD", // New Zealand Dollar
            "MXN", // Mexican Peso
            "SGD", // Singapore Dollar
            "HKD", // Hong Kong Dollar
            "NOK", // Norwegian Krone
            "KRW", // South Korean Won
            "TRY", // Turkish Lira
            "INR", // Indian Rupee
            "BRL", // Brazilian Real
            "ZAR"  // South African Rand
        )

    }
}

class NotEnoughFundsException : IllegalStateException()


data class Balance(
    val value: Float,
    val currency: String,
)

