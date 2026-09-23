package com.example.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/**
 * Checks and binds galaxy.mp4 from:
 * 1. Assets: galaxy.mp4
 * 2. Raw resources: R.raw.galaxy
 * 3. App files directory: files/galaxy.mp4 or externalFiles/galaxy.mp4
 */
object GalaxyVideoSourceHelper {
    fun bindDataSource(context: Context, mediaPlayer: MediaPlayer): Boolean {
        // 1. Try Assets (e.g. assets/galaxy.mp4)
        try {
            val afd = context.assets.openFd("galaxy.mp4")
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            return true
        } catch (_: Exception) {
        }

        // 2. Try Raw Resource (e.g. res/raw/galaxy.mp4)
        try {
            val rawId = context.resources.getIdentifier("galaxy", "raw", context.packageName)
            if (rawId != 0) {
                val afd = context.resources.openRawResourceFd(rawId)
                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                return true
            }
        } catch (_: Exception) {
        }

        // 3. Try App Internal Storage
        try {
            val localFile = File(context.filesDir, "galaxy.mp4")
            if (localFile.exists() && localFile.length() > 0) {
                mediaPlayer.setDataSource(localFile.absolutePath)
                return true
            }
        } catch (_: Exception) {
        }

        // 4. Try External Files Storage
        try {
            val extDir = context.getExternalFilesDir(null)
            if (extDir != null) {
                val extFile = File(extDir, "galaxy.mp4")
                if (extFile.exists() && extFile.length() > 0) {
                    mediaPlayer.setDataSource(extFile.absolutePath)
                    return true
                }
            }
        } catch (_: Exception) {
        }

        return false
    }
}

/**
 * Dedicated video player composable for galaxy.mp4 in the manga details header card.
 * Plays the video silently and continuously in a loop behind the card content.
 * Gracefully renders an animated procedural cosmic galaxy if the file is being loaded
 * or not yet placed in assets/raw.
 */
@Composable
fun GalaxyBackgroundVideo(
    modifier: Modifier = Modifier,
    alpha: Float = 0.75f
) {
    val context = LocalContext.current
    var isVideoActive by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Procedural Cosmic Galaxy fallback & ambient background
        CosmicGalaxyAtmosphere(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = if (isVideoActive) alpha * 0.4f else alpha }
        )

        // Android TextureView for hardware-accelerated video rendering with proper Compose clipping
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        private var player: MediaPlayer? = null

                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            try {
                                val mp = MediaPlayer()
                                mp.setSurface(Surface(surface))
                                val hasSource = GalaxyVideoSourceHelper.bindDataSource(ctx, mp)
                                if (hasSource) {
                                    mp.isLooping = true
                                    mp.setVolume(0f, 0f) // Silent ambient aesthetic playback
                                    mp.setOnVideoSizeChangedListener { _, videoWidth, videoHeight ->
                                        if (videoWidth > 0 && videoHeight > 0) {
                                            adjustAspectRatio(this@apply, width, height, videoWidth, videoHeight)
                                        }
                                    }
                                    mp.setOnPreparedListener { p ->
                                        p.start()
                                        isVideoActive = true
                                    }
                                    mp.setOnErrorListener { _, _, _ ->
                                        isVideoActive = false
                                        true
                                    }
                                    mp.prepareAsync()
                                    player = mp
                                } else {
                                    mp.release()
                                    isVideoActive = false
                                }
                            } catch (_: Exception) {
                                isVideoActive = false
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            try {
                                player?.stop()
                                player?.release()
                            } catch (_: Exception) {
                            }
                            player = null
                            isVideoActive = false
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha }
        )
    }
}

/**
 * Adjusts the TextureView matrix to center-crop the video within the view bounds.
 */
private fun adjustAspectRatio(textureView: TextureView, viewWidth: Int, viewHeight: Int, videoWidth: Int, videoHeight: Int) {
    if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return

    val scaleX = viewWidth.toFloat() / videoWidth.toFloat()
    val scaleY = viewHeight.toFloat() / videoHeight.toFloat()
    val maxScale = maxOf(scaleX, scaleY)

    val scaledWidth = videoWidth * maxScale
    val scaledHeight = videoHeight * maxScale

    val matrix = Matrix()
    matrix.setScale(scaledWidth / viewWidth, scaledHeight / viewHeight, viewWidth / 2f, viewHeight / 2f)
    textureView.setTransform(matrix)
}

/**
 * Procedural ambient galaxy animation displayed as an aesthetic foundation
 * and high-fidelity fallback whenever galaxy.mp4 is loading.
 */
@Composable
fun CosmicGalaxyAtmosphere(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_cosmic_anim")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "galaxy_rotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "galaxy_pulse"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .background(Color(0xFF090D16))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            val centerX = width * 0.55f
            val centerY = height * 0.5f

            // 1. Deep Cosmic Nebula Gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f * pulseGlow),
                        Color(0xFF6B21A8).copy(alpha = 0.35f * pulseGlow),
                        Color(0xFF1E1B4B).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxOf(width, height) * 0.85f
                )
            )

            // 2. Swirling Galaxy Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryColor.copy(alpha = 0.70f * pulseGlow),
                        primaryColor.copy(alpha = 0.40f * pulseGlow),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = width * 0.42f
                ),
                center = Offset(centerX, centerY),
                radius = width * 0.42f
            )

            // 3. Starlight Particles
            val starCount = 38
            for (i in 0 until starCount) {
                val angle = Math.toRadians((i * (360f / starCount) + rotationAngle).toDouble())
                val dist = (width * 0.12f) + ((i * 19) % (width.toInt() / 2)).toFloat()
                val starX = (centerX + cos(angle) * dist).toFloat()
                val starY = (centerY + sin(angle) * (dist * 0.65f)).toFloat()

                if (starX in 0f..width && starY in 0f..height) {
                    val starAlpha = ((i % 5 + 1) / 5f) * pulseGlow
                    val starRadius = if (i % 7 == 0) 2.4f else 1.4f
                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        radius = starRadius,
                        center = Offset(starX, starY)
                    )
                }
            }
        }
    }
}
