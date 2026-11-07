package codestitcher.calcpro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codestitcher.calcpro.calculation.ExpressionEvaluator
import codestitcher.calcpro.data.CalculationHistoryItem
import codestitcher.calcpro.data.HistoryRepository
import codestitcher.calcpro.ui.components.ButtonType
import codestitcher.calcpro.ui.components.CalcProTopBar
import codestitcher.calcpro.ui.components.CalculatorButton
import codestitcher.calcpro.ui.theme.AccentCyan
import codestitcher.calcpro.ui.theme.AccentOrange
import codestitcher.calcpro.ui.theme.AccentPurple
import codestitcher.calcpro.ui.theme.CalcProTheme
import codestitcher.calcpro.ui.util.HapticManager
import codestitcher.calcpro.ui.util.HapticType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val composeHaptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Persistent History Repository
    val historyRepo = remember { HistoryRepository(context) }
    val history = remember {
        mutableStateListOf<CalculationHistoryItem>().apply {
            addAll(historyRepo.loadHistory())
        }
    }

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var livePreview by remember { mutableStateOf("") }
    var lastEvaluated by remember { mutableStateOf(false) }
    var lastNumericAnswer by remember { mutableDoubleStateOf(0.0) }

    var isSciMode by remember { mutableStateOf(false) }
    var isDegreeMode by remember { mutableStateOf(true) }
    var isShiftMode by remember { mutableStateOf(false) } // Casio 2nd / Shift mode

    var showHistorySheet by remember { mutableStateOf(false) }

    // Scroll states for single line display with horizontal scroll
    val exprScrollState = rememberScrollState()
    val resultScrollState = rememberScrollState()

    // Real-time live calculation preview
    LaunchedEffect(expression, isDegreeMode) {
        if (expression.isNotBlank() && !lastEvaluated) {
            val eval = ExpressionEvaluator.evaluate(expression, isDegreeMode, lastNumericAnswer)
            livePreview = if (eval is ExpressionEvaluator.EvalResult.Success) {
                eval.formatted
            } else {
                ""
            }
        } else {
            livePreview = ""
        }
        // Auto scroll expression to the end (newest input)
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    // Auto scroll result to end when evaluated
    LaunchedEffect(resultText, livePreview) {
        resultScrollState.animateScrollTo(resultScrollState.maxValue)
    }

    fun onInput(char: String) {
        if (lastEvaluated) {
            expression = if (char in listOf("+", "-", "×", "÷", "^", "²", "³", "%", "P", "C", "⁻¹")) {
                resultText + char
            } else {
                char
            }
            lastEvaluated = false
        } else {
            expression += char
        }
    }

    fun onSmartParentheses() {
        if (lastEvaluated) {
            expression = "("
            lastEvaluated = false
            return
        }
        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }
        val shouldClose = openCount > closeCount && expression.isNotEmpty() &&
                (expression.last().isDigit() || expression.last() == ')' || expression.last() == 'π' || expression.last() == 'e' || expression.last() == '%' || expression.last() == '²' || expression.last() == '³' || expression.last() == '¹')

        expression += if (shouldClose) ")" else "("
    }

    fun onBackspace() {
        if (expression.isNotEmpty()) {
            val multiCharFuncs = listOf(
                "sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "asin(", "acos(", "atan(",
                "sinh(", "cosh(", "tanh(", "sin(", "cos(", "tan(",
                "sqrt(", "cbrt(", "log(", "ln(", "abs(", "10^(", "e^("
            )
            val matchedFunc = multiCharFuncs.find { expression.endsWith(it) }
            expression = if (matchedFunc != null) {
                expression.dropLast(matchedFunc.length)
            } else {
                expression.dropLast(1)
            }
            lastEvaluated = false
        }
    }

    fun onClear() {
        expression = ""
        resultText = "0"
        livePreview = ""
        lastEvaluated = false
    }

    fun onEquals() {
        if (expression.isBlank()) return

        when (val eval = ExpressionEvaluator.evaluate(expression, isDegreeMode, lastNumericAnswer)) {
            is ExpressionEvaluator.EvalResult.Success -> {
                val evaluated = eval.formatted
                lastNumericAnswer = eval.value
                val historyItem = CalculationHistoryItem(expression = expression, result = evaluated)
                history.add(0, historyItem)
                historyRepo.saveHistory(history) // Save permanently to SharedPreferences
                resultText = evaluated
                lastEvaluated = true
            }
            is ExpressionEvaluator.EvalResult.Error -> {
                resultText = eval.message
                lastEvaluated = true
            }
        }
    }

    fun onCopyResult() {
        val toCopy = if (lastEvaluated) resultText else if (expression.isNotEmpty()) expression else "0"
        clipboardManager.setText(AnnotatedString(toCopy))
        HapticManager.perform(context, composeHaptic, HapticType.FUNCTION)
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Copied $toCopy to clipboard")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CalcProTopBar(
                isSciMode = isSciMode,
                isDarkTheme = isDarkTheme,
                historyCount = history.size,
                onToggleSciMode = { isSciMode = !isSciMode },
                onToggleTheme = onToggleTheme,
                onNavigateToCurrency = { onNavigateToRoute("currency_converter") },
                onNavigateToUnits = { onNavigateToRoute("unit_converter") },
                onOpenHistory = { showHistorySheet = true }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp) // Avoid double navigation bar padding
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .navigationBarsPadding()
                .padding(horizontal = 14.dp)
                .padding(bottom = 8.dp), // Snug, comfortable bottom spacing (No floating gaps!)
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. OLED Display Card (Expands naturally to absorb available vertical space!)
            val displayBg = if (isDarkTheme) Color(0xFF131720) else MaterialTheme.colorScheme.surface
            val displayBorder = if (isDarkTheme) Color(0xFF222836) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .heightIn(min = 135.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(displayBg)
                    .border(1.dp, displayBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Top status bar inside display card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Chips: DEG/RAD + SCI + SHIFT
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DEG / RAD Toggle Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDegreeMode) AccentCyan.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isDegreeMode) AccentCyan.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    HapticManager.perform(context, composeHaptic, HapticType.MODE_TOGGLE)
                                    isDegreeMode = !isDegreeMode
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isDegreeMode) "DEG" else "RAD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDegreeMode) AccentCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // SCI Mode Indicator Chip
                        if (isSciMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentPurple.copy(alpha = 0.15f))
                                    .border(1.dp, AccentPurple.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SCI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurple
                                )
                            }
                        }

                        // SHIFT Mode Indicator Chip (Casio fx-991EX style)
                        if (isSciMode && isShiftMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFD600).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFFFFD600).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SHIFT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD600)
                                )
                            }
                        }
                    }

                    // Copy to clipboard action
                    IconButton(
                        onClick = { onCopyResult() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle: Expression - Strictly SINGLE LINE with horizontal scroll!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(exprScrollState, reverseScrolling = true),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Bottom: Result / Live Preview - Strictly SINGLE LINE with horizontal scroll!
                val displayVal = if (lastEvaluated) resultText else if (livePreview.isNotEmpty()) "= $livePreview" else ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(resultScrollState, reverseScrolling = true)
                        .clickable { onCopyResult() },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayVal.ifEmpty { if (lastEvaluated) resultText else "" },
                        color = when {
                            lastEvaluated -> AccentCyan
                            displayVal.startsWith("=") -> AccentCyan.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Anchored Keypad Area (Positioned naturally at the bottom for effortless thumb reach!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Casio fx-991EX Scientific Grid (Expands smoothly when SCI is active)
                AnimatedVisibility(
                    visible = isSciMode,
                    enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(160)) + fadeOut(tween(160))
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        // Casio Sci Row 1: SHIFT/2nd, sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹, ln/eˣ, log/10ˣ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                text = if (isShiftMode) "2nd" else "SHIFT",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { isShiftMode = !isShiftMode }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "sin⁻¹" else "sin",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "sin⁻¹(" else "sin(") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "cos⁻¹" else "cos",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "cos⁻¹(" else "cos(") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "tan⁻¹" else "tan",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "tan⁻¹(" else "tan(") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "eˣ" else "ln",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "e^(" else "ln(") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "10ˣ" else "log",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "10^(" else "log(") }
                            )
                        }

                        // Casio Sci Row 2: x²/x³, xⁿ/ⁿ√, √/³√, x⁻¹/nPr, nCr/MOD, x!/|x|, π/ANS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorButton(
                                text = if (isShiftMode) "x³" else "x²",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "³" else "²") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "ⁿ√" else "xʸ",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "√(" else "^") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "³√" else "√",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "cbrt(" else "sqrt(") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "nPr" else "x⁻¹",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "P" else "⁻¹") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "MOD" else "nCr",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "%" else "C") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "|x|" else "x!",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "abs(" else "!") }
                            )
                            CalculatorButton(
                                text = if (isShiftMode) "π" else "ANS",
                                type = ButtonType.SCI,
                                isDark = isDarkTheme,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                onClick = { onInput(if (isShiftMode) "π" else "ANS") }
                            )
                        }
                    }
                }

                // Standard 5 Keypad Rows (Comfortable height ~64dp, resting cleanly near the bottom)
                val standardRowHeight = 64.dp

                // Row 1: C, ( ), %, ÷
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(standardRowHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(text = "C", type = ButtonType.FUNCTION, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onClear() })
                    CalculatorButton(text = "( )", type = ButtonType.FUNCTION, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onSmartParentheses() })
                    CalculatorButton(text = "%", type = ButtonType.FUNCTION, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("%") })
                    CalculatorButton(text = "÷", type = ButtonType.OPERATOR, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("÷") })
                }

                // Row 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(standardRowHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(text = "7", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("7") })
                    CalculatorButton(text = "8", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("8") })
                    CalculatorButton(text = "9", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("9") })
                    CalculatorButton(text = "×", type = ButtonType.OPERATOR, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("×") })
                }

                // Row 3: 4, 5, 6, -
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(standardRowHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(text = "4", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("4") })
                    CalculatorButton(text = "5", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("5") })
                    CalculatorButton(text = "6", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("6") })
                    CalculatorButton(text = "-", type = ButtonType.OPERATOR, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("-") })
                }

                // Row 4: 1, 2, 3, +
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(standardRowHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(text = "1", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("1") })
                    CalculatorButton(text = "2", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("2") })
                    CalculatorButton(text = "3", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("3") })
                    CalculatorButton(text = "+", type = ButtonType.OPERATOR, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("+") })
                }

                // Row 5: ⌫, 0, ., =
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(standardRowHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalculatorButton(
                        text = "⌫",
                        type = ButtonType.FUNCTION,
                        icon = Icons.AutoMirrored.Outlined.Backspace,
                        isDark = isDarkTheme,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onBackspace() }
                    )
                    CalculatorButton(text = "0", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput("0") })
                    CalculatorButton(text = ".", type = ButtonType.NUMBER, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onInput(".") })
                    CalculatorButton(text = "=", type = ButtonType.EQUALS, isDark = isDarkTheme, modifier = Modifier.weight(1f).fillMaxHeight(), onClick = { onEquals() })
                }
            }
        }
    }

    // Permanent History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calculation History (${history.size})",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (history.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                history.clear()
                                historyRepo.clearHistory()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = AccentOrange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No calculations recorded yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(history) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .clickable {
                                        expression = item.result
                                        resultText = item.result
                                        lastEvaluated = true
                                        showHistorySheet = false
                                    }
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = item.expression,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "= ${item.result}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Calculator Dark Mode", showBackground = true)
@Composable
fun CalculatorScreenDarkPreview() {
    CalcProTheme(darkTheme = true) {
        CalculatorScreen(isDarkTheme = true, onToggleTheme = {}, onNavigateToRoute = {})
    }
}

@Preview(name = "Calculator Light Mode", showBackground = true)
@Composable
fun CalculatorScreenLightPreview() {
    CalcProTheme(darkTheme = false) {
        CalculatorScreen(isDarkTheme = false, onToggleTheme = {}, onNavigateToRoute = {})
    }
}
