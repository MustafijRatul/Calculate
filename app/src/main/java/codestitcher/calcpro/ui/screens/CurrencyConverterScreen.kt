package codestitcher.calcpro.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codestitcher.calcpro.data.CurrencyItem
import codestitcher.calcpro.data.CurrencyRepository
import codestitcher.calcpro.ui.components.SubScreenTopBar
import codestitcher.calcpro.ui.theme.AccentCyan
import codestitcher.calcpro.ui.theme.AccentOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var fromCode by remember { mutableStateOf("USD") }
    var toCode by remember { mutableStateOf("BDT") }
    var amountText by remember { mutableStateOf("100") }

    var isSelectingFrom by remember { mutableStateOf(false) }
    var isSelectingTo by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var swapRot by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = swapRot,
        animationSpec = spring(),
        label = "swapRot"
    )

    // Exchange Rates State (Cached / Synced once a day)
    var ratesMap by remember { mutableStateOf(CurrencyRepository.getSavedRates(context)) }
    var syncInfo by remember { mutableStateOf(CurrencyRepository.getLastSyncFormatted(context)) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Sync rates once a day on screen launch
    LaunchedEffect(Unit) {
        val updated = CurrencyRepository.syncRatesIfNeeded(context, force = false)
        ratesMap = updated
        syncInfo = CurrencyRepository.getLastSyncFormatted(context)
    }

    fun triggerManualRefresh() {
        if (isRefreshing) return
        isRefreshing = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            val updated = CurrencyRepository.syncRatesIfNeeded(context, force = true)
            ratesMap = updated
            syncInfo = CurrencyRepository.getLastSyncFormatted(context)
            isRefreshing = false
            snackbarHostState.showSnackbar("Exchange rates updated successfully!")
        }
    }

    val allCurrencies = CurrencyRepository.ALL_CURRENCIES
    val fromCurrency = allCurrencies.find { it.code == fromCode } ?: allCurrencies.first { it.code == "USD" }
    val toCurrency = allCurrencies.find { it.code == toCode } ?: allCurrencies.first { it.code == "BDT" }

    val fromRate = ratesMap[fromCode] ?: fromCurrency.defaultRate
    val toRate = ratesMap[toCode] ?: toCurrency.defaultRate

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val converted = if (fromRate > 0) {
        (amount / fromRate) * toRate
    } else 0.0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SubScreenTopBar(title = "Currency Converter", onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rate Sync Status Bar with Refresh Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Public,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AccentCyan
                    )
                    Text(
                        text = syncInfo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { triggerManualRefresh() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Rates",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Input Amount Card
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount to convert") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Currency Selectors Row + Swap Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From Currency Button with Country Flag
                CurrencySelectorButton(
                    currency = fromCurrency,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        searchQuery = ""
                        isSelectingFrom = true
                    }
                )

                // Swap Button
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        swapRot += 180f
                        val temp = fromCode
                        fromCode = toCode
                        toCode = temp
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Currencies",
                        modifier = Modifier.rotate(animatedRotation),
                        tint = AccentOrange
                    )
                }

                // To Currency Button with Country Flag
                CurrencySelectorButton(
                    currency = toCurrency,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        searchQuery = ""
                        isSelectingTo = true
                    }
                )
            }

            // Converted Result Display Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${fromCurrency.flag} ${fromCurrency.symbol} ${String.format("%,.2f", amount)} ${fromCurrency.code} =",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${toCurrency.symbol} ${String.format("%,.2f", converted)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${toCurrency.flag} ${toCurrency.country} (${toCurrency.name})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "1 ${fromCurrency.code} = ${String.format("%.4f", toRate / fromRate)} ${toCurrency.code}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Modal Sheet for Selecting Currency with Alphabetical Grouping & Search
    if (isSelectingFrom || isSelectingTo) {
        val filteredList = remember(searchQuery) {
            val list = if (searchQuery.isBlank()) {
                allCurrencies
            } else {
                val q = searchQuery.trim().lowercase()
                allCurrencies.filter {
                    it.country.lowercase().contains(q) ||
                    it.name.lowercase().contains(q) ||
                    it.code.lowercase().contains(q)
                }
            }
            list.sortedBy { it.country }
        }

        // Group by starting letter of country
        val grouped = remember(filteredList) {
            filteredList.groupBy { it.country.first().uppercaseChar() }
        }

        ModalBottomSheet(
            onDismissRequest = {
                isSelectingFrom = false
                isSelectingTo = false
                searchQuery = ""
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isSelectingFrom) "Select Origin Country" else "Select Target Country",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by country or currency...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AccentCyan)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No countries match \"$searchQuery\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 440.dp)
                    ) {
                        grouped.forEach { (alphabet, countries) ->
                            // Minimal & Professional Alphabet Section Header
                            item(key = "header_$alphabet") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp, bottom = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(AccentCyan.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = alphabet.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            items(countries, key = { it.code + it.country }) { curr ->
                                val isSelected = (isSelectingFrom && curr.code == fromCode) || (isSelectingTo && curr.code == toCode)
                                val rate = ratesMap[curr.code] ?: curr.defaultRate

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) AccentCyan.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (isSelectingFrom) {
                                                fromCode = curr.code
                                                isSelectingFrom = false
                                            } else {
                                                toCode = curr.code
                                                isSelectingTo = false
                                            }
                                            searchQuery = ""
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Country Flag
                                        Text(
                                            text = curr.flag,
                                            fontSize = 26.sp
                                        )

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = curr.country,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "(${curr.code})",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AccentCyan
                                                )
                                            }
                                            Text(
                                                text = "${curr.name} • ${curr.symbol}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${String.format("%.2f", rate)} / $",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = AccentOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencySelectorButton(
    currency: CurrencyItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Flag
            Text(
                text = currency.flag,
                fontSize = 24.sp
            )

            Column {
                Text(
                    text = currency.code,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currency.country,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
