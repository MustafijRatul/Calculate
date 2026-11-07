package codestitcher.calcpro.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

enum class HapticType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    EQUALS,
    BACKSPACE,
    CLEAR,
    MODE_TOGGLE
}

object HapticManager {
    fun perform(context: Context, composeHaptic: HapticFeedback?, type: HapticType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val effect = when (type) {
                        HapticType.NUMBER -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        HapticType.OPERATOR -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.FUNCTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.EQUALS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        HapticType.BACKSPACE -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        HapticType.CLEAR -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                        HapticType.MODE_TOGGLE -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                    vibrator.vibrate(effect)
                    return
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val effect = when (type) {
                        HapticType.NUMBER -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        HapticType.OPERATOR -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.FUNCTION -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.EQUALS -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        HapticType.BACKSPACE -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        HapticType.CLEAR -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                        HapticType.MODE_TOGGLE -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                    vibrator.vibrate(effect)
                    return
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val (duration, amplitude) = when (type) {
                        HapticType.NUMBER -> Pair(12L, 70)
                        HapticType.OPERATOR -> Pair(22L, 160)
                        HapticType.FUNCTION -> Pair(20L, 140)
                        HapticType.EQUALS -> Pair(35L, 255)
                        HapticType.BACKSPACE -> Pair(15L, 100)
                        HapticType.CLEAR -> Pair(28L, 200)
                        HapticType.MODE_TOGGLE -> Pair(18L, 120)
                    }
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                    return
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(20L)
                return
            }
        } catch (_: Exception) {
            // Fallback to Compose haptic
        }

        when (type) {
            HapticType.NUMBER, HapticType.BACKSPACE -> composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
