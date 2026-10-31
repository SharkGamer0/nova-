package com.novaforge.launcher.icon

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.novaforge.launcher.data.AppIconManager
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
            .clip(shape),
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
                    size = size,
                    packageName = packageName,
                    fallbackColor = fallbackColor
                )
            } else {
                // File missing or removed externally - graceful real icon fallback
                RenderNativeOrFallbackIcon(context, packageName, label, fallbackColor, size)
            }
        } else {
            // Load real native application icon from PackageManager with LRU caching
            RenderNativeOrFallbackIcon(context, packageName, label, fallbackColor, size)
        }
    }
}

@Composable
fun RenderNativeOrFallbackIcon(
    context: Context,
    packageName: String,
    label: String,
    fallbackColor: Color,
    size: Dp
) {
    val realBitmap = remember(packageName) {
        AppIconManager.getAppIconBitmap(context, packageName)
    }

    if (realBitmap != null) {
        Image(
            bitmap = realBitmap.asImageBitmap(),
            contentDescription = label,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fallbackColor),
            contentAlignment = Alignment.Center
        ) {
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
    size: Dp,
    packageName: String = "",
    fallbackColor: Color = Color(0xFF2A9D8F)
) {
    val context = LocalContext.current

    when (format) {
        AnimationFormat.GIF, AnimationFormat.WEBP -> {
            var hasError by remember { mutableStateOf(false) }

            if (!hasError && (isPlaybackAllowed || !staticFallback)) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(file)
                        .crossfade(true)
                        .build(),
                    contentDescription = fallbackLabel,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    onError = {
                        hasError = true
                    }
                )
            } else {
                RenderStaticFileFallback(
                    file = file,
                    fallbackLabel = fallbackLabel,
                    size = size,
                    context = context,
                    packageName = packageName,
                    fallbackColor = fallbackColor
                )
            }
        }

        AnimationFormat.LOTTIE -> {
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
            RenderStaticFileFallback(
                file = file,
                fallbackLabel = fallbackLabel,
                size = size,
                context = context,
                packageName = packageName,
                fallbackColor = fallbackColor
            )
        }
    }
}

@Composable
fun RenderStaticFileFallback(
    file: File,
    fallbackLabel: String,
    size: Dp,
    context: Context = LocalContext.current,
    packageName: String = "",
    fallbackColor: Color = Color(0xFF2A9D8F)
) {
    val bitmap = remember(file.absolutePath) {
        try {
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } catch (_: Throwable) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = fallbackLabel,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    } else {
        RenderNativeOrFallbackIcon(context, packageName, fallbackLabel, fallbackColor, size)
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
        fontSize = if (size > 40.dp) 18.sp else 14.sp
    )
}

@Composable
fun RenderProceduralAnimation(
    format: AnimationFormat,
    animationId: String,
    speed: Float,
    size: Dp,
    isPlaying: Boolean
) {
    val transition = rememberInfiniteTransition(label = "ProceduralAnim_$animationId")
    val duration = (1200 / speed.coerceIn(0.5f, 3.0f)).toInt()

    when (animationId) {
        "preset-pulse" -> {
            val scale by if (isPlaying) {
                transition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )
            } else remember { mutableFloatStateOf(1.0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.7f)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2A9D8F), Color(0xFF264653))
                        )
                    )
                }
            }
        }

        "preset-orbit" -> {
            val angle by if (isPlaying) {
                transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration * 2, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "orbit_angle"
                )
            } else remember { mutableFloatStateOf(0f) }

            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                val center = Offset(size.toPx() / 2, size.toPx() / 2)
                val orbitRadius = size.toPx() * 0.32f

                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = orbitRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                val rad = (angle * PI / 180).toFloat()
                val satelliteCenter = Offset(
                    x = center.x + orbitRadius * cos(rad),
                    y = center.y + orbitRadius * sin(rad)
                )

                drawCircle(
                    color = Color(0xFFE76F51),
                    radius = 4.dp.toPx(),
                    center = satelliteCenter
                )
            }
        }

        "preset-wave" -> {
            val waveOffset by if (isPlaying) {
                transition.animateFloat(
                    initialValue = -0.5f,
                    targetValue = 0.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "wave_offset"
                )
            } else remember { mutableFloatStateOf(0f) }

            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val width = size.toPx()
                val height = size.toPx()
                drawLine(
                    color = Color(0xFF2A9D8F),
                    start = Offset(0f, height * (0.5f + waveOffset * 0.2f)),
                    end = Offset(width, height * (0.5f - waveOffset * 0.2f)),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        "preset-sparks" -> {
            val rot by if (isPlaying) {
                transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 180f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "sparks_rot"
                )
            } else remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rot),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.65f)) {
                    drawLine(
                        color = Color(0xFFE9C46A),
                        start = Offset(0f, size.toPx() * 0.32f),
                        end = Offset(size.toPx() * 0.65f, size.toPx() * 0.32f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFFE9C46A),
                        start = Offset(size.toPx() * 0.32f, 0f),
                        end = Offset(size.toPx() * 0.32f, size.toPx() * 0.65f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        else -> {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                drawCircle(color = Color(0xFF2A9D8F))
            }
        }
    }
}
