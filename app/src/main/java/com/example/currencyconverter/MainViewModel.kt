package com.example.currencyconverter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val currencyRepository = CurrencyRepository(scope = viewModelScope)

    var sellAmount by mutableFloatStateOf(0f)
        private set
    var sellCurrency by mutableStateOf("EUR")
        private set
    var receiveAmount by mutableFloatStateOf(0f)
        private set
    var receiveCurrency by mutableStateOf("USD")
        private set

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val availableCurrencies: List<String>
        get() = currencyRepository.availableCurrencies

    val balances: List<Balance>
        get() = currencyRepository.balances

    override fun onCleared() {
        super.onCleared()
        currencyRepository.clear()
    }

    fun onSellValueChange(value: String) {
        sellAmount = value.toFloatOrNull() ?: 0f
        calculateReceiveAmount()
    }

    fun onSellCurrencyChange(value: String) {
        sellCurrency = value
        if (sellCurrency == receiveCurrency) {
            receiveCurrency = availableCurrencies.filterNot { it == sellCurrency }.first()
        }
        calculateReceiveAmount()
    }

    fun onReceiveCurrencyChange(value: String) {
        receiveCurrency = value
        calculateReceiveAmount()
    }

    private fun calculateReceiveAmount() {
        viewModelScope.launch {
            receiveAmount = currencyRepository.convertCurrency(sellAmount, sellCurrency, receiveCurrency)
        }
    }

    fun submit() {
        viewModelScope.launch {

            val commission = currencyRepository.calculateCommission(sellAmount)

            try {
                currencyRepository.exchange(sellAmount, sellCurrency, receiveAmount, receiveCurrency)

                val commissionMessage = if (commission > 0) {
                    " Commission Fee - %.2f %s.".format(commission, sellCurrency)
                } else {
                    ""
                }

                _message.value = "You have converted %.2f %s to %.2f %s.%s"
                    .format(sellAmount, sellCurrency, receiveAmount, receiveCurrency, commissionMessage)

            } catch (e: NotEnoughFundsException) {
                _message.value = "You don't have enough amount in your wallet"
            }
        }
    }

    fun messageViewed() {
        _message.value = null
    }
}
