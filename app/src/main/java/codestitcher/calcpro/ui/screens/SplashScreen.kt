package codestitcher.calcpro.ui.screens

import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.PathMeasure as AndroidPathMeasure
import android.graphics.RectF as AndroidRectF
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import codestitcher.calcpro.ui.util.HapticManager
import codestitcher.calcpro.ui.util.HapticType
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isFinished by remember { mutableStateOf(false) }
    var animationStarted by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(0f) }

    fun finishSplash() {
        if (!isFinished) {
            isFinished = true
            HapticManager.perform(context, haptic, HapticType.FUNCTION)
            onSplashFinished()
        }
    }

    // Sequence Timing:
    // 1. App opens instantly in pitch black (#000000) matching native window background.
    // 2. Just a brief ~120ms pause in black, then the animation draws smoothly.
    // 3. Exactly 1 full loop of 3.2s animation.
    // 4. Smooth transition to calculator screen.
    LaunchedEffect(Unit) {
        delay(120) // Brief subtle pause before lines trace out
        animationStarted = true
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 3200, // Exactly 1 complete loop (3.2s)
                easing = LinearEasing
            )
        )
        finishSplash()
    }

    // Pre-allocated static vectors & geometry (SVG 1000x360 coordinate space)
    val capsule1Path = remember {
        AndroidPath().apply {
            addRoundRect(AndroidRectF(-72f, -95f, -6f, 55f), 33f, 33f, AndroidPath.Direction.CW)
        }
    }
    val capsule2Path = remember {
        AndroidPath().apply {
            addRoundRect(AndroidRectF(6f, -55f, 72f, 95f), 33f, 33f, AndroidPath.Direction.CW)
        }
    }
    val measureCap1 = remember { AndroidPathMeasure(capsule1Path, true) }
    val measureCap2 = remember { AndroidPathMeasure(capsule2Path, true) }

    val textPath = remember {
        val basePaint = AndroidPaint().apply {
            isAntiAlias = true
            textSize = 96f
            typeface = AndroidTypeface.create("sans-serif", AndroidTypeface.BOLD)
            letterSpacing = -0.025f
        }
        AndroidPath().apply {
            basePaint.getTextPath("CodeStitcher", 0, "CodeStitcher".length, 320f, 212f, this)
        }
    }

    // Pre-allocated paints
    val capsuleOutlinePaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeWidth = 4.5f
            color = AndroidColor.WHITE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
        }
    }
    val capsuleFillPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
            color = AndroidColor.WHITE
        }
    }
    val stitchPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeWidth = 7.5f
            color = AndroidColor.BLACK
            strokeCap = AndroidPaint.Cap.ROUND
        }
    }
    val textOutlinePaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeWidth = 2.2f
            color = AndroidColor.WHITE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
        }
    }
    val textFillPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.FILL
            color = AndroidColor.WHITE
        }
    }

    // Reusable segment paths for allocation-free 120 FPS rendering
    val segCap1 = remember { AndroidPath() }
    val segCap2 = remember { AndroidPath() }
    val segText = remember { AndroidPath() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    // Tap anywhere to skip immediately
                    finishSplash()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (animationStarted) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val t = animProgress.value

                // Phase interpolation matching CSS @keyframes
                fun phase(start: Float, end: Float): Float = when {
                    t < start -> 0f
                    t > end -> 1f
                    else -> (t - start) / (end - start)
                }

                // Capsule 1 (delay 0s): draw outline 0..35%, fill 35..55%
                val cap1DrawProgress = phase(0.00f, 0.35f)
                val cap1FillAlpha = phase(0.35f, 0.55f)

                // Capsule 2 (delay 0.2s): draw outline 6..41%, fill 41..60%
                val cap2DrawProgress = phase(0.06f, 0.41f)
                val cap2FillAlpha = phase(0.41f, 0.60f)

                // Stitch Cut line (starts 20%, draws to 40% across gap)
                val stitchDrawProgress = phase(0.20f, 0.40f)

                // Text (delay 0.4s): draw outline 12..45%, fill 45..65%
                val textDrawProgress = phase(0.12f, 0.45f)
                val textFillAlpha = phase(0.45f, 0.65f)

                // Global fade out near end of loop (88% to 100%)
                val globalAlpha = when {
                    t < 0.88f -> 1f
                    else -> 1f - phase(0.88f, 1.00f)
                }

                val targetWidth = 1000f
                val targetHeight = 360f

                // Responsive scaling to fit all phone screens perfectly
                val scale = (size.width * 0.90f / targetWidth).coerceAtMost(size.height * 0.70f / targetHeight)
                val offsetX = (size.width - targetWidth * scale) / 2f
                val offsetY = (size.height - targetHeight * scale) / 2f

                drawContext.canvas.nativeCanvas.apply {
                    save()
                    translate(offsetX, offsetY)
                    scale(scale, scale)

                    // 1. THE 'S-STITCH' MARK (Centered at X: 180, Y: 180 and rotated 45°)
                    save()
                    translate(180f, 180f)
                    rotate(45f)

                    // Capsule 1 & 2 Fills
                    if (cap1FillAlpha > 0f) {
                        capsuleFillPaint.alpha = (cap1FillAlpha * globalAlpha * 255).toInt().coerceIn(0, 255)
                        drawPath(capsule1Path, capsuleFillPaint)
                    }
                    if (cap2FillAlpha > 0f) {
                        capsuleFillPaint.alpha = (cap2FillAlpha * globalAlpha * 255).toInt().coerceIn(0, 255)
                        drawPath(capsule2Path, capsuleFillPaint)
                    }

                    // Capsule 1 & 2 Outlines
                    capsuleOutlinePaint.alpha = (globalAlpha * 255).toInt().coerceIn(0, 255)
                    if (cap1DrawProgress > 0f) {
                        segCap1.reset()
                        measureCap1.getSegment(0f, measureCap1.length * cap1DrawProgress, segCap1, true)
                        drawPath(segCap1, capsuleOutlinePaint)
                    }
                    if (cap2DrawProgress > 0f) {
                        segCap2.reset()
                        measureCap2.getSegment(0f, measureCap2.length * cap2DrawProgress, segCap2, true)
                        drawPath(segCap2, capsuleOutlinePaint)
                    }

                    // Stitch Cut Line (Black stitch cut on top of white capsules across the gap)
                    if (stitchDrawProgress > 0f) {
                        stitchPaint.alpha = (globalAlpha * 255).toInt().coerceIn(0, 255)
                        val curX = -20f + 40f * stitchDrawProgress
                        drawLine(-20f, 0f, curX, 0f, stitchPaint)
                    }

                    restore() // End of 'S-STITCH' MARK

                    // 2. BRAND TYPOGRAPHY ("CodeStitcher")
                    if (textFillAlpha > 0f) {
                        textFillPaint.alpha = (textFillAlpha * globalAlpha * 255).toInt().coerceIn(0, 255)
                        drawPath(textPath, textFillPaint)
                    }

                    if (textDrawProgress > 0f) {
                        textOutlinePaint.alpha = (globalAlpha * 255).toInt().coerceIn(0, 255)
                        segText.reset()
                        val textMeasure = AndroidPathMeasure(textPath, false)
                        val p = textDrawProgress.coerceIn(0f, 1f)
                        do {
                            textMeasure.getSegment(0f, textMeasure.length * p, segText, true)
                        } while (textMeasure.nextContour())
                        drawPath(segText, textOutlinePaint)
                    }

                    restore() // End of viewport transform
                }
            }
        }
    }
}
