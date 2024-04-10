package com.example.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.currencyconverter.ui.theme.CurrencyConverterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CurrencyConverterTheme {
                MainScreen()
            }
        }

    }
}

@Composable
fun Balance(balance: Balance, modifier: Modifier = Modifier) {
    Text(
        text = "%.2f %s".format(balance.value, balance.currency),
        modifier = modifier,
    )
}

@Composable
fun UserBalances() {
    val viewModel: MainViewModel = viewModel()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_my_balances).uppercase(),
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        viewModel.balances.chunked(2).forEach { list ->
            Row {
                list.forEach { balance ->
                    Box(modifier = Modifier.weight(1f)) {
                        Balance(balance = balance)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyBalancesPreview() {
    CurrencyConverterTheme {
        UserBalances()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    currency: String,
    options: List<String>,
    onCurrencyChange: (String) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.width(122.dp)
    ) {
        TextField(
            readOnly = true,
            onValueChange = {},
            value = currency,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors().copy(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        onCurrencyChange(item)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun CurrencySell(
    currency: String,
    currencyOptions: List<String>,
    onValueChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_up),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        var value by remember { mutableStateOf("") }
        TextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                value = it
            },
            label = { Text(text = stringResource(id = R.string.title_sell)) },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        CurrencySelector(
            currency = currency,
            options = currencyOptions,
            onCurrencyChange = onCurrencyChange,
        )
    }
}

@Composable
fun CurrencyReceive(
    value: Float,
    currency: String,
    currencyOptions: List<String>,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_down),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        TextField(
            readOnly = true,
            value = "%.2f".format(value).takeIf { value > 0f } ?: "",
            onValueChange = { },
            label = { Text(text = stringResource(id = R.string.title_receive)) },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
            colors = TextFieldDefaults.colors().copy(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        CurrencySelector(
            currency = currency,
            options = currencyOptions,
            onCurrencyChange = onCurrencyChange,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val snackbarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(
                    stringResource(id = R.string.app_name),
                    Modifier.fillMaxWidth()
                )
            })
        },
        content = { contentPadding ->
            Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                val viewModel: MainViewModel = viewModel()
                val message by viewModel.message.collectAsState()

                message?.let {
                    coroutineScope.launch {
                        viewModel.messageViewed()
                        snackbarHostState.showSnackbar(it, withDismissAction = true)
                    }
                }

                UserBalances()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CurrencySell(
                    // user is able to select any currency from their wallet
                    currencyOptions = viewModel.balances.map { it.currency },
                    currency = viewModel.sellCurrency,
                    onValueChange = { viewModel.onSellValueChange(it) },
                    onCurrencyChange = { viewModel.onSellCurrencyChange(it) },
                    modifier = Modifier.padding(16.dp)
                )
                CurrencyReceive(
                    currencyOptions = viewModel.availableCurrencies,
                    value = viewModel.receiveAmount,
                    currency = viewModel.receiveCurrency,
                    onCurrencyChange = { viewModel.onReceiveCurrencyChange(it) },
                    modifier = Modifier.padding(16.dp),
                )
                Button(
                    onClick = { viewModel.submit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.action_submit))
                }

            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    CurrencyConverterTheme {
        MainScreen()
    }
}