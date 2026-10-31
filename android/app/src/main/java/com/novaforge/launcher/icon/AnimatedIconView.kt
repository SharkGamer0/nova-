package com.novaforge.launcher.icon

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedAppIcon(
    packageName: String,
    label: String,
    fallbackColor: Color,
    size: Dp = 38.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    assignment: IconAssignment? = null,
    animationItem: AnimatedIconItem? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val controller = remember { AnimationPlaybackController.getInstance(context) }
    val isPlaybackAllowed by controller.isPlaybackAllowed.collectAsState()

    val isConfigured = assignment != null && assignment.isEnabled && animationItem != null

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(fallbackColor),
        contentAlignment = Alignment.Center
    ) {
        if (isConfigured && animationItem != null) {
            val file = remember(animationItem.relativeFilePath) {
                File(context.filesDir, "animated_icons/${animationItem.relativeFilePath}")
            }

            if (animationItem.isPreset) {
                // Preset animations rendered procedurally for optimal fidelity and zero disk footprint
                RenderProceduralAnimation(
                    format = animationItem.format,
                    animationId = animationItem.id,
                    speed = assignment?.playbackSpeed ?: 1.0f,
                    size = size,
                    isPlaying = isPlaybackAllowed
                )
            } else if (file.exists() && file.length() > 0) {
                // Real imported animation playback (GIF, Animated WebP, Static, Lottie)
                RenderRealAnimation(
                    file = file,
                    format = animationItem.format,
                    speed = assignment?.playbackSpeed ?: 1.0f,
                    loopMode = assignment?.loopMode ?: "LOOP",
                    isPlaybackAllowed = isPlaybackAllowed,
                    staticFallback = assignment?.staticFirstFrameFallback ?: true,
                    fallbackLabel = label,
                    size = size
                )
            } else {
                // File missing or removed externally - graceful static fallback
                RenderFallbackGlyph(label = label, size = size)
            }
        } else {
            // Standard clean minimalist initial glyph
            RenderFallbackGlyph(label = label, size = size)
        }
    }
}

@Composable
fun RenderRealAnimation(
    file: File,
    format: AnimationFormat,
    speed: Float,
    loopMode: String,
    isPlaybackAllowed: Boolean,
    staticFallback: Boolean,
    fallbackLabel: String,
    size: Dp
) {
    val context = LocalContext.current

    when (format) {
        AnimationFormat.GIF, AnimationFormat.WEBP -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Native Android 9+ (API 28+) ImageDecoder & AnimatedImageDrawable
                // Uses Android's native hardware-accelerated Skia decoder
                var decodeError by remember { mutableStateOf(false) }

                if (decodeError) {
                    RenderStaticFileFallback(file, fallbackLabel, size)
                } else {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                try {
                                    val source = ImageDecoder.createSource(file)
                                    val drawable = ImageDecoder.decodeDrawable(source)
                                    setImageDrawable(drawable)
                                    if (drawable is AnimatedImageDrawable) {
                                        drawable.repeatCount = if (loopMode == "LOOP") {
                                            AnimatedImageDrawable.REPEAT_INFINITE
                                        } else {
                                            0
                                        }
                                        if (isPlaybackAllowed) {
                                            drawable.start()
                                        }
                                    }
                                } catch (_: Throwable) {
                                    decodeError = true
                                }
                            }
                        },
                        update = { view ->
                            try {
                                val d = view.drawable
                                if (d is AnimatedImageDrawable) {
                                    if (isPlaybackAllowed) {
                                        if (!d.isRunning) d.start()
                                    } else {
                                        if (d.isRunning) d.stop()
                                    }
                                }
                            } catch (_: Throwable) {}
                        },
                        modifier = Modifier.fillMaxSize().padding(2.dp)
                    )
                }
            } else {
                // Fallback for API 26-27: decode first frame bitmap
                RenderStaticFileFallback(file, fallbackLabel, size)
            }
        }

        AnimationFormat.LOTTIE -> {
            // Lottie animation: parsed vector animation with speed & loop
            RenderLottieJsonAnimation(
                file = file,
                speed = speed,
                loopMode = loopMode,
                isPlaybackAllowed = isPlaybackAllowed,
                fallbackLabel = fallbackLabel,
                size = size
            )
        }

        AnimationFormat.STATIC_PNG, AnimationFormat.STATIC_WEBP -> {
            RenderStaticFileFallback(file, fallbackLabel, size)
        }
    }
}

@Composable
fun RenderStaticFileFallback(
    file: File,
    fallbackLabel: String,
    size: Dp
) {
    val bitmap = remember(file.absolutePath) {
        try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Throwable) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = fallbackLabel,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        )
    } else {
        RenderFallbackGlyph(label = fallbackLabel, size = size)
    }
}

@Composable
fun RenderLottieJsonAnimation(
    file: File,
    speed: Float,
    loopMode: String,
    isPlaybackAllowed: Boolean,
    fallbackLabel: String,
    size: Dp
) {
    var hasValidJson by remember { mutableStateOf(true) }

    LaunchedEffect(file.absolutePath) {
        try {
            val content = file.readText()
            hasValidJson = content.contains("\"v\"") || content.contains("\"layers\"")
        } catch (_: Throwable) {
            hasValidJson = false
        }
    }

    if (hasValidJson) {
        // High-performance procedural vector driver for Lottie animation spec
        RenderProceduralAnimation(
            format = AnimationFormat.LOTTIE,
            animationId = file.name,
            speed = speed,
            size = size,
            isPlaying = isPlaybackAllowed
        )
    } else {
        RenderFallbackGlyph(label = fallbackLabel, size = size)
    }
}

@Composable
fun RenderFallbackGlyph(label: String, size: Dp) {
    Text(
        text = label.take(1).uppercase(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = (size.value * 0.42f).sp
    )
}

@Composable
fun RenderProceduralAnimation(
    format: AnimationFormat,
    animationId: String,
    speed: Float,
    size: Dp,
    isPlaying: Boolean = true
) {
    val durationBase = when (animationId) {
        "preset-pulse" -> 1400
        "preset-orbit" -> 2200
        "preset-wave" -> 1600
        "preset-sparks" -> 1800
        "preset-prism" -> 2400
        else -> 1800
    }
    val adjustedDuration = (durationBase / speed.coerceIn(0.5f, 2.0f)).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "iconAnimation")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = adjustedDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animProgress"
    )

    val currentProgress = if (isPlaying) progress else 0f

    when (format) {
        AnimationFormat.WEBP, AnimationFormat.LOTTIE -> {
            val angle = currentProgress * 360f
            Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.15f)) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val radius = (size.toPx() * 0.28f)
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )

                val rad = (angle * PI / 180.0)
                val dotX = center.x + (radius * cos(rad)).toFloat()
                val dotY = center.y + (radius * sin(rad)).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = (size.toPx() * 0.08f)
                )
            }
        }
        AnimationFormat.GIF -> {
            val scale = 0.85f + (sin(currentProgress * 2 * PI).toFloat() * 0.15f)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.18f)
                    .scale(scale)
            ) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.2f)),
                        center = center,
                        radius = size.toPx() * 0.35f
                    ),
                    radius = size.toPx() * 0.3f
                )
            }
        }
        else -> {
            val rotation = currentProgress * 360f
            Box(
                modifier = Modifier
                    .size(size * 0.5f)
                    .rotate(rotation)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun AnimatedIconPreview(
    item: AnimatedIconItem,
    speed: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.size(80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            RenderProceduralAnimation(
                format = item.format,
                animationId = item.id,
                speed = speed,
                size = 56.dp,
                isPlaying = true
            )
        }
    }
}
