package codestitcher.calcpro.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codestitcher.calcpro.ui.theme.*
import codestitcher.calcpro.ui.util.HapticManager
import codestitcher.calcpro.ui.util.HapticType

enum class ButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    SCI,
    EQUALS
}

@Composable
fun CalculatorButton(
    text: String,
    type: ButtonType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val composeHaptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 120 FPS hardware-accelerated spring scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnScale"
    )

    // Cached solid colors
    val (solidBg, textColor, borderColor) = remember(type, isDark) {
        when (type) {
            ButtonType.NUMBER -> if (isDark) {
                Triple(Color(0xFF181C24), TextPrimaryDark, Color(0xFF262C38))
            } else {
                Triple(Color(0xFFFFFFFF), TextPrimaryLight, Color(0xFFE2E8F0))
            }
            ButtonType.OPERATOR -> {
                Triple(Color(0xFFFF9500), Color.White, Color(0x33FFA000))
            }
            ButtonType.FUNCTION -> if (isDark) {
                Triple(Color(0xFF1E2534), AccentCyan, Color(0x3300E5FF))
            } else {
                Triple(Color(0xFFEDF2F7), Color(0xFF0284C7), Color(0xFFCBD5E1))
            }
            ButtonType.SCI -> if (isDark) {
                Triple(Color(0xFF192030), AccentPurple, Color(0x33B388FF))
            } else {
                Triple(Color(0xFFF1F5F9), Color(0xFF7C3AED), Color(0xFFE2E8F0))
            }
            ButtonType.EQUALS -> {
                Triple(Color(0xFF00E5FF), Color(0xFF0A101D), Color(0x6600E5FF))
            }
        }
    }

    val shape = remember { RoundedCornerShape(18.dp) }

    Box(
        modifier = modifier
            // Defers scale read to the Draw phase directly on RenderThread GPU (Zero recomposition!)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(solidBg)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    val hapticType = when {
                        text == "C" -> HapticType.CLEAR
                        text == "⌫" || icon != null -> HapticType.BACKSPACE
                        type == ButtonType.EQUALS -> HapticType.EQUALS
                        type == ButtonType.OPERATOR -> HapticType.OPERATOR
                        type == ButtonType.FUNCTION -> HapticType.FUNCTION
                        type == ButtonType.SCI -> HapticType.FUNCTION
                        else -> HapticType.NUMBER
                    }
                    HapticManager.perform(context, composeHaptic, hapticType)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = when {
                    text.length > 4 -> 12.sp
                    text.length > 3 -> 14.sp
                    text.length > 2 -> 16.sp
                    type == ButtonType.OPERATOR -> 26.sp
                    type == ButtonType.EQUALS -> 28.sp
                    else -> 23.sp
                },
                fontWeight = when (type) {
                    ButtonType.EQUALS -> FontWeight.ExtraBold
                    ButtonType.OPERATOR -> FontWeight.Bold
                    ButtonType.NUMBER -> FontWeight.Medium
                    else -> FontWeight.SemiBold
                }
            )
        }
    }
}
