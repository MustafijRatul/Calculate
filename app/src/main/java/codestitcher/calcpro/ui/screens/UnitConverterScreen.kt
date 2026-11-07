package codestitcher.calcpro.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codestitcher.calcpro.ui.components.SubScreenTopBar
import codestitcher.calcpro.ui.theme.AccentCyan
import codestitcher.calcpro.ui.theme.AccentOrange

enum class UnitCategory(val displayName: String) {
    LENGTH("Length"),
    WEIGHT("Weight"),
    TEMPERATURE("Temperature"),
    SPEED("Speed")
}

val UNIT_DATA = mapOf(
    UnitCategory.LENGTH to mapOf(
        "Meters (m)" to 1.0,
        "Kilometers (km)" to 0.001,
        "Miles (mi)" to 0.000621371,
        "Yards (yd)" to 1.09361,
        "Feet (ft)" to 3.28084,
        "Inches (in)" to 39.3701
    ),
    UnitCategory.WEIGHT to mapOf(
        "Kilograms (kg)" to 1.0,
        "Grams (g)" to 1000.0,
        "Pounds (lb)" to 2.20462,
        "Ounces (oz)" to 35.274,
        "Metric Tons (t)" to 0.001
    ),
    UnitCategory.TEMPERATURE to mapOf(
        "Celsius (°C)" to 1.0,
        "Fahrenheit (°F)" to 1.0,
        "Kelvin (K)" to 1.0
    ),
    UnitCategory.SPEED to mapOf(
        "Meters/sec (m/s)" to 1.0,
        "Kilometers/hour (km/h)" to 3.6,
        "Miles/hour (mph)" to 2.23694,
        "Knots (kn)" to 1.94384
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }

    val currentUnits = remember(selectedCategory) {
        UNIT_DATA[selectedCategory]?.keys?.toList() ?: emptyList()
    }

    var fromUnit by remember { mutableStateOf(currentUnits.firstOrNull() ?: "") }
    var toUnit by remember { mutableStateOf(currentUnits.getOrNull(1) ?: fromUnit) }
    var amountText by remember { mutableStateOf("1") }

    // When category changes, reset units
    LaunchedEffect(selectedCategory) {
        val list = UNIT_DATA[selectedCategory]?.keys?.toList() ?: emptyList()
        fromUnit = list.firstOrNull() ?: ""
        toUnit = list.getOrNull(1) ?: fromUnit
    }

    var isSelectingFrom by remember { mutableStateOf(false) }
    var isSelectingTo by remember { mutableStateOf(false) }

    var swapRot by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = swapRot,
        animationSpec = spring(),
        label = "swapUnitRot"
    )

    fun convertUnits(valIn: Double, from: String, to: String, category: UnitCategory): Double {
        if (from == to) return valIn
        if (category == UnitCategory.TEMPERATURE) {
            // Convert to Celsius first
            val inCelsius = when (from) {
                "Celsius (°C)" -> valIn
                "Fahrenheit (°F)" -> (valIn - 32.0) * 5.0 / 9.0
                "Kelvin (K)" -> valIn - 273.15
                else -> valIn
            }
            // Convert Celsius to Target
            return when (to) {
                "Celsius (°C)" -> inCelsius
                "Fahrenheit (°F)" -> (inCelsius * 9.0 / 5.0) + 32.0
                "Kelvin (K)" -> inCelsius + 273.15
                else -> inCelsius
            }
        } else {
            val map = UNIT_DATA[category] ?: return valIn
            val rateFrom = map[from] ?: 1.0
            val rateTo = map[to] ?: 1.0
            val base = valIn / rateFrom
            return base * rateTo
        }
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val converted = convertUnits(amount, fromUnit, toUnit, selectedCategory)

    Scaffold(
        topBar = {
            SubScreenTopBar(title = "Unit Converter", onBack = onBack)
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
            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedCategory = cat
                        },
                        label = { Text(cat.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Input Value TextField
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Value to convert") },
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

            // From / To Selector & Swap
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From Unit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isSelectingFrom = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fromUnit,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = AccentCyan)
                    }
                }

                // Swap Button
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        swapRot += 180f
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Units",
                        modifier = Modifier.rotate(animatedRotation),
                        tint = AccentOrange
                    )
                }

                // To Unit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isSelectingTo = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = toUnit,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(Icons.Default.ArrowDropDown, null, tint = AccentCyan)
                    }
                }
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
                    text = "$amountText $fromUnit =",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                val formattedConverted = if (converted == converted.toLong().toDouble()) {
                    String.format("%,d", converted.toLong())
                } else {
                    String.format(java.util.Locale.US, "%.5f", converted).trimEnd('0').trimEnd('.')
                }
                Text(
                    text = formattedConverted,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = toUnit,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Modal Sheet for Unit Picker
    if (isSelectingFrom || isSelectingTo) {
        ModalBottomSheet(
            onDismissRequest = {
                isSelectingFrom = false
                isSelectingTo = false
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isSelectingFrom) "Select 'From' Unit" else "Select 'To' Unit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentUnits) { u ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if ((isSelectingFrom && u == fromUnit) || (isSelectingTo && u == toUnit))
                                        AccentCyan.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isSelectingFrom) {
                                        fromUnit = u
                                        isSelectingFrom = false
                                    } else {
                                        toUnit = u
                                        isSelectingTo = false
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = u,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
